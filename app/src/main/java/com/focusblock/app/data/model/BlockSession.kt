package com.focusblock.app.data.model

data class BlockSession(
    val startTime: Long,
    val endTime: Long,
    val blockedPackages: Set<String>,
    val isStrictMode: Boolean
) {
    val isActive: Boolean
        get() = System.currentTimeMillis() < endTime

    val remainingMillis: Long
        get() = maxOf(0L, endTime - System.currentTimeMillis())
}
