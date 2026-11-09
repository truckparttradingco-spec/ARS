package com.truckparttrading.arsnet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

private data class MarketAsset(
    val title: String,
    val subtitle: String,
    val symbol: String,
    val icon: String,
    val hasBuySell: Boolean
)

private val assets = listOf(
    MarketAsset("دلار آمریکا", "US Dollar", "USD", "🇺🇸", true),
    MarketAsset("یورو", "Euro", "EUR", "🇪🇺", true),
    MarketAsset("یوان چین", "Chinese Yuan", "CNY", "🇨🇳", true),
    MarketAsset("لیر ترکیه", "Turkish Lira", "TRY", "🇹🇷", true),
    MarketAsset("درهم امارات", "UAE Dirham", "AED", "🇦🇪", true),
    MarketAsset("تتر", "Tether", "USDT", "₮", true),
    MarketAsset("سکه طلا", "Gold Coin", "COIN", "🪙", true),
    MarketAsset("طلای ۱۸ عیار · هر گرم", "18K Gold / Gram", "18K", "🟨", true),
    MarketAsset("اونس جهانی طلا", "Gold Spot", "XAU/USD", "🥇", false),
    MarketAsset("بیت‌کوین", "Bitcoin", "BTC/USD", "₿", false)
)

private val Navy = Color(0xFF07113D)
private val Blue = Color(0xFF233FC2)
private val CardWhite = Color(0xFFFAFAFC)
private val Muted = Color(0xFF858A98)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF91A8FF),
                background = Navy,
                surface = CardWhite
            )) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MarketHome()
                }
            }
        }
    }
}

@Composable
private fun MarketHome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF07113D), Blue, Color(0xFF111A58))))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ARZ-NET", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("نرخ لحظه‌ای بازارها", color = Color(0xFFD4DCFF), fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0x332FFFFFF)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                        Text(" بازار آزاد", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Color(0xFFB9C8FF), modifier = Modifier.size(16.dp))
                Text(
                    " نرخ‌ها پس از اتصال منابع معتبر نمایش داده می‌شوند",
                    color = Color(0xFFD6DCFF),
                    fontSize = 11.sp
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assets) { asset -> AssetCard(asset) }
            }
        }
    }
}

@Composable
private fun AssetCard(asset: MarketAsset) {
    Card(
        modifier = Modifier.fillMaxWidth().height(if (asset.hasBuySell) 190.dp else 166.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(39.dp).background(Color(0xFFF0F2FA), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(asset.icon, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
                Spacer(Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(asset.title, color = Color(0xFF171923), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1)
                    Text(asset.subtitle, color = Muted, fontSize = 10.sp, textAlign = TextAlign.End)
                }
            }
            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFE8EAF1))
            Spacer(Modifier.height(9.dp))
            if (asset.hasBuySell) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PriceColumn("خرید")
                    PriceColumn("فروش")
                }
            } else {
                Text("قیمت جهانی", color = Muted, fontSize = 11.sp)
                Text("—", color = Color(0xFF20222B), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(asset.symbol, color = Muted, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (asset.hasBuySell) "در انتظار دریافت نرخ معتبر" else "منبع جهانی · در انتظار اتصال",
                color = Color(0xFF8A8E9B),
                fontSize = 9.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PriceColumn(label: String) {
    Column(modifier = Modifier.padding(top = 1.dp)) {
        Text(label, color = Color(0xFF7B8190), fontSize = 11.sp)
        Text("—", color = Color(0xFF20222B), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text("تومان", color = Muted, fontSize = 9.sp)
    }
}
