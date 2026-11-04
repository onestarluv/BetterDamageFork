package kr.toxicity.damage.manager

import kr.toxicity.damage.api.manager.DamageSkinManager
import kr.toxicity.damage.api.pack.PackAssets
import kr.toxicity.damage.api.skin.DamageSkin
import kr.toxicity.damage.skin.DamageSkinImpl
import kr.toxicity.damage.skin.SkinCatalog
import kr.toxicity.damage.util.handle
import org.jetbrains.annotations.Unmodifiable
import java.util.*

object DamageSkinManagerImpl : DamageSkinManager {

    private val skinMap = hashMapOf<String, DamageSkin>()

    override fun skin(name: String): DamageSkin? = skinMap[name]
        ?: skinMap.values.singleOrNull { it.displayName() == name }

    fun displayNames(): List<String> = skinMap.values.map { it.displayName() }.sorted()
    override fun allNames(): @Unmodifiable Set<String> = Collections.unmodifiableSet(skinMap.keys)

    override fun reload(assets: PackAssets) {
        skinMap.clear()
        SkinCatalog.files.keys.filter { EffectManagerImpl.effect(it) != null }.forEach { key ->
            runCatching {
                skinMap[key] = DamageSkinImpl(key, SkinCatalog.skin(key))
            }.onFailure {
                it.handle("Unable to load this damage skin: $key")
            }
        }
    }
}
