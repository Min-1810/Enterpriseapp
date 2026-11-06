package com.fpt.enterpriseapp.ui.employee

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Attendance
import com.fpt.enterpriseapp.data.model.Employee
import com.fpt.enterpriseapp.data.repository.AttendanceRepository
import com.fpt.enterpriseapp.data.repository.AuthRepository
import com.fpt.enterpriseapp.data.repository.EmployeeRepository
import com.fpt.enterpriseapp.utils.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class EmployeeListUiState(
    val employees: List<Employee> = emptyList(),
    val filtered: List<Employee> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedRole: String? = null,
    val selectedDepartment: String? = null,
    val currentRole: String = "EMPLOYEE",
    val currentDepartment: String = ""
)

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepo: EmployeeRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeListUiState())
    val uiState: StateFlow<EmployeeListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. CHỜ LẤY THÔNG TIN USER TRƯỚC
            val currentEmp = authRepo.getCurrentEmployee()
            val role = currentEmp?.role ?: "EMPLOYEE"
            val dept = currentEmp?.department ?: ""

            _uiState.update {
                it.copy(currentRole = role, currentDepartment = dept)
            }

            // 2. SAU KHI CÓ ROLE CHUẨN RỒI MỚI ĐI LẤY DANH SÁCH VÀ LỌC
            employeeRepo.getAll().collect { list ->
                val sorted = list.sortedBy { it.fullName.lowercase() }
                _uiState.update { state ->
                    state.copy(
                        employees = sorted,
                        filtered = applyFilterAndSort(
                            list = sorted,
                            q = state.searchQuery,
                            role = state.selectedRole,
                            dept = state.selectedDepartment,
                            currentRole = role, // Lấy role chuẩn vừa lấy được
                            currentDept = dept
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(q: String) = _uiState.update {
        it.copy(
            searchQuery = q,
            filtered = applyFilterAndSort(
                it.employees, q, it.selectedRole, it.selectedDepartment, it.currentRole, it.currentDepartment
            )
        )
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch { employeeRepo.delete(id) }
    }

    fun isAdmin(): Boolean = uiState.value.currentRole == "ADMIN"

    fun onRoleFilterChange(role: String?) = _uiState.update {
        it.copy(
            selectedRole = role,
            filtered = applyFilterAndSort(
                it.employees, it.searchQuery, role, it.selectedDepartment, it.currentRole, it.currentDepartment
            )
        )
    }

    fun onDepartmentFilterChange(dept: String?) = _uiState.update {
        it.copy(
            selectedDepartment = dept,
            filtered = applyFilterAndSort(
                it.employees, it.searchQuery, it.selectedRole, dept, it.currentRole, it.currentDepartment
            )
        )
    }

    private fun applyFilterAndSort(
        list: List<Employee>,
        q: String,
        role: String?,
        dept: String?,
        currentRole: String,
        currentDept: String
    ): List<Employee> {
        var result = list

        if (currentRole != "ADMIN" && currentRole != "MANAGER") {
            result = result.filter { it.department == currentDept }
        }

        if (q.isNotBlank()) {
            val s = q.lowercase()
            result = result.filter {
                it.fullName.lowercase().contains(s) ||
                        it.email.lowercase().contains(s) ||
                        it.department.lowercase().contains(s)
            }
        }
        if (role != null) {
            result = result.filter { it.role == role }
        }
        if (dept != null) {
            result = result.filter { it.department == dept }
        }
        return result
    }

    fun exportToCsv(context: Context) {
        val currentEmployees = _uiState.value.employees
        val headers = listOf("ID", "Họ Tên", "Email", "Phòng Ban", "Vai Trò", "SĐT")

        val rows = currentEmployees.map { employee ->
            listOf(
                employee.id,
                employee.fullName,
                employee.email,
                employee.department,
                employee.role,
                employee.phone
            )
        }
        FileUtil.exportCsv(context, "DanhSachNhanVien.csv", headers, rows)
    }

    fun bulkCreateAccounts(
        inputText: String,
        onResult: (successCount: Int, errorCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            var success = 0
            var error = 0
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

            val lines = inputText.split("\n").filter { it.isNotBlank() }

            for (line in lines) {
                val parts = line.split(",")
                if (parts.size >= 3) {
                    val email = parts[0].trim()
                    val name = parts[1].trim()
                    val dept = parts[2].trim()

                    try {
                        val result = auth.createUserWithEmailAndPassword(email, "123456").await()
                        val uid = result.user?.uid
                        if (uid != null) {
                            val emp = Employee(
                                id = uid, fullName = name, email = email,
                                department = dept, role = "EMPLOYEE", position = "Staff"
                            )
                            employeeRepo.add(emp)
                            success++
                        }
                        delay(500)
                    } catch (e: Exception) {
                        error++
                    }
                } else {
                    error++
                }
            }
            onResult(success, error)
        }
    }
}

// ─────────────────────────────────────────────
// Các ViewModel khác giữ nguyên
// ─────────────────────────────────────────────
data class EmployeeEditUiState(
    val employee: Employee = Employee(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class EmployeeEditViewModel @Inject constructor(
    private val employeeRepo: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeEditUiState())
    val uiState: StateFlow<EmployeeEditUiState> = _uiState.asStateFlow()

    fun load(employeeId: String?) {
        if (employeeId.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val emp = employeeRepo.getById(employeeId)
            _uiState.update {
                it.copy(employee = emp ?: Employee(), isLoading = false)
            }
        }
    }

    fun updateField(block: (Employee) -> Employee) {
        _uiState.update { it.copy(employee = block(it.employee), error = null) }
    }

    fun save() {
        val emp = _uiState.value.employee
        if (emp.fullName.isBlank() || emp.email.isBlank()) {
            _uiState.update { it.copy(error = "Họ tên và email không được trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (emp.id.isBlank()) employeeRepo.add(emp)
            else employeeRepo.update(emp)
            result.onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "Lỗi khi lưu")
                    }
                }
        }
    }
}

data class AttendanceUiState(
    val currentEmployee: Employee? = null,
    val today: Attendance? = null,
    val history: List<Attendance> = emptyList(),
    val isProcessing: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val emp = authRepo.getCurrentEmployee()
            _uiState.update { it.copy(currentEmployee = emp) }
            if (emp != null) {
                _uiState.update { it.copy(today = attendanceRepo.getToday(emp.id)) }
                attendanceRepo.getByEmployee(emp.id).collect { list ->
                    _uiState.update { it.copy(history = list) }
                }
            }
        }
    }

    fun checkIn() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, message = null) }
            attendanceRepo.checkIn(emp.id, emp.fullName)
                .onSuccess {
                    _uiState.update {
                        it.copy(isProcessing = false, message = "Check-in thành công!")
                    }
                    _uiState.update { it.copy(today = attendanceRepo.getToday(emp.id)) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, message = e.message ?: "Lỗi")
                    }
                }
        }
    }

    fun checkOut() {
        val emp = _uiState.value.currentEmployee ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, message = null) }
            attendanceRepo.checkOut(emp.id)
                .onSuccess { hours ->
                    _uiState.update {
                        it.copy(isProcessing = false,
                            message = "Check-out thành công! Giờ làm: %.1f h".format(hours))
                    }
                    _uiState.update { it.copy(today = attendanceRepo.getToday(emp.id)) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, message = e.message ?: "Lỗi")
                    }
                }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}