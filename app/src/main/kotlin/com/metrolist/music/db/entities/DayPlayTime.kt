package com.metrolist.music.db.entities

import androidx.compose.runtime.Immutable

@Immutable
data class DayPlayTime(
    val day: String,
    val playTime: Long,
)
