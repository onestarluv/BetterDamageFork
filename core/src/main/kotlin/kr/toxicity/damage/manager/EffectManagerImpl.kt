package kr.toxicity.damage.manager

import kr.toxicity.damage.api.effect.DamageEffect
import kr.toxicity.damage.api.manager.EffectManager
import kr.toxicity.damage.api.pack.PackAssets
import kr.toxicity.damage.effect.DamageEffectImpl
import kr.toxicity.damage.skin.SkinCatalog
import kr.toxicity.damage.util.handle
import org.jetbrains.annotations.Unmodifiable
import java.util.*

object EffectManagerImpl : EffectManager {

    private val effectMap = hashMapOf<String, DamageEffect>()

    override fun reload(assets: PackAssets) {
        effectMap.clear()
        SkinCatalog.files.keys.filter { ImageManagerImpl.image(it) != null }.forEach { key ->
            runCatching {
                effectMap[key] = DamageEffectImpl(SkinCatalog.effect(key))
                effectMap["$key/critical"] = DamageEffectImpl(SkinCatalog.effect(key, true))
            }.onFailure { e ->
                e.handle("Unable to read this effect: $key")
            }
        }
    }

    override fun effectNames(): @Unmodifiable Set<String> = Collections.unmodifiableSet(effectMap.keys)
    override fun effect(name: String): DamageEffect? = effectMap[name]
}
