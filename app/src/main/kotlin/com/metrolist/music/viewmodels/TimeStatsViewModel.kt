package com.metrolist.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.DayPlayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimeStatsViewModel
@Inject
constructor(
    val database: MusicDatabase,
) : ViewModel() {

    private val refreshTrigger = flow { emit(Unit) }

    val todayPlayTimeMs: Flow<Long> = refreshTrigger.flatMapLatest {
        val now = LocalDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay()
        database.getTotalPlayTimeInRange(startOfDay, now)
    }.map { it ?: 0L }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val thisWeekPlayTimes: Flow<List<DayPlayTime>> = refreshTrigger.flatMapLatest {
        val now = LocalDateTime.now()
        val monday = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()
        database.getPlayTimeByDay(monday, now)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lastWeekPlayTimes: Flow<List<DayPlayTime>> = refreshTrigger.flatMapLatest {
        val today = LocalDate.now()
        val lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(7)
        val lastSunday = lastMonday.plusDays(6).atTime(LocalTime.MAX)
        database.getPlayTimeByDay(lastMonday.atStartOfDay(), lastSunday)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
