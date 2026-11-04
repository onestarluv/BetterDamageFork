package kr.toxicity.damage.util

fun <T> T?.ifNull(lazyMessage: () -> String): T & Any = this ?: throw RuntimeException(lazyMessage())

fun String.toFilePath() = replace('\\', '/')
