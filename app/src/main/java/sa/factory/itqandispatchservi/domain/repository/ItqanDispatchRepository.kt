package sa.factory.itqandispatchservi.domain.repository

import sa.factory.itqandispatchservi.model.ItqanDispatchItem
import sa.factory.itqandispatchservi.model.ItqanDispatchOrder
import sa.factory.itqandispatchservi.model.ItqanDispatchTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ItqanDispatchRepository {
    val catalogFlow: Flow<List<ItqanDispatchItem>>
    val activeCart: StateFlow<List<ItqanDispatchItem>>
    val telemetryFlow: Flow<ItqanDispatchTelemetry>
    
    fun addToCart(item: ItqanDispatchItem)
    fun removeFromCart(item: ItqanDispatchItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): ItqanDispatchOrder
    suspend fun refreshCatalog()
}
