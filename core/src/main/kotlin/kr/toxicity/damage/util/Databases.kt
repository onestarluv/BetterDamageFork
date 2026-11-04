package kr.toxicity.damage.util

import kr.toxicity.damage.manager.DatabaseManagerImpl
import kr.toxicity.damage.player.DamagePlayerDataImpl
import java.util.*

fun UUID.toPlayerData() = DatabaseManagerImpl.connection().load(this) as DamagePlayerDataImpl
