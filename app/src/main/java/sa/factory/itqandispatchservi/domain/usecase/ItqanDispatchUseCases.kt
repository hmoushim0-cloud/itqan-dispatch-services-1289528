package sa.factory.itqandispatchservi.domain.usecase

import sa.factory.itqandispatchservi.domain.repository.ItqanDispatchRepository
import sa.factory.itqandispatchservi.model.ItqanDispatchItem
import sa.factory.itqandispatchservi.model.ItqanDispatchOrder
import sa.factory.itqandispatchservi.model.ItqanDispatchTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: ItqanDispatchRepository) {
    operator fun invoke(): Flow<List<ItqanDispatchItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: ItqanDispatchRepository) {
    val cart = repository.activeCart
    fun add(item: ItqanDispatchItem) = repository.addToCart(item)
    fun remove(item: ItqanDispatchItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: ItqanDispatchRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): ItqanDispatchOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: ItqanDispatchRepository) {
    operator fun invoke(): Flow<ItqanDispatchTelemetry> = repository.telemetryFlow
}
