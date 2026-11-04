package kr.toxicity.damage.compatibility.nexo

import com.nexomc.nexo.api.NexoItems
import kr.toxicity.damage.api.BetterDamage
import kr.toxicity.damage.manager.DamageSkinManagerImpl
import kr.toxicity.damage.util.*
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NexoSkinCoupons : Listener, AutoCloseable {
    private val pending = ConcurrentHashMap<UUID, CouponTransaction>()
    private val luckPerms = LuckPermsProvider.get()
    private val confirmations = CouponConfirmationDialog()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun use(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.useItemInHand() == Event.Result.DENY) return
        val hand = event.hand ?: return
        val item = event.item ?: return
        val itemId = NexoItems.idFromItem(item) ?: return
        if (!itemId.endsWith("_coupon")) return
        val id = itemId.removeSuffix("_coupon")
        val skin = DamageSkinManagerImpl.skin(id)?.takeIf { it.name() == id } ?: return
        event.isCancelled = true
        val player = event.player
        val permission = "betterdamage.skin.$id"
        // Includes inherited, wildcard and operator permissions. Existing owners get no feedback or consumption.
        if (player.hasPermission(permission) || pending.containsKey(player.uniqueId)) return
        val slot = if (hand == EquipmentSlot.OFF_HAND) 40 else player.inventory.heldItemSlot
        val expected = item.clone()
        confirmations.show(player, skin.displayName()) { redeem(player, id, slot, expected) }
    }

    private fun redeem(player: Player, id: String, slot: Int, expected: ItemStack) {
        val skin = DamageSkinManagerImpl.skin(id)?.takeIf { it.name() == id } ?: return
        val permission = "betterdamage.skin.$id"
        if (player.hasPermission(permission) || pending.containsKey(player.uniqueId)) return
        val current = player.inventory.getItem(slot)
        if (current == null || current.amount < 1 || !current.isSimilar(expected) ||
            NexoItems.idFromItem(current) != "${id}_coupon") {
            player.audience().warn("쿠폰이 이동되어 사용을 취소했습니다. 다시 우클릭해주세요.")
            return
        }
        val user = luckPerms.userManager.getUser(player.uniqueId) ?: run {
            player.audience().warn("권한 데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }
        val data = user.data()
        val previous = data.toCollection().filter { it.key == permission && it.contexts.isEmpty && !it.hasExpiry() }
        if (previous.any { it.value }) return
        val node = PermissionNode.builder(permission).value(true).build()
        previous.forEach { data.remove(it) }
        if (!data.add(node).wasSuccessful()) {
            previous.forEach { data.add(it) }
            return
        }
        val saved = try { luckPerms.userManager.saveUser(user) } catch (error: Exception) {
            data.remove(node)
            previous.forEach { data.add(it) }
            error.handle("쿠폰 권한 저장 요청에 실패했습니다.")
            player.audience().warn("쿠폰을 사용하지 못했습니다. 잠시 후 다시 시도해주세요.")
            return
        }
        val transaction = CouponTransaction(saved) {
            data.remove(node)
            previous.forEach { data.add(it) }
            luckPerms.userManager.saveUser(user).whenComplete { _, error ->
                if (error != null) error.handle("쿠폰 사용 취소 권한 저장에 실패했습니다: ${player.uniqueId}, $permission")
            }
        }
        pending[player.uniqueId] = transaction
        transaction.completion.whenComplete { _, _ -> pending.remove(player.uniqueId, transaction) }
        saved.whenComplete { _, error ->
            if (error != null) {
                transaction.cancel()
                error.handle("쿠폰 권한 저장에 실패했습니다: ${player.uniqueId}, $permission")
                if (PLUGIN.isEnabled) player.audience().warn("권한을 저장하지 못해 쿠폰을 사용하지 않았습니다.")
            } else if (!PLUGIN.isEnabled) {
                transaction.cancel()
            } else deliver(player, transaction) {
                val consumed = transaction.consume {
                    val current = player.inventory.getItem(slot)
                    if (!player.isOnline || Bukkit.getPlayer(player.uniqueId) !== player ||
                        DamageSkinManagerImpl.skin(id) == null || current == null || current.amount < 1 ||
                        !current.isSimilar(expected)) false
                    else {
                        if (current.amount == 1) player.inventory.setItem(slot, null)
                        else current.amount -= 1
                        true
                    }
                }
                if (consumed) {
                    player.audience().info("${skin.displayName()} 데미지 스킨을 획득했습니다.")
                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1f, 1f)
                } else player.audience().warn("쿠폰이 이동되어 사용을 취소했습니다. 다시 우클릭해주세요.")
            }
        }
    }

    private fun deliver(player: Player, transaction: CouponTransaction, action: () -> Unit) {
        val retired = Runnable {
            transaction.cancel()
        }
        if (BetterDamage.IS_PAPER) player.scheduler.run(PLUGIN, { action() }, retired)
        else Bukkit.getScheduler().runTask(PLUGIN, Runnable(action))
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        confirmations.cancel(event.player.uniqueId)
        pending[event.player.uniqueId]?.cancel()
    }

    override fun close() {
        confirmations.clear()
        pending.values.forEach { it.cancel() }
        pending.clear()
    }
}
