package com.chandru.strack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.chandru.strack.data.SaleEntity
import com.chandru.strack.data.STrackDatabase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val Ink = Color(0xFF0B1018)
private val Panel = Color(0xFF151D28)
private val Mint = Color(0xFF74E0C1)
private val Muted = Color(0xFF9AA7B7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(applicationContext, STrackDatabase::class.java, "strack.db").build()
        setContent { STrackApp(database) }
    }
}

@Composable
private fun STrackApp(database: STrackDatabase) {
    val sales by database.saleDao().observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var page by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
        primary = Mint, background = Ink, surface = Panel, onBackground = Color.White,
        onSurface = Color.White, onPrimary = Ink
    )) {
        Scaffold(
            containerColor = Ink,
            contentWindowInsets = WindowInsets.navigationBars,
            floatingActionButton = {
                if (page != 4) FloatingActionButton(
                    onClick = { showAdd = true }, containerColor = Mint, contentColor = Ink,
                    shape = RoundedCornerShape(18.dp)
                ) { Icon(Icons.Default.Add, contentDescription = "Add sale") }
            },
            bottomBar = {
                NavigationBar(containerColor = Panel) {
                    val labels = listOf("Home", "Sales", "Brands", "Analytics", "Nila")
                    val icons = listOf(Icons.Default.Home, Icons.Default.ReceiptLong, Icons.Default.PhoneAndroid, Icons.Default.Analytics, Icons.Default.Assistant)
                    labels.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = page == index,
                            onClick = { page = index },
                            icon = { Icon(icons[index], contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(targetState = page, transitionSpec = { tween(220) togetherWith tween(180) }, label = "page") { target ->
                when (target) {
                    0 -> HomeScreen(sales, Modifier.padding(padding), onAdd = { showAdd = true })
                    1 -> SalesScreen(sales, Modifier.padding(padding), onDelete = { id -> scope.launch { database.saleDao().deleteById(id) } })
                    2 -> BrandsScreen(sales, Modifier.padding(padding))
                    3 -> AnalyticsScreen(sales, Modifier.padding(padding))
                    else -> NilaScreen(Modifier.padding(padding))
                }
            }
        }
        if (showAdd) AddSaleDialog(
            onDismiss = { showAdd = false },
            onSave = { brand, model, quantity ->
                scope.launch {
                    database.saleDao().insert(SaleEntity(brand = brand.trim(), model = model.trim(), quantity = quantity, timestampMillis = System.currentTimeMillis()))
                }
                showAdd = false
            }
        )
    }
}

@Composable
private fun HomeScreen(sales: List<SaleEntity>, modifier: Modifier, onAdd: () -> Unit) {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val todays = sales.filter { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() == today }
    val month = sales.filter { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate().let { d -> d.year == today.year && d.month == today.month } }
    val ftd = todays.sumOf { it.quantity }
    val mtd = month.sumOf { it.quantity }
    val samsung = month.filter { it.brand.equals("Samsung", true) }.sumOf { it.quantity }
    val total = month.sumOf { it.quantity }
    val share = if (total == 0) 0.0 else samsung * 100.0 / total
    val greeting = when (java.time.LocalTime.now().hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; in 17..20 -> "Good evening"; else -> "Good night" }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(18.dp)); Text("$greeting, Chandru 👋", fontSize = 25.sp, fontWeight = FontWeight.Bold); Text("The Chennai Mobile  ·  Sales at a glance", color = Muted, fontSize = 13.sp) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(26.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric("FTD SALES", ftd.toString().padStart(2, '0'), Modifier.weight(1f))
                        Metric("MTD SALES", mtd.toString().padStart(1, '0'), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("SAMSUNG SHARE · THIS MONTH", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.Bottom) { Text("${"%.1f".format(Locale.US, share)}%", fontSize = 34.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Text("of store sales", color = Muted, modifier = Modifier.padding(bottom = 6.dp)) }
                    ShareBar(share.toFloat())
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("STORE TOTAL", total.toString(), "units this month", Modifier.weight(1f)); MetricCard("TODAY", ftd.toString(), "units recorded", Modifier.weight(1f)) } }
        item { Text("Recent sales", fontSize = 18.sp, fontWeight = FontWeight.Bold); if (sales.isEmpty()) EmptySales(onAdd) }
        items(sales.take(4), key = { it.id }) { SaleRow(it) }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) { Text(label, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold); AnimatedContent(value, label = "metric-$label") { Text(it, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Mint) } }
}

@Composable
private fun MetricCard(title: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp)) { Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(value, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text(caption, color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun ShareBar(percent: Float) {
    val progress by animateFloatAsState((percent.coerceIn(0f, 100f) / 100f), tween(450), label = "share")
    Canvas(Modifier.fillMaxWidth().height(8.dp)) {
        drawRoundRect(Color(0xFF2A3542), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
        drawRoundRect(Brush.horizontalGradient(listOf(Mint, Color(0xFF4BB4D6))), size = Size(size.width * progress, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
    }
}

@Composable
private fun EmptySales(onAdd: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No sales yet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Add your first sale to start tracking FTD and MTD.", color = Muted)
            TextButton(onClick = onAdd) { Text("Add your first sale") }
        }
    }
}

@Composable
private fun SaleRow(sale: SaleEntity, onDelete: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF223A3A)), contentAlignment = Alignment.Center) { Text(sale.brand.take(1).uppercase(), color = Mint, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(sale.model, fontWeight = FontWeight.SemiBold)
                Text("${sale.brand} · ${SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(sale.timestampMillis))}", color = Muted, fontSize = 12.sp)
            }
            Text("${sale.quantity} unit${if (sale.quantity == 1) "" else "s"}", color = Mint, fontWeight = FontWeight.SemiBold)
            if (onDelete != null) { IconButton(onClick = onDelete) { Text("×", color = Muted, fontSize = 22.sp) } }
        }
    }
}

@Composable
private fun SalesScreen(sales: List<SaleEntity>, modifier: Modifier, onDelete: (Long) -> Unit) {
    var deleting by remember { mutableStateOf<SaleEntity?>(null) }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Sales history", Modifier.padding(top = 22.dp), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Your sales are saved on this device", color = Muted)
        Spacer(Modifier.height(14.dp))
        if (sales.isEmpty()) EmptySales { }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) { items(sales, key = { it.id }) { sale -> SaleRow(sale) { deleting = sale } } }
    }
    deleting?.let { sale -> AlertDialog(
        onDismissRequest = { deleting = null },
        title = { Text("Delete this sale?") },
        text = { Text("${sale.brand} ${sale.model} · ${sale.quantity} unit(s)") },
        confirmButton = { TextButton(onClick = { onDelete(sale.id); deleting = null }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } }
    ) }
}

@Composable
private fun BrandsScreen(sales: List<SaleEntity>, modifier: Modifier) {
    val total = sales.sumOf { it.quantity }
    val brands = sales.groupBy { it.brand.trim().ifBlank { "Unknown" } }
        .mapValues { (_, entries) -> entries.sumOf { it.quantity } }.entries.sortedByDescending { it.value }
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Brand performance", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("All recorded sales · market share", color = Muted)
        Spacer(Modifier.height(18.dp))
        if (brands.isEmpty()) EmptySales { }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(brands) { (brand, units) ->
                val share = if (total == 0) 0.0 else units * 100.0 / total
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(brand, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("${"%.1f".format(Locale.US, share)}%", color = Mint, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp)); ShareBar(share.toFloat())
                        Text("$units units · #${brands.indexOfFirst { it.key == brand } + 1} by sales", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsScreen(sales: List<SaleEntity>, modifier: Modifier) {
    val today = LocalDate.now(); val zone = ZoneId.systemDefault()
    val monthSales = sales.filter { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate().let { d -> d.month == today.month && d.year == today.year } }
    val models = monthSales.groupBy { "${it.brand} ${it.model}" }.mapValues { (_, s) -> s.sumOf { it.quantity } }.entries.sortedByDescending { it.value }.take(8)
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Analytics", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Month-to-date model performance", color = Muted) }
        item { MetricCard("MONTH TO DATE", monthSales.sumOf { it.quantity }.toString(), "units sold", Modifier.fillMaxWidth()) }
        item { Text("Top selling models", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        if (models.isEmpty()) item { EmptySales { } }
        items(models) { (model, count) ->
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(model, fontWeight = FontWeight.Medium); Text("$count units", color = Mint, fontWeight = FontWeight.Bold) }
            }
        }
        item { Text("Numbers are calculated from your locally saved sales.", color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun NilaScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        NilaAvatar(Modifier.size(150.dp))
        Spacer(Modifier.height(18.dp))
        Text("Meet Nila", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Your mobile sales companion", color = Mint, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(18.dp)) {
                Text("Anime-inspired, showroom-ready", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Nila's AI service is not connected yet. We won't show pretend answers or unverified phone specifications. Your offline sales tracking works as usual.", color = Muted)
            }
        }
    }
}

@Composable
private fun NilaAvatar(modifier: Modifier = Modifier) {
    // Original, abstract anime-inspired mascot illustration drawn locally; no external artwork or network required.
    Canvas(modifier.clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF203D4A), Color(0xFF392A50))))) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val s = size.minDimension
        drawCircle(Color(0xFF17212B), s * .36f, Offset(cx, cy + s * .04f))
        // flowing hair silhouette
        drawOval(Color(0xFF293E64), topLeft = Offset(cx - s * .30f, cy - s * .32f), size = Size(s * .60f, s * .71f))
        drawOval(Color(0xFF506FA8), topLeft = Offset(cx - s * .24f, cy - s * .27f), size = Size(s * .48f, s * .58f))
        // face and fringe
        drawOval(Color(0xFFFFD8C6), topLeft = Offset(cx - s * .19f, cy - s * .15f), size = Size(s * .38f, s * .43f))
        drawOval(Color(0xFF293E64), topLeft = Offset(cx - s * .26f, cy - s * .31f), size = Size(s * .52f, s * .27f))
        drawCircle(Color(0xFF24334D), s * .028f, Offset(cx - s * .085f, cy + s * .045f))
        drawCircle(Color(0xFF24334D), s * .028f, Offset(cx + s * .085f, cy + s * .045f))
        drawCircle(Color.White.copy(alpha = .8f), s * .012f, Offset(cx - s * .092f, cy + s * .035f))
        drawCircle(Color.White.copy(alpha = .8f), s * .012f, Offset(cx + s * .078f, cy + s * .035f))
        drawLine(Color(0xFFE78D9B), Offset(cx - s * .04f, cy + s * .17f), Offset(cx + s * .04f, cy + s * .17f), s * .012f, cap = StrokeCap.Round)
        drawArc(Mint.copy(alpha = .6f), 205f, 130f, false, topLeft = Offset(s * .11f, s * .11f), size = Size(s * .78f, s * .78f), style = Stroke(s * .012f))
    }
}

@Composable
private fun AddSaleDialog(onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    val quantity = quantityText.toIntOrNull() ?: 0
    val brands = listOf("Samsung", "Vivo", "OPPO", "realme", "Redmi", "Xiaomi", "Lava", "TECNO", "Apple", "Nothing", "Google Pixel")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Brand", color = Muted, fontSize = 12.sp)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(brands) { name -> androidx.compose.material3.FilterChip(selected = brand == name, onClick = { brand = name }, label = { Text(name) }) }
                }
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true)
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Phone model") }, singleLine = true)
                OutlinedTextField(value = quantityText, onValueChange = { quantityText = it.filter(Char::isDigit).take(4) }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { if (brand.isNotBlank() && model.isNotBlank() && quantity > 0) onSave(brand, model, quantity) }, enabled = brand.isNotBlank() && model.isNotBlank() && quantity > 0) { Text("Save sale") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
