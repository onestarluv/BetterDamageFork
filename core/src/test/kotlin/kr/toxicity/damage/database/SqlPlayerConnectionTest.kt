package kr.toxicity.damage.database

import kr.toxicity.damage.api.skin.DamageSkin
import kr.toxicity.damage.player.DamagePlayerDataImpl
import org.h2.jdbcx.JdbcDataSource
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.*

class SqlPlayerConnectionTest {
    private fun skin(name: String) = Proxy.newProxyInstance(
        DamageSkin::class.java.classLoader, arrayOf(DamageSkin::class.java)
    ) { _, method, _ -> if (method.name == "name") name else null } as DamageSkin

    private fun source() = JdbcDataSource().apply {
        setURL("jdbc:h2:mem:${UUID.randomUUID()};MODE=MySQL;DB_CLOSE_DELAY=-1")
    }

    @Test
    fun `permissions control ownership selection and effect eligibility immediately`() {
        val skin = skin("test")
        var permitted = false
        val data = DamagePlayerDataImpl(UUID.randomUUID(), null, { permitted }, { listOf(skin) })
        assertTrue(data.skins().isEmpty())
        assertFalse(data.select(skin))
        permitted = true
        assertEquals(listOf("test"), data.skins().map { it.name() })
        assertTrue(data.select(skin))
        assertEquals("test", data.selectedSkin()?.name())
        permitted = false
        assertNull(data.selectedSkin())
        assertTrue(data.skins().isEmpty())
        assertEquals("test", data.savedSkin()?.name())
        data.clearSelection()
        assertNull(data.savedSkin())
    }

    @Test
    fun `only selection persists and borrowed connections remain usable`() {
        val source = source()
        val skin = skin("test")
        val database = SqlPlayerConnection({ source.connection }, "kokonut_betterdamage_", resolveSkin = { skin })
        val uuid = UUID.randomUUID()
        val data = DamagePlayerDataImpl(uuid, null, { true })
        assertTrue(data.select(skin))
        database.save(uuid, data)
        val loaded = database.load(uuid) as DamagePlayerDataImpl
        assertEquals("test", loaded.savedSkin()?.name())
        data.clearSelection()
        database.save(uuid, data)
        assertNull((database.load(uuid) as DamagePlayerDataImpl).savedSkin())
        data.select(skin)
        database.save(uuid, data)
        database.close()
        source.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM kokonut_betterdamage_player_selected_skin").use {
                    assertTrue(it.next())
                    assertEquals(1, it.getInt(1))
                }
            }
        }
    }

    @Test
    fun `failed save rolls back and preserves previous selection`() {
        val source = source()
        val database = SqlPlayerConnection({ source.connection }, resolveSkin = { skin(it) })
        val uuid = UUID.randomUUID()
        database.save(uuid, DamagePlayerDataImpl(uuid, skin("valid")))
        assertFails { database.save(uuid, DamagePlayerDataImpl(uuid, skin("x".repeat(256)))) }
        assertEquals("valid", (database.load(uuid) as DamagePlayerDataImpl).savedSkin()?.name())
    }
}
