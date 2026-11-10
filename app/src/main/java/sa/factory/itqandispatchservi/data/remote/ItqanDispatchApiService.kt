package sa.factory.itqandispatchservi.data.remote

import sa.factory.itqandispatchservi.model.ItqanDispatchItem
import sa.factory.itqandispatchservi.model.ItqanDispatchOrder

interface ItqanDispatchApiService {
    suspend fun fetchCatalog(): List<ItqanDispatchItem>
    suspend fun submitOrder(order: ItqanDispatchOrder): Boolean
}
