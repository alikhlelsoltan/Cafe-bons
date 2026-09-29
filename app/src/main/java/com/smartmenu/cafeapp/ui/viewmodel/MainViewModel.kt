package com.smartmenu.cafeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartmenu.cafeapp.data.model.*
import com.smartmenu.cafeapp.data.repository.CafeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Screen {
    AUTH,
    DASHBOARD,
    CATEGORIES_PRODUCTS,
    TABLES_QR,
    ORDERS,
    SETTINGS,
    CUSTOMER_PREVIEW,
    SUPER_ADMIN
}

enum class AuthTab {
    LOGIN,
    REGISTER
}

class MainViewModel(
    val repository: CafeRepository = CafeRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(Screen.AUTH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf<Screen>()

    private val _authTab = MutableStateFlow(AuthTab.LOGIN)
    val authTab: StateFlow<AuthTab> = _authTab.asStateFlow()

    private val _isPendingView = MutableStateFlow(false)
    val isPendingView: StateFlow<Boolean> = _isPendingView.asStateFlow()

    private val _isVerifyingEmail = MutableStateFlow(false)
    val isVerifyingEmail: StateFlow<Boolean> = _isVerifyingEmail.asStateFlow()

    private val _generatedVerificationCode = MutableStateFlow("")
    val generatedVerificationCode: StateFlow<String> = _generatedVerificationCode.asStateFlow()

    private val _lastAttemptEmail = MutableStateFlow("alikhlel132@gmail.com")
    val lastAttemptEmail: StateFlow<String> = _lastAttemptEmail.asStateFlow()

    private val _lastRegisteredCafeId = MutableStateFlow("")
    val lastRegisteredCafeId: StateFlow<String> = _lastRegisteredCafeId.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _selectedTableForQr = MutableStateFlow<Int?>(null)
    val selectedTableForQr: StateFlow<Int?> = _selectedTableForQr.asStateFlow()

    private val _selectedOrderForBon = MutableStateFlow<Order?>(null)
    val selectedOrderForBon: StateFlow<Order?> = _selectedOrderForBon.asStateFlow()

    // Customer Menu Preview states
    private val _customerTableNumber = MutableStateFlow(1)
    val customerTableNumber: StateFlow<Int> = _customerTableNumber.asStateFlow()

    private val _customerCart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val customerCart: StateFlow<Map<Product, Int>> = _customerCart.asStateFlow()

    private val _customerNotes = MutableStateFlow("")
    val customerNotes: StateFlow<String> = _customerNotes.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenStack.isNotEmpty()) {
            _currentScreen.value = _screenStack.removeAt(_screenStack.size - 1)
            return true
        } else if (_currentScreen.value != Screen.DASHBOARD && _currentScreen.value != Screen.AUTH) {
            _currentScreen.value = if (repository.currentCafe.value != null) Screen.DASHBOARD else Screen.AUTH
            return true
        }
        return false
    }

    fun setAuthTab(tab: AuthTab) {
        _authTab.value = tab
        _authError.value = null
    }

    fun showPendingView(show: Boolean) {
        _isPendingView.value = show
    }

    fun login(emailOrId: String, pass: String) {
        if (emailOrId.isBlank() || pass.isBlank()) {
            _authError.value = "الرجاء إدخال البريد الإلكتروني وكلمة المرور"
            return
        }
        _lastAttemptEmail.value = emailOrId.trim()
        viewModelScope.launch {
            val result = repository.loginCafe(emailOrId, pass)
            result.onSuccess {
                _authError.value = null
                _isPendingView.value = false
                _isVerifyingEmail.value = false
                _currentScreen.value = Screen.DASHBOARD
            }.onFailure { err ->
                if (err.message == "PENDING_APPROVAL") {
                    _isPendingView.value = true
                    _authError.value = null
                } else if (err.message == "SUSPENDED_ACCOUNT") {
                    _authError.value = "⛔ اشتراك هذا الكافيه موقوف أو منتهي! يرجى مراجعة إدارة المنظومة عبر لوحة المطور للتجديد."
                } else {
                    _authError.value = err.message ?: "خطأ في تسجيل الدخول"
                }
            }
        }
    }

    fun loginWithGoogle(email: String = "alikhlel132@gmail.com", displayName: String = "كافيه البستان") {
        _lastAttemptEmail.value = email.trim()
        viewModelScope.launch {
            val result = repository.loginWithGoogle(email, displayName)
            result.onSuccess {
                _authError.value = null
                _isPendingView.value = false
                _isVerifyingEmail.value = false
                _currentScreen.value = Screen.DASHBOARD
                _toastMessage.value = "مرحباً بك! تم تسجيل الدخول بنجاح عبر حساب Google ($email)"
            }.onFailure { err ->
                if (err.message == "PENDING_APPROVAL") {
                    _isPendingView.value = true
                    _authError.value = null
                } else if (err.message == "SUSPENDED_ACCOUNT") {
                    _authError.value = "⛔ اشتراك هذا الكافيه موقوف أو منتهي! يرجى التواصل مع إدارة النظام للتجديد."
                } else {
                    _authError.value = err.message ?: "فشل تسجيل الدخول عبر Google"
                }
            }
        }
    }

    fun register(
        name: String,
        whatsappPhone: String,
        email: String,
        pass: String,
        confirmPass: String
    ) {
        if (name.isBlank()) {
            _authError.value = "الرجاء إدخال اسم الكافيه"
            return
        }
        if (whatsappPhone.isBlank()) {
            _authError.value = "الرجاء إدخال رقم الواتساب للتواصل وتفعيل الحساب"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "الرجاء إدخال بريد إلكتروني صحيح"
            return
        }
        if (pass.isBlank() || pass.length < 6) {
            _authError.value = "يجب أن تتكون كلمة السر من 6 خانات أو أكثر"
            return
        }
        if (pass != confirmPass) {
            _authError.value = "كلمتا السر غير متطابقتين! يرجى إعادة كتابة كلمة السر للتأكيد"
            return
        }

        _lastAttemptEmail.value = email.trim()
        viewModelScope.launch {
            val result = repository.registerCafe(name, email, pass, whatsappPhone)
            result.onSuccess { cafe ->
                _authError.value = null
                _lastRegisteredCafeId.value = cafe.id
                _isVerifyingEmail.value = false
                _isPendingView.value = true
                _toastMessage.value = "تم إرسال طلبك بنجاح! سيتم تفعيل حسابك بأقرب وقت."
            }.onFailure {
                _authError.value = "خطأ أثناء التسجيل: ${it.message}"
            }
        }
    }

    fun switchToLoginWithEmail(email: String) {
        _lastAttemptEmail.value = email.trim()
        _isPendingView.value = false
        _isVerifyingEmail.value = false
        _authTab.value = AuthTab.LOGIN
    }

    fun confirmEmail(code: String) {
        val cafeIdOrEmail = _lastRegisteredCafeId.value.ifBlank { _lastAttemptEmail.value }
        viewModelScope.launch {
            val res = repository.verifyEmailCode(cafeIdOrEmail, code)
            res.onSuccess {
                _authError.value = null
                _isVerifyingEmail.value = false
                _isPendingView.value = true
                _toastMessage.value = "تم تأكيد ملكية البريد الإلكتروني بنجاح! طلبك الآن بانتظار اعتماد المطور."
            }.onFailure {
                _authError.value = it.message ?: "رمز التحقق غير صحيح"
            }
        }
    }

    fun skipEmailVerificationToPending() {
        _isVerifyingEmail.value = false
        _isPendingView.value = true
    }

    fun checkPendingApproval() {
        viewModelScope.launch {
            repository.fetchCafesFromFirebase()
            val query = _lastAttemptEmail.value.lowercase().trim()
            val cafeId = _lastRegisteredCafeId.value.lowercase().trim()

            val found = repository.cafes.value.find { cafe ->
                val e = cafe.email.lowercase().trim()
                val id = cafe.id.lowercase().trim()
                (query.isNotEmpty() && (e == query || e.substringBefore("@") == query.substringBefore("@") || id == query)) ||
                (cafeId.isNotEmpty() && id == cafeId)
            } ?: repository.cafes.value.find { it.status.equals("approved", ignoreCase = true) }

            if (found != null && found.status.equals("approved", ignoreCase = true)) {
                repository.setCurrentCafe(found)
                _isPendingView.value = false
                _isVerifyingEmail.value = false
                _authError.value = null
                _currentScreen.value = Screen.DASHBOARD
                _toastMessage.value = "تهانينا! تم اعتماد وتفعيل حسابك (${found.name}) بنجاح."
            } else if (found != null && found.status.equals("suspended", ignoreCase = true)) {
                _authError.value = "⛔ هذا الحساب موقوف أو معلق من إدارة النظام."
            } else {
                _toastMessage.value = "الطلب ما زال قيد المراجعة في لوحة تحكم المطور. اضغط فحص مجدداً بعد الموافقة."
            }
        }
    }

    fun logout() {
        repository.setCurrentCafe(null)
        _screenStack.clear()
        _isPendingView.value = false
        _currentScreen.value = Screen.AUTH
    }

    fun setSelectedTableForQr(tableNum: Int?) {
        _selectedTableForQr.value = tableNum
    }

    fun setSelectedOrderForBon(order: Order?) {
        _selectedOrderForBon.value = order
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        repository.updateOrderStatus(orderId, newStatus)
    }

    fun setCustomerTableNumber(num: Int) {
        _customerTableNumber.value = num
    }

    fun quickOrderProduct(product: Product, tableNumber: Int) {
        val item = OrderItem(
            productId = product.id,
            nameArabic = product.nameArabic,
            price = product.price,
            quantity = 1
        )
        repository.placeCustomerOrder(
            tableNumber = tableNumber,
            items = listOf(item),
            notes = ""
        )
        _toastMessage.value = "تم إرسال (+ اوردر) ${product.nameArabic} لطاولة #$tableNumber بنجاح!"
    }

    fun setCustomerNotes(notes: String) {
        _customerNotes.value = notes
    }

    fun addToCart(product: Product) {
        val current = _customerCart.value.toMutableMap()
        current[product] = (current[product] ?: 0) + 1
        _customerCart.value = current
    }

    fun removeFromCart(product: Product) {
        val current = _customerCart.value.toMutableMap()
        val count = current[product] ?: 0
        if (count > 1) {
            current[product] = count - 1
        } else {
            current.remove(product)
        }
        _customerCart.value = current
    }

    fun clearCart() {
        _customerCart.value = emptyMap()
        _customerNotes.value = ""
    }

    fun submitCustomerOrder(): Order? {
        val cart = _customerCart.value
        if (cart.isEmpty()) return null

        val orderItems = cart.map { (prod, qty) ->
            OrderItem(
                productId = prod.id,
                nameArabic = prod.nameArabic,
                price = prod.price,
                quantity = qty
            )
        }

        val order = repository.placeCustomerOrder(
            tableNumber = _customerTableNumber.value,
            items = orderItems,
            notes = _customerNotes.value
        )

        clearCart()
        _toastMessage.value = "تم إرسال الطلب بنجاح إلى المطبخ والكاشير!"
        return order
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun approveCafe(cafeId: String) {
        repository.updateCafeStatus(cafeId, "approved")
        _toastMessage.value = "تمت الموافقة على الكافيه وتفعيل لوحة التحكم!"
    }

    fun rejectCafe(cafeId: String) {
        repository.updateCafeStatus(cafeId, "rejected")
        _toastMessage.value = "تم رفض الكافيه."
    }
}
