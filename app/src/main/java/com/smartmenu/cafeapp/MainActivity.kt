package com.smartmenu.cafeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.smartmenu.cafeapp.ui.screens.*
import com.smartmenu.cafeapp.ui.theme.CafeBonsTheme
import com.smartmenu.cafeapp.ui.viewmodel.MainViewModel
import com.smartmenu.cafeapp.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CafeBonsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigationHost(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != Screen.AUTH && currentScreen != Screen.DASHBOARD) {
        viewModel.navigateBack()
    }

    when (currentScreen) {
        Screen.AUTH -> AuthScreen(viewModel = viewModel)
        Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
        Screen.CATEGORIES_PRODUCTS -> CategoriesProductsScreen(viewModel = viewModel)
        Screen.TABLES_QR -> TablesQrScreen(viewModel = viewModel)
        Screen.ORDERS -> OrdersScreen(viewModel = viewModel)
        Screen.SETTINGS -> CafeSettingsScreen(viewModel = viewModel)
        Screen.CUSTOMER_PREVIEW -> CustomerMenuScreen(viewModel = viewModel)
        Screen.SUPER_ADMIN -> SuperAdminScreen(viewModel = viewModel)
    }
}
