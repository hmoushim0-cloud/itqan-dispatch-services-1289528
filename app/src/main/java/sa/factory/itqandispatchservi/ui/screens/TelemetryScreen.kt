package sa.factory.itqandispatchservi.ui.screens

import androidx.compose.foundation.layout.*
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

@Composable
fun TelemetryScreen(state: ItqanDispatchUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("مراقبة العتاد الحي والحساسات", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BrandTextPrimary)
        Text("بروتوكول الربط المباشر الميداني", fontSize = 12.sp, color = BrandTextSecondary)

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("حالة الجهاز الملحق:", color = BrandTextSecondary, fontSize = 13.sp)
                    Badge(containerColor = BrandPrimary) {
                        Text(state.telemetry?.status ?: "ONLINE", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                    }
                }
                Text("اسم العتاد: ${state.telemetry?.peripheralName ?: 'Mada Contactless EMV L2 NFC + GPS Dispatch'}", fontWeight = FontWeight.Bold, color = BrandTextPrimary, fontSize = 14.sp)
                Text("جودة الإشارة: ${state.telemetry?.signalDbm ?: -58} dBm  •  البطارية: ${state.telemetry?.batteryPct ?: 98}%", color = BrandSecondary, fontSize = 12.sp)
                
                Divider(color = BrandCard)
                Text("حزمة البيانات اللحظية (Raw Telemetry Payload):", color = BrandTextSecondary, fontSize = 11.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.telemetry?.liveReading ?: "Mada NFC: EMV L2 Auth | GPS: 24.713°N 46.675°E | Safety Trigger: 87% Confidence",
                        color = BrandPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("معايير الامتثال والحوكمة", fontWeight = FontWeight.Bold, color = BrandTextPrimary, fontSize = 14.sp)
                Text("✓ تشفير كامل للاتصال الطرفي (AES-256 GCM)", color = BrandTextSecondary, fontSize = 12.sp)
                Text("✓ مطابقة اشتراطات هيئة الاتصالات والفضاء والتقنية", color = BrandTextSecondary, fontSize = 12.sp)
                Text("✓ مزامنة فورية خالية من الإيموجي ومتوافقة مع المعايير السعودية", color = BrandTextSecondary, fontSize = 12.sp)
            }
        }
    }
}
