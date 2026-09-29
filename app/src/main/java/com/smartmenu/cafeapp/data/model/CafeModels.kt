package com.smartmenu.cafeapp.data.model

data class Cafe(
    val id: String,
    val name: String,
    val email: String = "",
    val password: String = "",
    val status: String = "approved", // "pending", "approved", "rejected", "suspended"
    val emailVerified: Boolean = true,
    val verificationCode: String = "",
    val createdAt: String = "",
    val phone: String = "+966 50 123 4567",
    val welcomeMessage: String = "أهلاً وسهلاً بكم في كافيهنا! نتمنى لكم وقتاً ممتعاً",
    val currency: String = "د.ع",
    val vatPercentage: Double = 0.0,
    val servicePercentage: Double = 0.0,
    val wifiSsid: String = "Cafe-Guest",
    val wifiPassword: String = "Cafe2026",
    val totalTables: Int = 12,
    val subscriptionPlan: String = "سنوي VIP",
    val subscriptionExpiry: String = "2027-09-01",
    val subscriptionStatus: String = "active" // "active", "trial", "expired", "suspended"
)

data class Category(
    val id: String,
    val cafeId: String,
    val nameArabic: String,
    val nameEnglish: String,
    val iconEmoji: String,
    val displayOrder: Int = 0
)

data class Product(
    val id: String,
    val cafeId: String,
    val categoryId: String,
    val nameArabic: String,
    val nameEnglish: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean = true,
    val iconEmoji: String = "☕",
    val prepTimeMinutes: Int = 5
)

enum class OrderStatus(val titleAr: String, val badgeColorHex: Long) {
    PENDING("جديد", 0xFFFF8800),
    PREPARING("قيد التحضير", 0xFF33B5E5),
    READY("جاهز للتقديم", 0xFF00C851),
    COMPLETED("مكتمل وتم الحساب", 0xFF707070),
    CANCELLED("ملغي", 0xFFD9534F)
}

data class OrderItem(
    val productId: String,
    val nameArabic: String,
    val price: Double,
    val quantity: Int,
    val notes: String = ""
)

data class Order(
    val id: String,
    val cafeId: String,
    val tableNumber: Int,
    val customerName: String = "علي",
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val subtotal: Double = 0.0,
    val vat: Double = 0.0,
    val total: Double = 0.0,
    val isPaid: Boolean = false
)

enum class TableStatus(val titleAr: String) {
    AVAILABLE("فارغة"),
    OCCUPIED("مشغولة"),
    BILL_REQUESTED("طلب الحساب")
}

data class TableInfo(
    val tableNumber: Int,
    val status: TableStatus = TableStatus.AVAILABLE,
    val activeOrderId: String? = null
)
