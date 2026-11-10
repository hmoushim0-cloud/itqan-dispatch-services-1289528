package sa.factory.itqandispatchservi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sa.factory.itqandispatchservi.ui.theme.*
import sa.factory.itqandispatchservi.ui.viewmodel.ItqanDispatchUiState
import sa.factory.itqandispatchservi.ui.viewmodel.ItqanDispatchViewModel

@Composable
fun CheckoutScreen(
    state: ItqanDispatchUiState,
    viewModel: ItqanDispatchViewModel
) {
    val subtotal = state.cart.sumOf { it.priceSar }
    val vat = subtotal * 0.15
    val total = subtotal + vat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("الفاتورة والدفع الرقمي", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandTextPrimary)

        if (state.lastOrder != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("✓ تم اعتماد الطلب بنجاح: ${state.lastOrder.orderId}", fontWeight = FontWeight.Bold, color = BrandPrimary)
                    Text("الإجمالي المسدد: ${state.lastOrder.totalSar} ر.س (شامل 15% ضريبة)", fontSize = 13.sp, color = BrandTextPrimary)
                    Text("بوابة الدفع: ${state.lastOrder.paymentMethod}", fontSize = 11.sp, color = BrandTextSecondary)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            modifier = Modifier.weight(1f)
        ) {
            if (state.cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("السلة فارغة. أضف عناصر من الصفحة الرئيسية", color = BrandTextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.cart) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.title, color = BrandTextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${item.priceSar} ر.س", color = BrandSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = { viewModel.removeFromCart(item) }) {
                                Text("✕", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المجموع الفرعي:", color = BrandTextSecondary, fontSize = 13.sp)
                    Text("${String.format("%.2f", subtotal)} ر.س", color = BrandTextPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ضريبة القيمة المضافة (15%):", color = BrandTextSecondary, fontSize = 13.sp)
                    Text("${String.format("%.2f", vat)} ر.س", color = BrandTextPrimary, fontWeight = FontWeight.Bold)
                }
                Divider(color = BrandSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي الكلي:", color = BrandTextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("${String.format("%.2f", total)} ر.س", color = BrandPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }

        Button(
            onClick = { viewModel.checkout() },
            enabled = state.cart.isNotEmpty() && !state.isCheckingOut,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (state.isCheckingOut) "جاري المعالجة..." else "إتمام الدفع عبر Mada / Apple Pay", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
        }
    }
}
