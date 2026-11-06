package com.fpt.enterpriseapp.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Project
import com.fpt.enterpriseapp.data.model.Task
import com.fpt.enterpriseapp.data.repository.AuthRepository
import com.fpt.enterpriseapp.data.repository.ProjectRepository
import com.fpt.enterpriseapp.data.repository.TaskRepository
import com.fpt.enterpriseapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<Project> = emptyList(),
    val searchQuery: String = "",
    val filtered: List<Project> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepo: ProjectRepository,
    private val taskRepo: TaskRepository,
    private val authRepo: AuthRepository // Thêm AuthRepo để lấy thông tin người dùng
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentEmp = authRepo.getCurrentEmployee()

            // Kết hợp danh sách Dự án và toàn bộ Task
            combine(projectRepo.getAll(), taskRepo.getAll()) { projects, allTasks ->
                var result = projects

                // NẾU LÀ EMPLOYEE: Chỉ hiển thị dự án mà người đó có task
                if (currentEmp?.role == "EMPLOYEE") {
                    // Tìm các projectId mà nhân viên này được giao task
                    val myProjectIds = allTasks
                        .filter { it.assigneeName == currentEmp.fullName }
                        .map { it.projectId }
                        .toSet()

                    result = projects.filter { myProjectIds.contains(it.id) }
                }

                result
            }.collect { filteredProjects ->
                _uiState.update {
                    it.copy(
                        projects = filteredProjects,
                        filtered = applyFilter(filteredProjects, it.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(q: String) = _uiState.update {
        it.copy(searchQuery = q, filtered = applyFilter(it.projects, q))
    }

    fun delete(id: String) = viewModelScope.launch { projectRepo.delete(id) }

    private fun applyFilter(list: List<Project>, q: String) =
        if (q.isBlank()) list
        else list.filter {
            it.name.lowercase().contains(q.lowercase()) ||
                    it.description.lowercase().contains(q.lowercase())
        }
}

// ────────────────────────────────────────
// Add/Edit Project (Giữ nguyên)
// ────────────────────────────────────────
data class ProjectEditUiState(
    val project: Project = Project(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ProjectEditViewModel @Inject constructor(
    private val projectRepo: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectEditUiState())
    val uiState: StateFlow<ProjectEditUiState> = _uiState.asStateFlow()

    fun load(projectId: String?) {
        if (projectId.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val p = projectRepo.getById(projectId)
            _uiState.update { it.copy(project = p ?: Project(), isLoading = false) }
        }
    }

    fun updateField(block: (Project) -> Project) {
        _uiState.update { state ->
            state.copy(project = block(state.project), error = null)
        }
    }

    fun save() {
        val p = _uiState.value.project
        if (p.name.isBlank()) {
            _uiState.update { it.copy(error = "Tên dự án không được trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (p.id.isBlank()) projectRepo.add(p) else projectRepo.update(p)
            result.onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "Lỗi")
                    }
                }
        }
    }
}

// ────────────────────────────────────────
// Task List + Add/Edit (Giữ nguyên)
// ────────────────────────────────────────
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val byStatus: Map<String, List<Task>> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    fun observeProject(projectId: String) {
        viewModelScope.launch {
            taskRepo.getByProject(projectId).collect { list ->
                val grouped = mapOf(
                    Constants.TASK_PENDING     to list.filter { it.status == Constants.TASK_PENDING },
                    Constants.TASK_IN_PROGRESS to list.filter { it.status == Constants.TASK_IN_PROGRESS },
                    Constants.TASK_DONE        to list.filter { it.status == Constants.TASK_DONE }
                )
                _uiState.update { it.copy(tasks = list, byStatus = grouped, isLoading = false) }
            }
        }
    }

    fun updateStatus(taskId: String, newStatus: String, projectId: String) {
        viewModelScope.launch {
            taskRepo.updateStatus(taskId, newStatus)
            projectRepo.updateProgress(projectId)
        }
    }

    fun delete(taskId: String, projectId: String) {
        viewModelScope.launch {
            taskRepo.delete(taskId)
            projectRepo.updateProgress(projectId)
        }
    }
}

data class TaskEditUiState(
    val task: Task = Task(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val projectRepo: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskEditUiState())
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    fun load(taskId: String?, projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (!taskId.isNullOrBlank()) {
                val t = taskRepo.getById(taskId)
                _uiState.update { it.copy(task = t ?: Task(projectId = projectId), isLoading = false) }
            } else {
                val proj = projectRepo.getById(projectId)
                _uiState.update {
                    it.copy(task = Task(projectId = projectId, projectName = proj?.name ?: ""),
                        isLoading = false)
                }
            }
        }
    }

    fun updateField(block: (Task) -> Task) {
        _uiState.update { state ->
            state.copy(task = block(state.task), error = null)
        }
    }

    fun save() {
        val t = _uiState.value.task
        if (t.title.isBlank()) {
            _uiState.update { it.copy(error = "Tiêu đề task không được trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (t.id.isBlank()) taskRepo.add(t) else taskRepo.update(t)
            result.onSuccess {
                projectRepo.updateProgress(t.projectId)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Lỗi") }
            }
        }
    }
}