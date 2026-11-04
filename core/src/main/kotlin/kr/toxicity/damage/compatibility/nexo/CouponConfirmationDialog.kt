package kr.toxicity.damage.compatibility.nexo

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.type.DialogType
import kr.toxicity.damage.util.PLUGIN
import kr.toxicity.damage.util.onPlayerThread
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

internal class CouponConfirmationDialog {
    private val requests = CouponConfirmationRequests()

    fun show(player: Player, skinName: String, confirm: () -> Unit) {
        val token = requests.open(player.uniqueId)
        val options = ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(2)).build()
        fun answer(actor: net.kyori.adventure.audience.Audience, confirmed: Boolean) {
            if (actor !== player || !PLUGIN.isEnabled) return
            player.onPlayerThread {
                if (Bukkit.getPlayer(player.uniqueId) !== player || !requests.answer(player.uniqueId, token)) return@onPlayerThread
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.5f, 1f)
                if (confirmed) confirm()
            }
        }
        player.showDialog(Dialog.create { builder ->
            builder.empty().base(DialogBase.builder(Component.text("데미지 스킨 쿠폰 사용"))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .body(listOf(DialogBody.plainMessage(Component.text(
                    "$skinName 쿠폰을 사용하시겠습니까?\n확인하면 쿠폰 1개를 소모하고 해당 스킨을 획득합니다.\n취소하면 쿠폰을 사용하지 않습니다."
                )))).build())
                .type(DialogType.confirmation(
                    ActionButton.builder(Component.text("확인", NamedTextColor.GREEN))
                        .action(DialogAction.customClick({ _, audience -> answer(audience, true) }, options)).build(),
                    ActionButton.builder(Component.text("취소", NamedTextColor.RED))
                        .action(DialogAction.customClick({ _, audience -> answer(audience, false) }, options)).build()
                ))
        })
    }

    fun cancel(player: UUID) = requests.cancel(player)
    fun clear() = requests.clear()
}
