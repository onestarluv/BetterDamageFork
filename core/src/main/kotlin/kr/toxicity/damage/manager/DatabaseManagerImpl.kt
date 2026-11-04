package kr.toxicity.damage.manager

import kr.toxicity.damage.api.database.DamagePlayerDatabase
import kr.toxicity.damage.api.manager.DatabaseManager
import kr.toxicity.damage.api.pack.PackAssets
import kr.toxicity.damage.database.KokonutDatabase

object DatabaseManagerImpl : DatabaseManager {
    private var connection: DamagePlayerDatabase.Connection? = null

    override fun connection(): DamagePlayerDatabase.Connection =
        checkNotNull(connection) { "Kokonut DB가 연결되지 않았습니다." }

    override fun start() {
        check(connection == null) { "Kokonut DB가 이미 연결되어 있습니다." }
        connection = KokonutDatabase().connect()
    }

    // Core owns the service; resource reloads do not reconnect to the database.
    override fun reload(assets: PackAssets) {}

    override fun end() {
        connection?.close()
        connection = null
    }
}
