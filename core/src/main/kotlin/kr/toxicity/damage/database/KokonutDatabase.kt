package kr.toxicity.damage.database

import kr.toxicity.damage.api.database.DamagePlayerDatabase
import org.bukkit.Bukkit
import java.sql.Connection

/** Uses the core's service without bundling its implementation or JDBC driver. */
class KokonutDatabase : DamagePlayerDatabase {
    override fun connect(): DamagePlayerDatabase.Connection {
        val plugin = Bukkit.getPluginManager().getPlugin("Kokonut-Core")
        check(plugin != null && plugin.isEnabled) { "kokonut_damage는 활성화된 Kokonut-Core가 필요합니다." }
        val serviceClass = plugin.javaClass.classLoader.loadClass("kr.gomsv.kokonutCore.api.database.CoreDatabase")
        val service = Bukkit.getServicesManager().load(serviceClass)
            ?: error("Kokonut-Core의 CoreDatabase 서비스가 등록되지 않았습니다.")
        val prefix = serviceClass.getMethod("tablePrefix").invoke(service) as String
        val openConnection = serviceClass.getMethod("openConnection")
        return SqlPlayerConnection(
            { openConnection.invoke(service) as Connection },
            prefix + "betterdamage_"
        )
    }
}
