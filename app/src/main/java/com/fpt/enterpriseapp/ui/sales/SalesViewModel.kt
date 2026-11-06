package com.fpt.enterpriseapp.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fpt.enterpriseapp.data.model.Contract
import com.fpt.enterpriseapp.data.model.Customer
import com.fpt.enterpriseapp.data.model.Transaction
import com.fpt.enterpriseapp.data.repository.ContractRepository
import com.fpt.enterpriseapp.data.repository.CustomerRepository
import com.fpt.enterpriseapp.data.repository.TransactionRepository
import com.fpt.enterpriseapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ────────────────────────────────────────
// Customer
// ────────────────────────────────────────
data class CustomerListUiState(
    val customers: List<Customer> = emptyList(),
    val filtered: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repo: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAll().collect { list ->
                _uiState.update {
                    it.copy(
                        customers = list,
                        filtered = applyFilter(list, it.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchChange(q: String) = _uiState.update {
        it.copy(searchQuery = q, filtered = applyFilter(it.customers, q))
    }

    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }

    private fun applyFilter(list: List<Customer>, q: String) =
        if (q.isBlank()) list
        else list.filter {
            it.name.lowercase().contains(q.lowercase()) ||
                    it.company.lowercase().contains(q.lowercase()) ||
                    it.email.lowercase().contains(q.lowercase())
        }
}

data class CustomerEditUiState(
    val customer: Customer = Customer(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class CustomerEditViewModel @Inject constructor(
    private val repo: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerEditUiState())
    val uiState: StateFlow<CustomerEditUiState> = _uiState.asStateFlow()

    fun load(id: String?) {
        if (id.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val c = repo.getById(id)
            _uiState.update { it.copy(customer = c ?: Customer(), isLoading = false) }
        }
    }

    fun updateField(block: (Customer) -> Customer) =
        _uiState.update { it.copy(customer = block(it.customer), error = null) }

    fun save() {
        val c = _uiState.value.customer
        if (c.name.isBlank()) {
            _uiState.update { it.copy(error = "Tên khách hàng không được trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (c.id.isBlank()) repo.add(c) else repo.update(c)
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
// Contract
// ────────────────────────────────────────
data class ContractListUiState(
    val contracts: List<Contract> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ContractViewModel @Inject constructor(
    private val contractRepo: ContractRepository,
    private val customerRepo: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContractListUiState())
    val uiState: StateFlow<ContractListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contractRepo.getAll().collect { list ->
                _uiState.update { it.copy(contracts = list, isLoading = false) }
            }
        }
        viewModelScope.launch {
            customerRepo.getAll().collect { list ->
                _uiState.update { it.copy(customers = list) }
            }
        }
    }

    fun delete(id: String) = viewModelScope.launch { contractRepo.delete(id) }
}

data class ContractEditUiState(
    val contract: Contract = Contract(),
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ContractEditViewModel @Inject constructor(
    private val repo: ContractRepository,
    private val customerRepo: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContractEditUiState())
    val uiState: StateFlow<ContractEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            customerRepo.getAll().collect { list ->
                _uiState.update { it.copy(customers = list) }
            }
        }
    }

    fun load(id: String?) {
        if (id.isNullOrBlank()) {
            // Generate mã hợp đồng
            _uiState.update {
                it.copy(contract = Contract(code = "HD${System.currentTimeMillis() % 100000}"))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val c = repo.getById(id)
            _uiState.update { it.copy(contract = c ?: Contract(), isLoading = false) }
        }
    }

    fun updateField(block: (Contract) -> Contract) =
        _uiState.update { it.copy(contract = block(it.contract), error = null) }

    fun save() {
        val c = _uiState.value.contract
        if (c.customerId.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng chọn khách hàng") }
            return
        }
        if (c.value <= 0) {
            _uiState.update { it.copy(error = "Giá trị hợp đồng phải > 0") }
            return
        }
        val customer = _uiState.value.customers.find { it.id == c.customerId }
        val toSave = c.copy(customerName = customer?.name ?: "")
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = if (toSave.id.isBlank()) repo.add(toSave) else repo.update(toSave)
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
// Transaction
// ────────────────────────────────────────
data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val contracts: List<Contract> = emptyList(),
    val totalRevenue: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val txRepo: TransactionRepository,
    private val contractRepo: ContractRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            txRepo.getAll().collect { list ->
                _uiState.update {
                    it.copy(
                        transactions = list,
                        totalRevenue = list.filter { t -> t.status == Constants.TX_SUCCESS }
                            .sumOf { it.amount },
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            contractRepo.getAll().collect { list ->
                _uiState.update { it.copy(contracts = list) }
            }
        }
    }

    fun delete(id: String) = viewModelScope.launch { txRepo.delete(id) }
}

data class TransactionEditUiState(
    val transaction: Transaction = Transaction(),
    val contracts: List<Contract> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    private val repo: TransactionRepository,
    private val contractRepo: ContractRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditUiState())
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contractRepo.getAll().collect { list ->
                _uiState.update { it.copy(contracts = list) }
            }
        }
    }

    fun load(id: String?) {
        if (id.isNullOrBlank()) return
        // Không hỗ trợ edit transaction, chỉ add + delete
    }

    fun updateField(block: (Transaction) -> Transaction) =
        _uiState.update { it.copy(transaction = block(it.transaction), error = null) }

    fun save() {
        val t = _uiState.value.transaction
        if (t.contractId.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng chọn hợp đồng") }
            return
        }
        if (t.amount <= 0) {
            _uiState.update { it.copy(error = "Số tiền phải > 0") }
            return
        }
        val contract = _uiState.value.contracts.find { it.id == t.contractId }
        val toSave = t.copy(
            contractCode = contract?.code ?: "",
            customerName = contract?.customerName ?: ""
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            repo.add(toSave)
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "Lỗi")
                    }
                }
        }
    }
}