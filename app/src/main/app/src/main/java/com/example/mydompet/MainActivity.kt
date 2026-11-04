package com.example.mydompet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF080F24)
private val Panel = Color(0xC9172440)
private val Blue = Color(0xFF1478F8)
private val Purple = Color(0xFF873BEB)
private val Green = Color(0xFF08B978)
private val Red = Color(0xFFFF5269)
private val Gold = Color(0xFFFFB52E)
private val Muted = Color(0xFFB8C5DF)

data class WorkEntry(val date: String, val hours: Double)
data class ExpenseEntry(val date: String, val note: String, val category: String, val amount: Long)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 15, 36)
        window.navigationBarColor = android.graphics.Color.rgb(8, 15, 36)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Blue, background = Night, surface = Panel)) { MyDompetApp() } }
    }
}

@Composable
fun MyDompetApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("my_dompet_data", 0) }
    var rateText by remember { mutableStateOf(prefs.getInt("rate", 15000).toString()) }
    var hoursText by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseNote by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makan") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("") }
    var workEntries by remember { mutableStateOf(run {
        val a = JSONArray(prefs.getString("work_entries", "[]") ?: "[]")
        List(a.length()) { i -> val o = a.getJSONObject(i); WorkEntry(o.getString("date"), o.getDouble("hours")) }
    }) }
    var expenses by remember { mutableStateOf(run {
        val a = JSONArray(prefs.getString("expenses", "[]") ?: "[]")
        List(a.length()) { i -> val o = a.getJSONObject(i); ExpenseEntry(o.getString("date"), o.getString("note"), o.getString("category"), o.getLong("amount")) }
    }) }
    fun saveAll() {
        val w = JSONArray(); workEntries.forEach { w.put(JSONObject().put("date", it.date).put("hours", it.hours)) }
        val e = JSONArray(); expenses.forEach { e.put(JSONObject().put("date", it.date).put("note", it.note).put("category", it.category).put("amount", it.amount)) }
        prefs.edit().putString("work_entries", w.toString()).putString("expenses", e.toString()).putInt("rate", rateText.toIntOrNull() ?: 15000).apply()
    }
    val rate = rateText.toLongOrNull()?.coerceAtLeast(0) ?: 0L
    val totalHours = workEntries.sumOf { it.hours }
    val income = (totalHours * rate).toLong()
    val spent = expenses.sumOf { it.amount }
    val balance = income - spent
    val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val dateNow = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())

    Scaffold(containerColor = Night, bottomBar = {
        NavigationBar(containerColor = Color(0xFF101A31), contentColor = Color.White, tonalElevation = 12.dp) {
            val tabs = listOf("Beranda", "Gaji", "Pengeluaran", "Ringkasan")
            val icons = listOf("⌂", "▣", "🛒", "▥")
            tabs.forEachIndexed { index, label ->
                NavigationBarItem(selected = selectedTab == index, onClick = { selectedTab = index; message = "" },
                    icon = { Text(icons[index], fontSize = 21.sp, fontWeight = FontWeight.Bold) },
                    label = { Text(label, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF55A6FF), selectedTextColor = Color(0xFF55A6FF), unselectedIconColor = Muted, unselectedTextColor = Muted, indicatorColor = Color(0xFF203A63)))
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Night)) {
            val wallpaper = when (selectedTab) {
                0 -> R.drawable.bg_home
                1 -> R.drawable.bg_gaji
                2 -> R.drawable.bg_pengeluaran
                else -> R.drawable.bg_ringkasan
            }
            Image(
                painter = painterResource(wallpaper),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark glass overlay keeps all financial information easy to read.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color(0x55040A18), Color(0xA8081022), Color(0xE6081022))
                    )
                )
            )
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 12.dp)) {
                when (selectedTab) {
                    0 -> {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(Gold, Color(0xFFFF7A24)))), contentAlignment = Alignment.Center) { Text("💼", fontSize = 22.sp) }
                                Spacer(Modifier.width(10.dp)); Column { Text("My Dompet", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold); Text("Kelola uang dengan bijak", color = Muted, fontSize = 11.sp) }
                            }
                            Text("⚙", color = Color.White, fontSize = 25.sp)
                        }
                        Spacer(Modifier.height(18.dp))
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF283B68), Color(0xFF17233F), Color(0xFF34234E)))).padding(18.dp)) {
                            Text("HALO, PENGGUNA! 👋", color = Color(0xFFFFD17A), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                            Text("Atur keuanganmu,\nwujudkan tujuanmu.", color = Color.White, fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.ExtraBold)
                            Text(dateNow, color = Muted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(13.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("💰", "Total Gaji", currency.format(income), Blue, Modifier.weight(1f))
                            MetricCard("🛒", "Total Pengeluaran", currency.format(spent), Red, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        BalanceCard(currency.format(balance))
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionButton("▣  Gaji", Blue, Modifier.weight(1f)) { selectedTab = 1 }
                            ActionButton("🛒  Pengeluaran", Purple, Modifier.weight(1f)) { selectedTab = 2 }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Transaksi Terbaru", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { selectedTab = 3 }) { Text("Lihat Semua  ›", color = Color(0xFF8FC4FF), fontSize = 12.sp) }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                            val recent = (workEntries.map { Triple(it.date, "Gaji • ${it.hours} jam", (it.hours * rate).toLong()) } + expenses.map { Triple(it.date, it.note, -it.amount) }).takeLast(5).reversed()
                            if (recent.isEmpty()) item { EmptyState("Belum ada transaksi", "Tambahkan gaji atau pengeluaran pertamamu.") }
                            itemsIndexed(recent) { _, item -> TransactionRow(if (item.third >= 0) "＋" else "−", item.second, item.first, (if (item.third >= 0) "+" else "-") + currency.format(kotlin.math.abs(item.third)), item.third >= 0) }
                        }
                    }
                    1 -> {
                        PageHeader("Gaji", "Catat jam kerja dan pantau pendapatan")
                        Spacer(Modifier.height(12.dp)); MetricCard("💰", "Total Gaji", currency.format(income), Blue, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp)); SectionPanel {
                            Text("Tambah Catatan Gaji", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            DarkField(rateText, { rateText = it.filter(Char::isDigit); saveAll() }, "Tarif per jam (Rp)", KeyboardType.Number)
                            DarkField(hoursText, { hoursText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } }, "Jam kerja", KeyboardType.Decimal)
                            GradientButton("＋  Simpan Gaji", Blue) {
                                val h = hoursText.replace(',', '.').toDoubleOrNull()
                                if (h == null || h <= 0 || h > 24 || rate <= 0) message = "Masukkan jam 0–24 dan tarif yang valid."
                                else { val d = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date()); workEntries = workEntries + WorkEntry(d, h); hoursText = ""; saveAll(); message = "Catatan gaji berhasil disimpan." }
                            }
                            if (message.isNotBlank()) Text(message, color = Color(0xFF75E6B4), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp)); Text("Riwayat Gaji", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            itemsIndexed(workEntries.indices.reversed().toList()) { _, idx -> val item = workEntries[idx]; HistoryRow("💼", "Gaji • ${item.hours} jam", item.date, currency.format((item.hours * rate).toLong()), true) { workEntries = workEntries.toMutableList().also { it.removeAt(idx) }; saveAll() } }
                        }
                    }
                    2 -> {
                        PageHeader("Pengeluaran", "Catat setiap kebutuhan dan belanja")
                        Spacer(Modifier.height(12.dp)); MetricCard("🛒", "Total Pengeluaran", currency.format(spent), Red, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp)); SectionPanel {
                            Text("Tambah Pengeluaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            DarkField(expenseAmount, { expenseAmount = it.filter(Char::isDigit) }, "Nominal (Rp)", KeyboardType.Number)
                            DarkField(expenseNote, { expenseNote = it }, "Keterangan", KeyboardType.Text)
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Kategori: $category   ▾") }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { listOf("Makan", "Minum", "Bensin", "Jajan", "Belanja", "Transportasi", "Lainnya").forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; expanded = false }) } }
                            }
                            GradientButton("＋  Simpan Pengeluaran", Purple) {
                                val amount = expenseAmount.toLongOrNull()
                                if (amount == null || amount <= 0 || expenseNote.isBlank()) message = "Isi nominal dan keterangan terlebih dahulu."
                                else { val d = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date()); expenses = expenses + ExpenseEntry(d, expenseNote.trim(), category, amount); expenseAmount = ""; expenseNote = ""; saveAll(); message = "Pengeluaran berhasil disimpan." }
                            }
                            if (message.isNotBlank()) Text(message, color = Color(0xFF75E6B4), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp)); Text("Riwayat Pengeluaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            itemsIndexed(expenses.indices.reversed().toList()) { _, idx -> val item = expenses[idx]; HistoryRow("🛒", item.note, "${item.date} • ${item.category}", "-${currency.format(item.amount)}", false) { expenses = expenses.toMutableList().also { it.removeAt(idx) }; saveAll() } }
                        }
                    }
                    else -> {
                        PageHeader("Ringkasan", "Gambaran kondisi keuanganmu")
                        Spacer(Modifier.height(16.dp))
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFF34204E), Color(0xFF182B51)))).padding(18.dp)) {
                            Text("RINGKASAN KEUANGAN", color = Color(0xFFFFD17A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Keuanganmu saat ini", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text(dateNow, color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(18.dp)); Text("Sisa Uang", color = Muted, fontSize = 13.sp)
                            Text(currency.format(balance), color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.height(12.dp)); MetricCard("💰", "Total Gaji", currency.format(income), Green, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp)); MetricCard("🛒", "Total Pengeluaran", currency.format(spent), Red, Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp)); InfoPanel("Total Jam Kerja", "${"%.2f".format(Locale.US, totalHours)} jam")
                        Spacer(Modifier.height(10.dp)); InfoPanel("Jumlah Transaksi", "${workEntries.size + expenses.size} transaksi")
                        Spacer(Modifier.height(16.dp)); Text("Aktivitas Terbaru", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                            val recent = (workEntries.map { Triple(it.date, "Gaji • ${it.hours} jam", (it.hours * rate).toLong()) } + expenses.map { Triple(it.date, it.note, -it.amount) }).reversed()
                            itemsIndexed(recent) { _, item -> TransactionRow(if (item.third >= 0) "＋" else "−", item.second, item.first, (if (item.third >= 0) "+" else "-") + currency.format(kotlin.math.abs(item.third)), item.third >= 0) }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun PageHeader(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = Color.White, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
        Column { Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold); Text(subtitle, color = Muted, fontSize = 12.sp) }
    }
}

@Composable private fun MetricCard(icon: String, title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(19.dp)).background(Brush.linearGradient(listOf(accent.copy(alpha = .30f), Panel))).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 27.sp); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = Muted, fontSize = 11.sp); Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable private fun BalanceCard(value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF087C55), Color(0xFF07563F)))).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("👛", fontSize = 28.sp); Spacer(Modifier.width(14.dp)); Column { Text("Sisa Uang", color = Color(0xFFC5FFE4), fontSize = 12.sp); Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable private fun ActionButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(8.dp)) { Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
}

@Composable private fun SectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Panel).padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable private fun DarkField(value: String, onValueChange: (String) -> Unit, label: String, keyboard: KeyboardType) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, color = Muted) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF65AFFF), unfocusedBorderColor = Color(0xFF53617C), cursorColor = Color.White))
}

@Composable private fun GradientButton(text: String, color: Color, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(text, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable private fun TransactionRow(icon: String, title: String, date: String, amount: String, positive: Boolean) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xA916223A)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(if (positive) Green else Red), contentAlignment = Alignment.Center) { Text(icon, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(date, color = Muted, fontSize = 10.sp) }
        Text(amount, color = if (positive) Color(0xFF39D98A) else Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable private fun HistoryRow(icon: String, title: String, subtitle: String, amount: String, positive: Boolean, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 22.sp); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text(subtitle, color = Muted, fontSize = 10.sp) }
        Column(horizontalAlignment = Alignment.End) { Text(amount, color = if (positive) Color(0xFF39D98A) else Red, fontWeight = FontWeight.Bold, fontSize = 12.sp); TextButton(onClick = onDelete, contentPadding = PaddingValues(0.dp)) { Text("Hapus", color = Muted, fontSize = 10.sp) } }
    }
}

@Composable private fun InfoPanel(title: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Panel).padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, color = Muted); Text(value, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("📒", fontSize = 28.sp); Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 11.sp) }
}
