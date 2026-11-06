package com.fpt.enterpriseapp.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Attendance
import com.fpt.enterpriseapp.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminAttendanceUiState(
    val records: List<Attendance> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminAttendanceViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAttendanceUiState())
    val uiState: StateFlow<AdminAttendanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            attendanceRepo.getAll().collect { list ->
                _uiState.update { it.copy(records = list, isLoading = false) }
            }
        }
    }
}