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

    private var webApiKey: String = "AIzaSyAJ5lYgz7GGUkUNMU2LJFqB1WoR09Ipgjg"

    init {
        seedInitialData()
        CoroutineScope(Dispatchers.IO).launch {
            fetchFirebaseSettings()
            fetchCafesFromFirebase()
        }
    }

    suspend fun fetchFirebaseSettings() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://firestore.googleapis.com/v1/projects/cafe-bons/databases/(default)/documents/settings/firebase")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
            }
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val fields = rootJson.optJSONObject("fields")
                webApiKey = fields?.optJSONObject("webApiKey")?.optString("stringValue") ?: ""
            }
            conn.disconnect()
        } catch (e: Exception) {
            // Ignore offline fallback
        }
    }

    suspend fun registerCafe(name: String, email: String, pass: String, whatsappPhone: String = ""): Result<Cafe> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val slugBase = cleanEmail.substringBefore("@")
            .replace("[^a-zA-Z0-9-]".toRegex(), "")
            .ifBlank { "cafe" }
        val uniqueId = "$slugBase-${System.currentTimeMillis() % 100000}"

        val newCafe = Cafe(
            id = uniqueId,
            name = name.trim(),
            email = cleanEmail,
            password = pass.trim(),
            phone = whatsappPhone.trim().ifBlank { "+964 770 000 0000" },
            status = "pending",
            emailVerified = true,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        )

        // Save locally - replace any previous state for this email
        _cafes.value = _cafes.value.filter { it.email != newCafe.email } + newCafe

        // Sync with Firebase Firestore REST API for cafe-bons
        try {
            syncCafeToFirebase(newCafe)
        } catch (e: Exception) {
            // Keep local success even if offline
        }

        Result.success(newCafe)
    }

    suspend fun verifyEmailCode(cafeIdOrEmail: String, inputCode: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val query = cafeIdOrEmail.trim().lowercase()
        val found = _cafes.value.find { 
            it.id.equals(query, ignoreCase = true) || it.email.equals(query, ignoreCase = true) 
        }

        if (found == null) {
            return@withContext Result.failure(Exception("لم يتم العثور على الحساب!"))
        }

        // Check if code matches (or accept 123456 as master test bypass)
        if (inputCode.trim() != found.verificationCode && inputCode.trim() != "123456") {
            return@withContext Result.failure(Exception("رمز التحقق غير صحيح! يرجى التأكد من الرمز المدخل."))
        }

        val updated = found.copy(emailVerified = true)
        _cafes.value = _cafes.value.map { if (it.id == updated.id) updated else it }
        try {
            syncCafeToFirebase(updated)
        } catch (e: Exception) {}

        Result.success(true)
    }

    suspend fun loginCafe(emailOrId: String, pass: String): Result<Cafe> = withContext(Dispatchers.IO) {
        // Fetch latest approvals and statuses from Firestore first
        try {
            fetchCafesFromFirebase()
        } catch (e: Exception) {}

        val query = emailOrId.lowercase().trim()
        val queryPrefix = query.substringBefore("@")

        // Lenient intelligent search
        var found = _cafes.value.find { cafe ->
            val cafeEmail = cafe.email.lowercase().trim()
            val cafeId = cafe.id.lowercase().trim()
            val cafeEmailPrefix = cafeEmail.substringBefore("@")

            // 1. Direct match on email
            (cafeEmail.isNotEmpty() && cafeEmail == query) ||
            // 2. Direct match on cafe ID
            (cafeId.isNotEmpty() && cafeId == query) ||
            // 3. Prefix match
            (cafeEmailPrefix.isNotEmpty() && (cafeEmailPrefix == query || cafeEmailPrefix == queryPrefix)) ||
            (cafeId.isNotEmpty() && (cafeId == queryPrefix || query == cafeId || queryPrefix.startsWith(cafeId) || cafeId.startsWith(queryPrefix)))
        }

        // If not found in local cafes and webApiKey is active, check Firebase Auth REST API
        if (found == null && webApiKey.isNotBlank() && query.contains("@")) {
            try {
                val authRes = firebaseAuthSignIn(query, pass)
                if (authRes != null) {
                    val newCafe = Cafe(
                        id = queryPrefix.replace("[^a-zA-Z0-9-]".toRegex(), "").ifBlank { "cafe-" + (System.currentTimeMillis() % 100000) },
                        name = "كافيه $queryPrefix",
                        email = query,
                        password = pass,
                        status = "approved",
                        emailVerified = true,
                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                    )
                    _cafes.value = _cafes.value + newCafe
                    try { syncCafeToFirebase(newCafe) } catch(e: Exception) {}
                    found = newCafe
                }
            } catch(e: Exception) {}
        }

        if (found == null) {
            return@withContext Result.failure(Exception("لم يتم العثور على حساب مرتبط بهذا البريد أو المعرف!"))
        }

        // Update empty email in Firestore if the user logged in with an email
        if (found.email.isBlank() && query.contains("@")) {
            val updated = found.copy(email = query)
            found = updated
            _cafes.value = _cafes.value.map { if (it.id == updated.id) updated else it }
            try { syncCafeToFirebase(updated) } catch(e: Exception) {}
        }

        if (found.password.isNotBlank() && found.password != pass) {
            return@withContext Result.failure(Exception("كلمة المرور غير صحيحة!"))
        }

        if (found.status.equals("suspended", ignoreCase = true)) {
            return@withContext Result.failure(Exception("SUSPENDED_ACCOUNT"))
        }

        if (!found.status.equals("approved", ignoreCase = true)) {
            return@withContext Result.failure(Exception("PENDING_APPROVAL"))
        }

        setCurrentCafe(found)
        Result.success(found)
    }

    private fun sendFirebaseAuthVerification(email: String, pass: String) {
        if (webApiKey.isBlank()) return
        try {
            // 1. Sign up user via Firebase Auth REST API
            val signUpUrl = URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$webApiKey")
            val conn = (signUpUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }
            val payload = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()); it.flush() }
            if (conn.responseCode in 200..299) {
                val res = conn.inputStream.bufferedReader().use { it.readText() }
                val idToken = JSONObject(res).optString("idToken")
                conn.disconnect()

                // 2. Trigger email verification link
                if (idToken.isNotBlank()) {
                    val oobUrl = URL("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$webApiKey")
                    val oobConn = (oobUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    val oobPayload = JSONObject().apply {
                        put("requestType", "VERIFY_EMAIL")
                        put("idToken", idToken)
                    }
                    OutputStreamWriter(oobConn.outputStream).use { it.write(oobPayload.toString()); it.flush() }
                    oobConn.disconnect()
                }
            } else {
                conn.disconnect()
            }
        } catch(e: Exception) {
            // Ignore error, local verification code is always ready
        }
    }

    private fun firebaseAuthSignIn(email: String, pass: String): String? {
        if (webApiKey.isBlank()) return null
        return try {
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$webApiKey")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }
            val payload = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("returnSecureToken", true)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()); it.flush() }
            if (conn.responseCode in 200..299) {
                val res = conn.inputStream.bufferedReader().use { it.readText() }
                JSONObject(res).optString("localId")
            } else {
                null
            }
        } catch(e: Exception) {
            null
        }
    }

    suspend fun loginWithGoogle(email: String, displayName: String): Result<Cafe> = withContext(Dispatchers.IO) {
        try {
            fetchCafesFromFirebase()
        } catch (e: Exception) {}

        val cleanEmail = email.trim().lowercase()
        var found = _cafes.value.find { it.email.equals(cleanEmail, ignoreCase = true) }

        if (found == null) {
            // Automatically register new cafe for this Google account
            val derivedSlug = cleanEmail.substringBefore("@")
                .replace("[^a-zA-Z0-9-]".toRegex(), "")
                .ifBlank { "cafe-" + (System.currentTimeMillis() % 100000) }

            val cafeName = displayName.ifBlank { "كافيه ${cleanEmail.substringBefore("@")}" }
            val newCafe = Cafe(
                id = derivedSlug,
                name = cafeName,
                email = cleanEmail,
                password = "google_authenticated",
                status = "approved", // Quick Google login approved
                emailVerified = true,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            )
            _cafes.value = _cafes.value + newCafe
            try {
                syncCafeToFirebase(newCafe)
            } catch (e: Exception) {}
            found = newCafe
        }

        if (found.status.equals("suspended", ignoreCase = true)) {
            return@withContext Result.failure(Exception("SUSPENDED_ACCOUNT"))
        }

        if (!found.status.equals("approved", ignoreCase = true)) {
            return@withContext Result.failure(Exception("PENDING_APPROVAL"))
        }

        setCurrentCafe(found)
        Result.success(found)
    }

    private fun seedInitialData() {
        val bustanCafe = Cafe(
            id = "bustan-cafe",
            name = "كافيه البستان",
            email = "alikhlel132@gmail.com",
            password = "123",
            status = "approved",
            createdAt = "2026-09-01",
            phone = "+964 770 123 4567",
            welcomeMessage = "أهلاً وسهلاً بكم في كافيه البستان! مسح الباركود يتيح لكم استعراض قائمتنا والطلب مباشرة لطاولتكم ☕",
            currency = "د.ع",
            vatPercentage = 0.0,
            wifiSsid = "Bustan-VIP",
            wifiPassword = "CoffeeBustan2026",
            totalTables = 12
        )

        val jasmineCafe = Cafe(
            id = "jasmine-cafe",
            name = "مقهى الياسمين",
            email = "jasmine@cafe.com",
            password = "123",
            status = "pending",
            createdAt = "2026-09-25",
            phone = "+964 771 987 6543",
            welcomeMessage = "مرحباً بكم في مقهى الياسمين",
            currency = "د.ع",
            vatPercentage = 0.0,
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
            Category("cat_cold", cafe.id, "مشروبات باردة وعصائر", "Cold Drinks", "🧃", 2),
            Category("cat_sweets", cafe.id, "حلويات ومخبوزات", "Desserts", "🍰", 3),
            Category("cat_shisha", cafe.id, "أركيلة ومعسلات", "Shisha & Hookah", "💨", 4)
        )
        _categories.value = defaultCategories

        val defaultProducts = listOf(
            Product("p1", cafe.id, "cat_shisha", "أركيلة تفاحتين فاخر", "Double Apple Shisha", "رأس فخاري مجهز بأجود أنواع الفحم الطبيعي", 7000.0, true, "💨", 5),
            Product("p2", cafe.id, "cat_sweets", "تشيز كيك لوتس دافئ", "Warm Lotus Cheesecake", "طبقة بسكويت مقرمشة مع كريمة جبنة وصوص زبدة اللوتس", 5500.0, true, "🍰", 3),
            Product("p3", cafe.id, "cat_hot", "شاي عراقي مهيل بالاستكانة", "Iraqi Cardamom Tea", "شاي سيلاني فاخر مخدر على الفحم مع حبات الهيل", 1500.0, true, "🫖", 2),
            Product("p4", cafe.id, "cat_hot", "قهوة تركي بالهيل", "Turkish Coffee Cardamom", "قهوة تركية محمصة بعناية ومغلية على الرمل مع الهيل", 2750.0, true, "☕", 3),
            Product("p5", cafe.id, "cat_cold", "موهيتو بلوبيري منعش", "Blueberry Mojito", "توت أزرق منعش مع ليمون ونعناع وصودا مثلجة", 4000.0, true, "🫐", 3),
            Product("p6", cafe.id, "cat_cold", "عصير برتقال طبيعي فريش", "Fresh Orange Juice", "عصير برتقال طبيعي 100% معصور طازج بدون سكر مضاف", 3500.0, true, "🍊", 3),
            Product("p7", cafe.id, "cat_cold", "سبانش لاتيه مثلج", "Iced Spanish Latte", "إسبريسو غني مع حليب محلى وثلج منعش", 4500.0, true, "🧊", 3)
        )
        _products.value = defaultProducts

        // Tables
        val tableList = (1..cafe.totalTables).map { num ->
            TableInfo(
                tableNumber = num,
                status = if (num in listOf(1, 3)) TableStatus.OCCUPIED else TableStatus.AVAILABLE,
                activeOrderId = if (num == 1) "ord_101" else if (num == 3) "ord_102" else null
            )
        }
        _tables.value = tableList

        // Sample Orders
        val sampleOrders = listOf(
            Order(
                id = "ord_101",
                cafeId = cafe.id,
                tableNumber = 1,
                customerName = "علي",
                items = listOf(
                    OrderItem("p4", "قهوة تركي بالهيل", 2750.0, 1, "سكر مضبوط")
                ),
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 3,
                notes = "طاولة الصالة الداخلية",
                subtotal = 2750.0,
                vat = 0.0,
                total = 2750.0,
                isPaid = false
            ),
            Order(
                id = "ord_102",
                cafeId = cafe.id,
                tableNumber = 3,
                customerName = "حيدر",
                items = listOf(
                    OrderItem("p1", "أركيلة تفاحتين فاخر", 7000.0, 1),
                    OrderItem("p3", "شاي عراقي مهيل بالاستكانة", 1500.0, 1)
                ),
                status = OrderStatus.PREPARING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 15,
                notes = "مع تغيير الفحم فوراً",
                subtotal = 8500.0,
                vat = 0.0,
                total = 8500.0,
                isPaid = false
            )
        )
        _orders.value = sampleOrders
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

    suspend fun fetchCafesFromFirebase() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://firestore.googleapis.com/v1/projects/cafe-bons/databases/(default)/documents/cafes")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                if (rootJson.has("documents")) {
                    val docs = rootJson.getJSONArray("documents")
                    val fetchedList = mutableListOf<Cafe>()
                    for (i in 0 until docs.length()) {
                        val doc = docs.getJSONObject(i)
                        val fields = doc.optJSONObject("fields") ?: continue
                        val id = fields.optJSONObject("cafeId")?.optString("stringValue")
                            ?: fields.optJSONObject("id")?.optString("stringValue")
                            ?: doc.optString("name").substringAfterLast("/")
                        val name = fields.optJSONObject("cafeName")?.optString("stringValue")
                            ?: fields.optJSONObject("name")?.optString("stringValue")
                            ?: fields.optJSONObject("title")?.optString("stringValue")
                            ?: "كافيه"
                        var email = fields.optJSONObject("email")?.optString("stringValue")
                            ?: fields.optJSONObject("mail")?.optString("stringValue")
                            ?: fields.optJSONObject("user")?.optString("stringValue")
                            ?: ""
                        if (email.isBlank()) {
                            email = "$id@gmail.com"
                        }
                        val password = fields.optJSONObject("password")?.optString("stringValue")
                            ?: fields.optJSONObject("pass")?.optString("stringValue")
                            ?: "123456"
                        val status = fields.optJSONObject("status")?.optString("stringValue") ?: "approved"
                        val emailVerified = fields.optJSONObject("emailVerified")?.optBoolean("booleanValue") ?: true
                        val verificationCode = fields.optJSONObject("verificationCode")?.optString("stringValue") ?: ""
                        val createdAt = fields.optJSONObject("createdAt")?.optString("stringValue") ?: ""
                        val phone = fields.optJSONObject("phone")?.optString("stringValue") ?: ""

                        fetchedList.add(
                            Cafe(
                                id = id,
                                name = name,
                                email = email,
                                password = password,
                                status = status,
                                emailVerified = emailVerified,
                                verificationCode = verificationCode,
                                createdAt = createdAt,
                                phone = phone
                            )
                        )
                    }
                    if (fetchedList.isNotEmpty()) {
                        val fetchedIds = fetchedList.map { it.id }.toSet()
                        val unreplaced = _cafes.value.filter { it.id !in fetchedIds }
                        _cafes.value = fetchedList + unreplaced
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            // Ignore offline fallback
        }
    }

    private fun syncCafeToFirebase(cafe: Cafe) {
        val firestoreUrl = "https://firestore.googleapis.com/v1/projects/cafe-bons/databases/(default)/documents/cafes/${cafe.id}"
        val url = URL(firestoreUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            doOutput = true
            connectTimeout = 6000
            readTimeout = 6000
        }

        val jsonFields = JSONObject().apply {
            put("fields", JSONObject().apply {
                put("cafeName", JSONObject().put("stringValue", cafe.name))
                put("cafeId", JSONObject().put("stringValue", cafe.id))
                put("email", JSONObject().put("stringValue", cafe.email.ifBlank { "${cafe.id}@gmail.com" }))
                put("password", JSONObject().put("stringValue", cafe.password))
                put("status", JSONObject().put("stringValue", cafe.status))
                put("emailVerified", JSONObject().put("booleanValue", cafe.emailVerified))
                put("verificationCode", JSONObject().put("stringValue", cafe.verificationCode))
                put("createdAt", JSONObject().put("stringValue", cafe.createdAt))
                put("phone", JSONObject().put("stringValue", cafe.phone))
                put("plan", JSONObject().put("stringValue", "trial_14"))
                put("planName", JSONObject().put("stringValue", "تجريبي (14 يوم)"))
                val expCal = java.util.Calendar.getInstance()
                expCal.add(java.util.Calendar.DAY_OF_YEAR, 30)
                val expDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(expCal.time)
                put("expiryDate", JSONObject().put("stringValue", expDate))
            })
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(jsonFields.toString())
            writer.flush()
        }
        val responseCode = conn.responseCode
        conn.disconnect()
    }
}
