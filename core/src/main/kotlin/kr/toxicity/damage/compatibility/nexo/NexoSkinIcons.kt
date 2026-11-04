package kr.toxicity.damage.compatibility.nexo

import com.nexomc.nexo.api.NexoItems
import org.bukkit.inventory.ItemStack

object NexoSkinIcons {
    fun create(skinId: String): ItemStack? {
        val source = NexoItems.itemFromId("${skinId}_coupon")?.build() ?: return null

        // Start from a fresh stack: coupon tags, mechanics and other metadata stay behind.
        return ItemStack(source.type).apply {
            val sourceMeta = source.itemMeta
            if (sourceMeta?.hasItemModel() == true) {
                itemMeta = itemMeta?.apply { itemModel = sourceMeta.itemModel }
            }
        }
    }
}
