package io.github.natanfudge.hardcraft.utils

fun <K, V> Map<K, V>.asMutableMap() = if (this is MutableMap<K, V>) this else toMutableMap()