package com.fpt.enterpriseapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.data.repository.AttendanceRepository
import com.fpt.enterpriseapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val employee: Employee? = null,
    val totalCheckIns: Int = 0,
    val totalHours: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val emp = authRepo.getCurrentEmployee()
            _uiState.update { it.copy(employee = emp, isLoading = false) }
            if (emp != null) {
                attendanceRepo.getByEmployee(emp.id).collect { list ->
                    _uiState.update {
                        it.copy(
                            totalCheckIns = list.size,
                            totalHours = list.sumOf { a -> a.totalHours }
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        authRepo.logout()
    }
}