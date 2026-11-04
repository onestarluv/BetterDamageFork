package kr.toxicity.damage.gui

import kr.toxicity.damage.compatibility.nexo.NexoSkinIcons
import kr.toxicity.damage.manager.DamageSkinManagerImpl
import kr.toxicity.damage.manager.PlayerManagerImpl
import kr.toxicity.damage.util.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

object SkinMenu : Listener {
    private class Page(val owner: java.util.UUID, var index: Int) : InventoryHolder {
        lateinit var contents: Inventory
        var skins: List<String> = emptyList()
        override fun getInventory(): Inventory = contents
    }

    fun register() {
        Bukkit.getPluginManager().registerEvents(this, PLUGIN)
        listOf("betterdamage.gui").forEach {
            if (Bukkit.getPluginManager().getPermission(it) == null)
                Bukkit.getPluginManager().addPermission(Permission(it, PermissionDefault.TRUE))
        }
    }

    fun open(player: Player, requestedPage: Int = 0) {
        val page = Page(player.uniqueId, requestedPage)
        page.contents = Bukkit.createInventory(page, 54, Component.text("데미지 스킨 목록"))
        if (render(player, page)) player.openInventory(page.contents)
    }

    private fun render(player: Player, page: Page): Boolean {
        if (!player.hasPermission("betterdamage.gui")) {
            player.audience().warn("스킨 장착 창을 열 권한이 없습니다.")
            return false
        }
        val data = PlayerManagerImpl.player(player.uniqueId)?.data() ?: run {
            player.audience().warn("플레이어 데이터를 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            return false
        }
        val skins = data.skins().sortedBy { it.displayName() }
        val pages = ((skins.size + 44) / 45).coerceAtLeast(1)
        page.index = page.index.coerceIn(0, pages - 1)
        val visible = skins.drop(page.index * 45).take(45)
        page.skins = visible.map { it.name() }
        page.contents.clear()
        val selected = data.selectedSkin()?.name()
        visible.forEachIndexed { slot, skin ->
            val equipped = selected == skin.name()
            val icon = if (Bukkit.getPluginManager().isPluginEnabled("Nexo"))
                NexoSkinIcons.create(skin.name()) else null
            page.contents.setItem(slot, item(icon ?: ItemStack(if (equipped) Material.NAME_TAG else Material.PAPER),
                Component.text(skin.displayName()), Component.empty(),
                Component.text(if (equipped) "✔ 장착 중" else "클릭하여 장착", NamedTextColor.GREEN)))
        }
        if (skins.isEmpty()) page.contents.setItem(22, item(Material.PAPER,
            Component.text("보유한 데미지 스킨이 없습니다.", NamedTextColor.GRAY)))
        for (slot in 45..53) page.contents.setItem(slot, item(Material.GRAY_STAINED_GLASS_PANE, Component.text(" ")))
        page.contents.setItem(45, item(Material.ARROW, Component.text("이전 페이지")))
        page.contents.setItem(49, item(Material.BOOK, Component.text("${page.index + 1} / $pages"),
            Component.text("보유한 데미지 스킨: ${skins.size}개", NamedTextColor.GRAY)))
        page.contents.setItem(50, item(Material.BARRIER, Component.text("데미지 스킨 초기화"),
            Component.text("장착한 데미지 스킨을 해제합니다.", NamedTextColor.GRAY)))
        page.contents.setItem(53, item(Material.ARROW, Component.text("다음 페이지")))
        return true
    }

    private fun item(material: Material, title: Component, vararg lore: Component) = item(ItemStack(material), title, *lore)

    private fun item(stack: ItemStack, title: Component, vararg lore: Component) = stack.apply {
        itemMeta = itemMeta?.apply {
            displayName(title)
            lore(lore.toList())
            CoreMessages.applyItemText(this)
        }
    }

    @EventHandler
    fun click(event: InventoryClickEvent) {
        val page = event.view.topInventory.holder as? Page ?: return
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        val slot = event.rawSlot
        if (player.uniqueId != page.owner || !event.isLeftClick || event.isShiftClick) return
        if (slot !in page.skins.indices && slot !in listOf(45, 50, 53)) return
        player.onPlayerThread {
            if (player.openInventory.topInventory.holder !== page) return@onPlayerThread
            if (!player.hasPermission("betterdamage.gui")) {
                player.closeInventory()
                return@onPlayerThread
            }
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f)
            val data = PlayerManagerImpl.player(player.uniqueId)?.data() ?: return@onPlayerThread
            when (slot) {
                45 -> page.index--
                53 -> page.index++
                50 -> {
                    data.clearSelection()
                    player.audience().info("데미지 스킨을 해제했습니다.")
                }
                in page.skins.indices -> {
                    val skin = DamageSkinManagerImpl.skin(page.skins[slot])
                    if (skin != null && data.selectedSkin()?.name() == skin.name()) return@onPlayerThread
                    if (skin != null && data.select(skin))
                        player.audience().info("${skin.displayName()} 데미지 스킨을 장착했습니다.")
                    else player.audience().warn("해당 스킨을 장착할 수 없습니다. 권한과 스킨 설정을 확인해주세요.")
                }
            }
            if (!render(player, page)) player.closeInventory()
        }
    }

    @EventHandler
    fun drag(event: InventoryDragEvent) {
        if (event.view.topInventory.holder is Page && event.rawSlots.any { it < 54 }) event.isCancelled = true
    }
}
