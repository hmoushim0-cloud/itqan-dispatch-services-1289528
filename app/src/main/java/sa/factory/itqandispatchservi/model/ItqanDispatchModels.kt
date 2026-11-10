package sa.factory.itqandispatchservi.model

import kotlinx.serialization.Serializable

@Serializable
data class ItqanDispatchItem(
    val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String = "معتمد",
    val category: String = "عام",
    val status: String = "AVAILABLE"
)

@Serializable
data class ItqanDispatchOrder(
    val orderId: String,
    val items: List<ItqanDispatchItem>,
    val subtotalSar: Double,
    val vatSar: Double,
    val totalSar: Double,
    val paymentMethod: String = "Mada Contactless EMV",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CONFIRMED"
)

@Serializable
data class ItqanDispatchTelemetry(
    val peripheralName: String,
    val status: String,
    val liveReading: String,
    val batteryPct: Int = 98,
    val signalDbm: Int = -58,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
    object Empty : UiState<Nothing>
}
