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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private data class MarketAsset(
    val title: String,
    val subtitle: String,
    val symbol: String,
    val icon: String,
    val hasBuySell: Boolean
)

private data class MarketPrice(
    val buy: String? = null,
    val sell: String? = null,
    val price: String? = null
)

private data class RatesResult(
    val rates: Map<String, MarketPrice>,
    val source: String?,
    val updatedAt: String?,
    val message: String
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

private const val RATES_ENDPOINT = "https://arz-net-market-api.truckparttrading-co.workers.dev/api/rates"
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
                androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MarketHome()
                }
            }
        }
    }
}

private fun fetchMarketRates(): RatesResult {
    var connection: HttpURLConnection? = null
    return try {
        connection = (URL(RATES_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        val root = JSONObject(body)
        if (status !in 200..299 || !root.optBoolean("ok")) {
            RatesResult(
                emptyMap(),
                null,
                null,
                if (root.optString("error") == "provider_not_configured")
                    "منبع معتبر نرخ هنوز پیکربندی نشده است"
                else "دریافت نرخ‌ها ناموفق بود"
            )
        } else {
            val items = root.optJSONArray("rates")
            val parsed = mutableMapOf<String, MarketPrice>()
            if (items != null) {
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val symbol = item.optString("symbol").takeIf { it.isNotBlank() } ?: continue
                    fun number(key: String): String? =
                        if (item.has(key) && !item.isNull(key)) item.optDouble(key).takeIf { it.isFinite() }?.toLong()?.toString()
                        else null
                    parsed[symbol] = MarketPrice(number("buy"), number("sell"), number("price"))
                }
            }
            RatesResult(
                parsed,
                root.optString("source").takeIf { it.isNotBlank() },
                root.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
                "اتصال منبع نرخ برقرار است"
            )
        }
    } catch (_: Exception) {
        RatesResult(emptyMap(), null, null, "فعلاً دسترسی به منبع نرخ برقرار نیست")
    } finally {
        connection?.disconnect()
    }
}

@Composable
private fun MarketHome() {
    var result by remember {
        mutableStateOf(RatesResult(emptyMap(), null, null, "در حال بررسی منبع نرخ…"))
    }

    LaunchedEffect(Unit) {
        while (true) {
            result = withContext(Dispatchers.IO) { fetchMarketRates() }
            delay(15_000)
        }
    }

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
                Text(" ${result.message}", color = Color(0xFFD6DCFF), fontSize = 11.sp)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assets) { asset -> AssetCard(asset, result.rates[asset.symbol]) }
            }

            Text(
                text = listOfNotNull(result.source, result.updatedAt).joinToString(" · ").ifBlank { "قیمت‌ها فقط پس از تأیید منبع نمایش داده می‌شوند" },
                color = Color(0xFFD6DCFF),
                fontSize = 9.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AssetCard(asset: MarketAsset, price: MarketPrice?) {
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
                    PriceColumn("خرید", price?.buy)
                    PriceColumn("فروش", price?.sell)
                }
            } else {
                Text("قیمت جهانی", color = Muted, fontSize = 11.sp)
                Text(price?.price ?: "—", color = Color(0xFF20222B), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(asset.symbol, color = Muted, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                if (price != null) "دریافت‌شده از منبع داده" else "در انتظار نرخ معتبر",
                color = Color(0xFF8A8E9B),
                fontSize = 9.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PriceColumn(label: String, value: String?) {
    Column(modifier = Modifier.padding(top = 1.dp)) {
        Text(label, color = Color(0xFF7B8190), fontSize = 11.sp)
        Text(value ?: "—", color = Color(0xFF20222B), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text("تومان", color = Muted, fontSize = 9.sp)
    }
}
