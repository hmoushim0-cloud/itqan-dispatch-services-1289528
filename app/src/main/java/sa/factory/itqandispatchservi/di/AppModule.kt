package sa.factory.itqandispatchservi.di

import sa.factory.itqandispatchservi.data.repository.ItqanDispatchRepositoryImpl
import sa.factory.itqandispatchservi.domain.repository.ItqanDispatchRepository
import sa.factory.itqandispatchservi.domain.usecase.*
import sa.factory.itqandispatchservi.hardware.HardwareSensorDriver
import sa.factory.itqandispatchservi.ui.viewmodel.ItqanDispatchViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: ItqanDispatchRepository = ItqanDispatchRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): ItqanDispatchViewModel = ItqanDispatchViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
