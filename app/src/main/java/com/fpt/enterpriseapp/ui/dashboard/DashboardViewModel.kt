package com.fpt.enterpriseapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.data.model.Task
import com.fpt.enterpriseapp.data.repository.AuthRepository
import com.fpt.enterpriseapp.data.repository.EmployeeRepository
import com.fpt.enterpriseapp.data.repository.ProjectRepository
import com.fpt.enterpriseapp.data.repository.TaskRepository
import com.fpt.enterpriseapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val employeeCount: Int = 0,
    val projectCount: Int = 0,
    val revenue: Double = 0.0,
    val recentTasks: List<Task> = emptyList(),
    val currentEmployee: Employee? = null,
    val monthlyRevenue: List<Float> = List(12) { 0f },
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val employeeRepo: EmployeeRepository,
    private val projectRepo: ProjectRepository,
    private val taskRepo: TaskRepository,
    private val txRepo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val currentEmp = authRepo.getCurrentEmployee()
            _uiState.update { it.copy(currentEmployee = currentEmp) }

            // 1. Lấy số lượng nhân sự (Chỉ Admin/Manager cần quan tâm)
            if (currentEmp?.role == "ADMIN" || currentEmp?.role == "MANAGER") {
                employeeRepo.getAll().collect { list ->
                    _uiState.update { it.copy(employeeCount = list.size, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        // 2. Đếm dự án (Lọc theo nhân viên nếu là EMPLOYEE)
        viewModelScope.launch {
            val currentEmp = authRepo.getCurrentEmployee()

            combine(projectRepo.getAll(), taskRepo.getAll()) { projects, allTasks ->
                if (currentEmp?.role == "EMPLOYEE") {
                    val myProjectIds = allTasks
                        .filter { it.assigneeName == currentEmp.fullName }
                        .map { it.projectId }
                        .toSet()
                    projects.filter { myProjectIds.contains(it.id) }.size
                } else {
                    projects.size
                }
            }.collect { count ->
                _uiState.update { it.copy(projectCount = count) }
            }
        }

        // 3. Lấy task gần đây (Lọc theo nhân viên nếu là EMPLOYEE)
        viewModelScope.launch {
            val currentEmp = authRepo.getCurrentEmployee()

            taskRepo.getAll().collect { list ->
                val myTasks = if (currentEmp?.role == "EMPLOYEE") {
                    list.filter { it.assigneeName == currentEmp.fullName }
                } else {
                    list
                }
                _uiState.update {
                    it.copy(recentTasks = myTasks.sortedByDescending { t -> t.createdDate }.take(5))
                }
            }
        }

        // 4. Doanh thu (Chỉ Admin/Manager mới tính)
        viewModelScope.launch {
            val currentEmp = authRepo.getCurrentEmployee()

            if (currentEmp?.role == "ADMIN" || currentEmp?.role == "MANAGER") {
                txRepo.getAll().collect { list ->
                    val total = list.sumOf { it.amount }
                    val monthRev = mutableListOf<Float>()
                    val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    for (m in 0..11) {
                        monthRev.add(txRepo.getRevenueByMonth(year, m).toFloat())
                    }
                    _uiState.update {
                        it.copy(revenue = total, monthlyRevenue = monthRev)
                    }
                }
            }
        }
    }
}