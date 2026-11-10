package sa.factory.itqandispatchservi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.itqandispatchservi.model.ItqanDispatchItem

@Entity(tableName = "services_items_cache")
data class ItqanDispatchEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): ItqanDispatchItem = ItqanDispatchItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
