package kr.toxicity.damage.skin

import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.*

class SkinCatalogTest {
    @TempDir
    lateinit var directory: File

    @Test
    fun `bundled skins have korean display names and valid transparent digit strips`() {
        assertEquals(setOf("breeze", "default", "halloween", "heart", "korean", "pumpkin", "sculk", "soul", "tiger"),
            SkinCatalog.bundledSkins.keys)
        SkinCatalog.bundledSkins.forEach { (id, name) ->
            assertTrue(name.any { it in '가'..'힣' }, id)
            val path = "/default/assets/skins/$id.png"
            val image = javaClass.getResourceAsStream(path)?.use(ImageIO::read)
            assertNotNull(image, path)
            assertEquals(0, image.width % 10, id)
            assertTrue(image.height > 0, id)
            assertTrue(image.colorModel.hasAlpha(), id)
        }
    }

    @Test
    fun `new bundled skins are installed without replacing existing customized files`() {
        val existing = File(directory, "heart.png").apply { writeText("custom") }
        val retired = File(directory, "peach.png").apply { writeText("retired") }
        SkinCatalog.installBundled(directory) { javaClass.getResourceAsStream("/$it") }
        assertEquals("custom", existing.readText())
        assertFalse(retired.exists())
        assertEquals(SkinCatalog.bundledSkins.keys, SkinCatalog.discover(directory) { fail(it) }.keys)
        SkinCatalog.bundledSkins.keys.filter { it != "heart" }.forEach { id ->
            val expected = javaClass.getResourceAsStream("/default/assets/skins/$id.png")!!.use { it.readBytes() }
            assertContentEquals(expected, File(directory, "$id.png").readBytes())
        }
    }

    @Test
    fun `only pngs with valid resource IDs are registered in stable order`() {
        listOf("zebra.png", "fire.PNG", "Bad Name.png", "ignore.yml").forEach {
            File(directory, it).writeText("")
        }
        File(directory, "nested.png").mkdir()
        val errors = mutableListOf<String>()
        val found = SkinCatalog.discover(directory, errors::add)
        assertEquals(listOf("fire", "zebra"), found.keys.toList())
        assertEquals(1, errors.size)
    }

    @Test
    fun `effect defaults preserve png color and use fixed scale for normal and critical damage`() {
        val normal = SkinCatalog.effect("fire")
        val critical = SkinCatalog.effect("fire", true)
        assertEquals("fire", normal.getString("image"))
        assertFalse(normal.contains("color"))
        assertFalse(critical.contains("color"))
        assertEquals(20, normal.getInt("duration"))
        assertEquals(2, normal.getInt("interval"))
        assertFalse(normal.getBoolean("show-global-player"))
        assertTrue(critical.getBoolean("show-global-player"))
        listOf(normal, critical).forEach { effect ->
            assertEquals("0", effect.getString("transformation.position.x"))
            assertEquals("0.7 + 0.5 * (t / 20)", effect.getString("transformation.position.y"))
            assertEquals("0", effect.getString("transformation.position.z"))
            assertEquals("60(r - 0.5)", effect.getString("transformation.rotation.x"))
            assertEquals("60(r - 0.5)", effect.getString("transformation.rotation.y"))
            assertEquals("0", effect.getString("transformation.rotation.z"))
            listOf("x", "y", "z").forEach { axis ->
                assertEquals("1", effect.getString("transformation.scale.$axis"))
            }
        }
    }
}
