package com.intervaltrack.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intervaltrack.app.data.model.AppSession
import com.intervaltrack.app.data.model.AppSummary
import com.intervaltrack.app.data.usage.UsageStatsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class UsageUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val selectedDate: Calendar = Calendar.getInstance(),
    val allSessions: List<AppSession> = emptyList(),
    val filteredSessions: List<AppSession> = emptyList(),
    val appSummaries: List<AppSummary> = emptyList(),
    val selectedPackage: String? = null,
    val searchQuery: String = "",
    val activeSession: AppSession? = null,
    val dayStartMs: Long = 0L,
    val dayEndMs: Long = 0L
)

class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val usageHelper = UsageStatsHelper(application.applicationContext)

    private val _uiState = MutableStateFlow(UsageUiState())
    val uiState: StateFlow<UsageUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoad()
        startPeriodicRefresh()
    }

    fun checkPermissionAndLoad() {
        val hasPerm = usageHelper.hasUsagePermission()
        _uiState.value = _uiState.value.copy(hasPermission = hasPerm)
        loadDayData(_uiState.value.selectedDate)
    }

    fun loadDayData(calendar: Calendar) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedDate = calendar)

            val (startOfDay, endOfDay) = getDayBounds(calendar)

            val sessions = withContext(Dispatchers.IO) {
                usageHelper.queryAppSessions(startOfDay, endOfDay)
            }

            val summaries = withContext(Dispatchers.IO) {
                usageHelper.computeAppSummaries(sessions)
            }

            val active = sessions.firstOrNull { it.isActiveNow }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allSessions = sessions,
                appSummaries = summaries,
                activeSession = active,
                dayStartMs = startOfDay,
                dayEndMs = endOfDay
            )
            applyFilters()
        }
    }

    fun selectPackage(pkg: String?) {
        _uiState.value = _uiState.value.copy(selectedPackage = pkg)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun goToPreviousDay() {
        val cal = (_uiState.value.selectedDate.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        loadDayData(cal)
    }

    fun goToNextDay() {
        val cal = (_uiState.value.selectedDate.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        if (!isFutureDate(cal)) {
            loadDayData(cal)
        }
    }

    private fun applyFilters() {
        val current = _uiState.value
        var list = current.allSessions

        if (!current.selectedPackage.isNullOrEmpty()) {
            list = list.filter { it.packageName == current.selectedPackage }
        }

        if (current.searchQuery.isNotBlank()) {
            val q = current.searchQuery.trim().lowercase()
            list = list.filter { it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }

        _uiState.value = current.copy(filteredSessions = list)
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(10_000) // refresh active session status every 10 seconds if today
                if (isToday(_uiState.value.selectedDate)) {
                    val (startOfDay, endOfDay) = getDayBounds(_uiState.value.selectedDate)
                    val sessions = withContext(Dispatchers.IO) {
                        usageHelper.queryAppSessions(startOfDay, endOfDay)
                    }
                    val summaries = withContext(Dispatchers.IO) {
                        usageHelper.computeAppSummaries(sessions)
                    }
                    val active = sessions.firstOrNull { it.isActiveNow }
                    _uiState.value = _uiState.value.copy(
                        allSessions = sessions,
                        appSummaries = summaries,
                        activeSession = active
                    )
                    applyFilters()
                }
            }
        }
    }

    private fun getDayBounds(calendar: Calendar): Pair<Long, Long> {
        val start = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return Pair(start, end)
    }

    fun isToday(calendar: Calendar): Boolean {
        val now = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    }

    private fun isFutureDate(calendar: Calendar): Boolean {
        val now = Calendar.getInstance()
        return calendar.after(now) && !isToday(calendar)
    }
}
