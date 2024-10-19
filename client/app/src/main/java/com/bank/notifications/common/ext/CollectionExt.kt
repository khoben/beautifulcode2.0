package com.bank.notifications.common.ext

fun <T> List<T>.replaceFirstIf(
    predicate: (T) -> Boolean,
    factory: () -> T
): List<T> {
    val replacementIdx = indexOfFirst(predicate)
    if (replacementIdx == -1) return this
    val destination = ArrayList<T>(this)
    destination[replacementIdx] = factory()
    return destination
}