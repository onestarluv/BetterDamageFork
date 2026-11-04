package kr.toxicity.damage.skin

import kr.toxicity.damage.manager.TriggerManagerImpl
import kr.toxicity.damage.util.DATA_FOLDER
import kr.toxicity.damage.util.PLUGIN
import kr.toxicity.damage.util.warn
import org.bukkit.configuration.MemoryConfiguration
import java.io.File
import java.io.InputStream

/** Egoverse skin defaults live here. Each PNG registers one complete skin. */
object SkinCatalog {
    val bundledSkins: Map<String, String> = mapOf(
        "breeze" to "브리즈",
        "default" to "기본",
        "halloween" to "할로윈",
        "heart" to "하트",
        "korean" to "한글",
        "pumpkin" to "호박",
        "sculk" to "스컬크",
        "soul" to "영혼",
        "tiger" to "호랑이"
    )

    private val retiredIds = setOf("default_skin", "peach", "pixel_white", "emerald", "starlight",
        "korean_numbers", "pink_hearts", "golden", "ice_flame", "teal_neon")

    fun displayName(id: String): String = bundledSkins[id] ?: id

    var files: Map<String, File> = emptyMap()
        private set

    fun reload() {
        val directory = File(DATA_FOLDER, "assets/skins")
        installBundled(directory, PLUGIN::getResource)
        files = discover(directory) { warn(it) }
    }

    /** Also installs new built-ins on existing servers, preserving customized PNGs. */
    internal fun installBundled(directory: File, resource: (String) -> InputStream?) {
        check(directory.isDirectory || directory.mkdirs()) { "스킨 이미지 폴더를 만들 수 없습니다: $directory" }
        // Remove only the superseded bundled IDs from earlier versions.
        retiredIds.forEach { id ->
            val retired = File(directory, "$id.png")
            check(!retired.exists() || (retired.isFile && retired.delete())) { "이전 스킨 파일을 삭제할 수 없습니다: $retired" }
        }
        bundledSkins.keys.forEach { id ->
            val target = File(directory, "$id.png")
            if (!target.exists()) {
                checkNotNull(resource("default/assets/skins/$id.png")) { "기본 스킨 리소스가 없습니다: $id" }
                    .use { input -> java.nio.file.Files.copy(input, target.toPath()) }
            }
        }
    }

    internal fun discover(directory: File, invalid: (String) -> Unit): Map<String, File> =
        directory.listFiles().orEmpty().filter { it.isFile && it.extension.equals("png", true) }
            .sortedBy { it.name }.mapNotNull { file ->
                val id = file.nameWithoutExtension
                if (!id.matches(Regex("[a-z0-9_-]+"))) {
                    invalid("스킨 파일명은 영문 소문자, 숫자, 밑줄, 하이픈만 사용할 수 있습니다: ${file.name}")
                    null
                } else id to file
            }.also { entries ->
                require(entries.map { it.first }.distinct().size == entries.size) { "중복된 스킨 ID가 있습니다." }
            }.toMap()

    fun image(file: File) = MemoryConfiguration().apply {
        set("file", "skins/${file.name}")
        set("space", -2)
    }

    fun effect(id: String, critical: Boolean = false) = MemoryConfiguration().apply {
        set("image", id)
        set("duration", 20)
        set("interval", 2)
        set("show-global-player", critical)
        // Preserve the original PNG colors.
        set("transformation.position.x", "0")
        set("transformation.position.y", "0.7 + 0.5 * (t / 20)")
        set("transformation.position.z", "0")
        set("transformation.rotation.x", "60(r - 0.5)")
        set("transformation.rotation.y", "60(r - 0.5)")
        set("transformation.rotation.z", "0")
        listOf("x", "y", "z").forEach { set("transformation.scale.$it", "1") }
    }

    fun skin(id: String) = MemoryConfiguration().apply {
        set("fallback", id)
        createSection("skins")
        listOf("bukkit_damage", "mythiclib_damage", "mythiclib_crit_damage").forEach { trigger ->
            if (TriggerManagerImpl.type(trigger) != null)
                set("skins.$trigger", if (trigger == "mythiclib_crit_damage") "$id/critical" else id)
        }
    }
}
