package kr.toxicity.damage.database

import kr.toxicity.damage.api.database.DamagePlayerDatabase
import kr.toxicity.damage.api.player.DamagePlayerData
import kr.toxicity.damage.api.skin.DamageSkin
import kr.toxicity.damage.manager.DamageSkinManagerImpl
import kr.toxicity.damage.player.DamagePlayerDataImpl
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

internal class SqlPlayerConnection(
    private val openConnection: () -> Connection,
    prefix: String = "",
    private val closeSource: () -> Unit = {},
    private val resolveSkin: (String) -> DamageSkin? = DamageSkinManagerImpl::skin
) : DamagePlayerDatabase.Connection {
    private val selectedTable: String

    init {
        require(prefix.matches(Regex("[A-Za-z0-9_]*"))) { "Invalid database table prefix." }
        selectedTable = "`${prefix}player_selected_skin`"
        openConnection().use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS $selectedTable (uuid CHAR(36) PRIMARY KEY, skin VARCHAR(255) NOT NULL) ENGINE=InnoDB")
            }
        }
    }

    private fun <T> Connection.query(sql: String, uuid: UUID, mapper: ResultSet.() -> T): T =
        prepareStatement(sql).use {
            it.setString(1, uuid.toString())
            it.executeQuery().use(mapper)
        }

    private fun Connection.update(sql: String, setter: PreparedStatement.() -> Unit) {
        prepareStatement(sql).use { it.setter(); it.executeUpdate() }
    }

    override fun load(uuid: UUID): DamagePlayerData = openConnection().use {
        DamagePlayerDataImpl(uuid,
            it.query("SELECT skin FROM $selectedTable WHERE uuid = ?", uuid) {
                if (next()) resolveSkin(getString("skin")) else null
            }
        )
    }

    override fun save(uuid: UUID, data: DamagePlayerData) {
        data as DamagePlayerDataImpl
        synchronized(data) {
            openConnection().use { connection ->
                connection.autoCommit = false
                try {
                    val selected = data.savedSkin()
                    if (selected != null) {
                        connection.update("INSERT INTO $selectedTable (uuid, skin) VALUES (?, ?) ON DUPLICATE KEY UPDATE skin = ?") {
                            setString(1, uuid.toString()); setString(2, selected.name()); setString(3, selected.name())
                        }
                    } else {
                        connection.update("DELETE FROM $selectedTable WHERE uuid = ?") { setString(1, uuid.toString()) }
                    }
                    connection.commit()
                } catch (failure: Throwable) {
                    runCatching { connection.rollback() }.exceptionOrNull()?.let(failure::addSuppressed)
                    throw failure
                }
            }
        }
    }

    override fun close() = closeSource()
}
