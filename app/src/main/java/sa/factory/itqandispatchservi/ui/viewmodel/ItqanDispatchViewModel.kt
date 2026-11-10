package sa.factory.itqandispatchservi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.itqandispatchservi.domain.usecase.*
import sa.factory.itqandispatchservi.model.ItqanDispatchItem
import sa.factory.itqandispatchservi.model.ItqanDispatchOrder
import sa.factory.itqandispatchservi.model.ItqanDispatchTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ItqanDispatchUiState(
    val catalog: List<ItqanDispatchItem> = emptyList(),
    val filteredCatalog: List<ItqanDispatchItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<ItqanDispatchItem> = emptyList(),
    val telemetry: ItqanDispatchTelemetry? = null,
    val lastOrder: ItqanDispatchOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class ItqanDispatchViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItqanDispatchUiState())
    val uiState: StateFlow<ItqanDispatchUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: ItqanDispatchItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: ItqanDispatchItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
