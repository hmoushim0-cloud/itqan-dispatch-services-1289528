package sa.factory.itqandispatchservi.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sa.factory.itqandispatchservi.di.DiContainer
import sa.factory.itqandispatchservi.ui.screens.*
import sa.factory.itqandispatchservi.ui.theme.*

class MainActivity : ComponentActivity() {
    private val di = DiContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = di.provideViewModel()

        setContent {
            ItqanDispatchTheme {
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = BrandSurface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = state.activeTab == "home",
                                onClick = { viewModel.switchTab("home") },
                                label = { Text("الرئيسية") },
                                icon = { Text("H", color = if (state.activeTab == "home") BrandPrimary else BrandTextSecondary, fontWeight = FontWeight.Bold) }
                            )
                            NavigationBarItem(
                                selected = state.activeTab == "telemetry",
                                onClick = { viewModel.switchTab("telemetry") },
                                label = { Text("العتاد") },
                                icon = { Text("IOT", color = if (state.activeTab == "telemetry") BrandPrimary else BrandTextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = state.activeTab == "checkout",
                                onClick = { viewModel.switchTab("checkout") },
                                label = { Text("الفاتورة (${state.cart.size})") },
                                icon = { Text("PAY", color = if (state.activeTab == "checkout") BrandPrimary else BrandTextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        when (state.activeTab) {
                            "home" -> HomeScreen(state = state, viewModel = viewModel, onItemClick = { viewModel.addToCart(it) })
                            "telemetry" -> TelemetryScreen(state = state)
                            "checkout" -> CheckoutScreen(state = state, viewModel = viewModel)
                            else -> HomeScreen(state = state, viewModel = viewModel, onItemClick = { viewModel.addToCart(it) })
                        }
                    }
                }
            }
        }
    }
}
