package com.smartmenu.cafeapp.data.repository

import com.smartmenu.cafeapp.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class CafeRepository(private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private val _cafes = MutableStateFlow<List<Cafe>>(emptyList())
    val cafes: StateFlow<List<Cafe>> = _cafes.asStateFlow()

    private val _currentCafe = MutableStateFlow<Cafe?>(null)
    val currentCafe: StateFlow<Cafe?> = _currentCafe.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _tables = MutableStateFlow<List<TableInfo>>(emptyList())
    val tables: StateFlow<List<TableInfo>> = _tables.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val bustanCafe = Cafe(
            id = "bustan-cafe",
            name = "كافيه البستان",
            password = "123",
            status = "approved",
            createdAt = "2026-09-01",
            phone = "+966 50 123 4567",
            welcomeMessage = "أهلاً وسهلاً بكم في كافيه البستان! مسح الباركود يتيح لكم استعراض قائمتنا والطلب مباشرة لطاولتكم ☕",
            currency = "SAR",
            vatPercentage = 15.0,
            wifiSsid = "Bustan-VIP",
            wifiPassword = "CoffeeBustan2026",
            totalTables = 12
        )

        val jasmineCafe = Cafe(
            id = "jasmine-cafe",
            name = "مقهى الياسمين",
            password = "123",
            status = "pending",
            createdAt = "2026-09-25",
            phone = "+966 55 987 6543",
            welcomeMessage = "مرحباً بكم في مقهى الياسمين",
            currency = "SAR",
            vatPercentage = 15.0,
            totalTables = 8
        )

        _cafes.value = listOf(bustanCafe, jasmineCafe)
        setCurrentCafe(bustanCafe)
    }

    fun setCurrentCafe(cafe: Cafe?) {
        _currentCafe.value = cafe
        if (cafe != null) {
            setupCafeData(cafe)
        }
    }

    private fun setupCafeData(cafe: Cafe) {
        val defaultCategories = listOf(
            Category("cat_hot", cafe.id, "مشروبات ساخنة", "Hot Drinks", "☕", 1),
            Category("cat_cold", cafe.id, "مشروبات باردة", "Cold Drinks", "🧋", 2),
            Category("cat_sweets", cafe.id, "حلويات ومخبوزات", "Desserts & Bakery", "🍰", 3),
            Category("cat_specialty", cafe.id, "قهوة مختصة V60", "Specialty Coffee", "✨", 4),
            Category("cat_shisha", cafe.id, "معسلات وشيشة", "Hookah Lounge", "💨", 5)
        )
        _categories.value = defaultCategories

        val defaultProducts = listOf(
            Product("p1", cafe.id, "cat_hot", "كابتشينو إيطالي", "Italian Cappuccino", "مزيج متوازن من الإسبريسو الفاخر وحليب مبخر بطبقة رغوة ناعمة", 18.0, true, "☕", 4),
            Product("p2", cafe.id, "cat_hot", "إسبريسو دبل شوت", "Double Espresso", "خلاصة حبوب البن المحمصة الطازجة بنكهة غنية وقوام مخملي", 14.0, true, "☕", 2),
            Product("p3", cafe.id, "cat_hot", "فلات وايت كلاسيك", "Flat White", "إسبريسو مكثف مع مايكروفوم حليبي ناعم ودافئ", 19.0, true, "☕", 4),
            Product("p4", cafe.id, "cat_hot", "شاي كرك بالزعفران", "Karak Tea Saffron", "شاي أسود معتّق مع حليب مبخر وهيل وزعفران أصلي", 12.0, true, "🫖", 3),
            Product("p5", cafe.id, "cat_cold", "سبانش لاتيه مثلج", "Iced Spanish Latte", "المشروب الأكثر طلباً: حليب مكثف ومحلى مع إسبريسو وثلج", 22.0, true, "🧊", 3),
            Product("p6", cafe.id, "cat_cold", "آيس أمريكانو منعش", "Iced Americano", "إسبريسو مثلج مع ماء بارد ومذاق نقي خالي من السكر", 16.0, true, "🧊", 2),
            Product("p7", cafe.id, "cat_cold", "موهيتو توت أزرق بلوبيري", "Blueberry Mojito", "توت بري منعش مع نعناع طازج وليمون ومياه غازية", 24.0, true, "🫐", 3),
            Product("p8", cafe.id, "cat_sweets", "تشيز كيك سان سيباستيان", "San Sebastian Cheesecake", "تشيز كيك إسباني محروق بقوام كريمي يقدم مع شوكولاتة بلجيكية", 28.0, true, "🍰", 2),
            Product("p9", cafe.id, "cat_sweets", "كيكة الزعفران الملكية", "Saffron Milk Cake", "كيكة إسفنجية غارقة بحليب الزعفران ومزينة بالكريمة الهشة", 26.0, true, "🥮", 2),
            Product("p10", cafe.id, "cat_specialty", "قهوة كولومبيا V60", "Colombia V60", "إيحاءات الفواكه الحمضية والشوكولاتة الداكنة بتقطير يدوي فائق الدقة", 25.0, true, "☕", 6),
            Product("p11", cafe.id, "cat_shisha", "شيشة تفاحتين فاخر", "Double Apple Shisha", "معسل فاخر مميز مع فحم طبيعي وخدمة تغيير الفحم المستمرة", 45.0, true, "💨", 8)
        )
        _products.value = defaultProducts

        // Tables
        val tableList = (1..cafe.totalTables).map { num ->
            TableInfo(
                tableNumber = num,
                status = if (num in listOf(2, 5)) TableStatus.OCCUPIED else TableStatus.AVAILABLE,
                activeOrderId = if (num == 2) "ord_101" else if (num == 5) "ord_102" else null
            )
        }
        _tables.value = tableList

        // Sample Orders
        val sampleOrders = listOf(
            Order(
                id = "ord_101",
                cafeId = cafe.id,
                tableNumber = 2,
                items = listOf(
                    OrderItem("p1", "كابتشينو إيطالي", 18.0, 2, "سكر خفيف"),
                    OrderItem("p8", "تشيز كيك سان سيباستيان", 28.0, 1, "شوكولاتة إضافية")
                ),
                status = OrderStatus.PREPARING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 12,
                notes = "الرجاء تقديم الكيك أولاً",
                subtotal = 64.0,
                vat = 9.6,
                total = 73.6,
                isPaid = false
            ),
            Order(
                id = "ord_102",
                cafeId = cafe.id,
                tableNumber = 5,
                items = listOf(
                    OrderItem("p5", "سبانش لاتيه مثلج", 22.0, 1),
                    OrderItem("p11", "شيشة تفاحتين فاخر", 45.0, 1)
                ),
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 4,
                notes = "طاولة التراس الخارجي",
                subtotal = 67.0,
                vat = 10.05,
                total = 77.05,
                isPaid = false
            ),
            Order(
                id = "ord_100",
                cafeId = cafe.id,
                tableNumber = 3,
                items = listOf(
                    OrderItem("p2", "إسبريسو دبل شوت", 14.0, 1),
                    OrderItem("p9", "كيكة الزعفران الملكية", 26.0, 1)
                ),
                status = OrderStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 45,
                subtotal = 40.0,
                vat = 6.0,
                total = 46.0,
                isPaid = true
            )
        )
        _orders.value = sampleOrders
    }

    suspend fun registerCafe(name: String, slug: String, pass: String): Result<Cafe> = withContext(Dispatchers.IO) {
        val newCafe = Cafe(
            id = slug.lowercase().trim(),
            name = name.trim(),
            password = pass,
            status = "pending",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        )

        // Save locally
        _cafes.value = _cafes.value.filter { it.id != newCafe.id } + newCafe

        // Sync with Firebase Firestore REST API for cafe-bons
        try {
            syncCafeToFirebase(newCafe)
        } catch (e: Exception) {
            // Keep local success even if offline
        }

        Result.success(newCafe)
    }

    suspend fun loginCafe(cafeId: String, pass: String): Result<Cafe> = withContext(Dispatchers.IO) {
        val id = cafeId.lowercase().trim()
        val found = _cafes.value.find { it.id == id }

        if (found == null) {
            return@withContext Result.failure(Exception("لم يتم العثور على هذا الكافيه!"))
        }

        if (found.password != pass) {
            return@withContext Result.failure(Exception("كلمة المرور غير صحيحة!"))
        }

        if (found.status != "approved") {
            return@withContext Result.failure(Exception("PENDING_APPROVAL"))
        }

        setCurrentCafe(found)
        Result.success(found)
    }

    fun updateCafeStatus(cafeId: String, newStatus: String) {
        _cafes.value = _cafes.value.map {
            if (it.id == cafeId) it.copy(status = newStatus) else it
        }
        if (_currentCafe.value?.id == cafeId) {
            _currentCafe.value = _currentCafe.value?.copy(status = newStatus)
        }
    }

    fun updateCafeSettings(updated: Cafe) {
        _currentCafe.value = updated
        _cafes.value = _cafes.value.map {
            if (it.id == updated.id) updated else it
        }
        // Update table count if changed
        val currentCount = _tables.value.size
        if (updated.totalTables != currentCount) {
            val newTables = (1..updated.totalTables).map { num ->
                _tables.value.find { it.tableNumber == num } ?: TableInfo(tableNumber = num)
            }
            _tables.value = newTables
        }
    }

    fun addCategory(nameAr: String, nameEn: String, emoji: String) {
        val cafe = _currentCafe.value ?: return
        val newCat = Category(
            id = "cat_${UUID.randomUUID().toString().take(6)}",
            cafeId = cafe.id,
            nameArabic = nameAr,
            nameEnglish = nameEn,
            iconEmoji = emoji.ifBlank { "🍽️" },
            displayOrder = _categories.value.size + 1
        )
        _categories.value = _categories.value + newCat
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        _products.value = _products.value.filter { it.categoryId != categoryId }
    }

    fun addProduct(
        categoryId: String,
        nameAr: String,
        nameEn: String,
        description: String,
        price: Double,
        emoji: String,
        prepMinutes: Int
    ) {
        val cafe = _currentCafe.value ?: return
        val newProd = Product(
            id = "p_${UUID.randomUUID().toString().take(6)}",
            cafeId = cafe.id,
            categoryId = categoryId,
            nameArabic = nameAr,
            nameEnglish = nameEn,
            description = description,
            price = price,
            isAvailable = true,
            iconEmoji = emoji.ifBlank { "☕" },
            prepTimeMinutes = prepMinutes
        )
        _products.value = _products.value + newProd
    }

    fun toggleProductAvailability(productId: String) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(isAvailable = !it.isAvailable) else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filter { it.id != productId }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) {
                val isPaid = if (newStatus == OrderStatus.COMPLETED) true else order.isPaid
                order.copy(status = newStatus, isPaid = isPaid)
            } else order
        }

        // If completed or cancelled, free the table
        val targetOrder = _orders.value.find { it.id == orderId }
        if (targetOrder != null && (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED)) {
            _tables.value = _tables.value.map { table ->
                if (table.tableNumber == targetOrder.tableNumber) {
                    table.copy(status = TableStatus.AVAILABLE, activeOrderId = null)
                } else table
            }
        }
    }

    fun placeCustomerOrder(
        tableNumber: Int,
        items: List<OrderItem>,
        notes: String
    ): Order? {
        val cafe = _currentCafe.value ?: return null
        if (items.isEmpty()) return null

        val subtotal = items.sumOf { it.price * it.quantity }
        val vat = subtotal * (cafe.vatPercentage / 100.0)
        val total = subtotal + vat

        val newOrder = Order(
            id = "ord_${UUID.randomUUID().toString().take(6)}",
            cafeId = cafe.id,
            tableNumber = tableNumber,
            items = items,
            status = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            notes = notes,
            subtotal = subtotal,
            vat = vat,
            total = total,
            isPaid = false
        )

        _orders.value = listOf(newOrder) + _orders.value
        _tables.value = _tables.value.map { table ->
            if (table.tableNumber == tableNumber) {
                table.copy(status = TableStatus.OCCUPIED, activeOrderId = newOrder.id)
            } else table
        }

        return newOrder
    }

    private fun syncCafeToFirebase(cafe: Cafe) {
        val firestoreUrl = "https://firestore.googleapis.com/v1/projects/cafe-bons/databases/(default)/documents/cafes/${cafe.id}"
        val url = URL(firestoreUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 4000
            readTimeout = 4000
        }

        val jsonFields = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("cafeName", JSONObject().put("stringValue", cafe.name))
                put("cafeId", JSONObject().put("stringValue", cafe.id))
                put("password", JSONObject().put("stringValue", cafe.password))
                put("status", JSONObject().put("stringValue", cafe.status))
                put("createdAt", JSONObject().put("stringValue", cafe.createdAt))
            })
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonFields.toString())
            writer.flush()
        }
        val responseCode = conn.responseCode
        conn.disconnect()
    }
}
