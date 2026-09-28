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

    fun login(cafeId: String, pass: String) {
        if (cafeId.isBlank() || pass.isBlank()) {
            _authError.value = "الرجاء إدخال المعرف وكلمة المرور"
            return
        }
        viewModelScope.launch {
            val result = repository.loginCafe(cafeId, pass)
            result.onSuccess {
                _authError.value = null
                _isPendingView.value = false
                _currentScreen.value = Screen.DASHBOARD
            }.onFailure { err ->
                if (err.message == "PENDING_APPROVAL") {
                    _isPendingView.value = true
                    _authError.value = null
                } else {
                    _authError.value = err.message ?: "خطأ في تسجيل الدخول"
                }
            }
        }
    }

    fun register(name: String, slug: String, pass: String) {
        if (name.isBlank() || slug.isBlank() || pass.isBlank()) {
            _authError.value = "الرجاء تعبئة كافة الحقول المطلوبة"
            return
        }
        viewModelScope.launch {
            val result = repository.registerCafe(name, slug, pass)
            result.onSuccess {
                _authError.value = null
                _isPendingView.value = true
            }.onFailure {
                _authError.value = "خطأ أثناء التسجيل: ${it.message}"
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

    fun setCustomerTableNumber(num: Int) {
        _customerTableNumber.value = num
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
