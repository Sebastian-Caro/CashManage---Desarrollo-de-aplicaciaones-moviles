package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.DashboardHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.CashManagerTheme
import com.example.ui.theme.ForestBackground
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CashManagerViewModel
import com.example.ui.viewmodel.CashManagerViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: CashManagerViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val prefs = applicationContext.getSharedPreferences("cash_manager_session", Context.MODE_PRIVATE)
        val repository = AppRepository(database.appDao(), prefs)
        CashManagerViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CashManagerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding(),
                    color = ForestBackground
                ) {
                    CashManagerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CashManagerApp(viewModel: CashManagerViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Hardware Back Button handling
    BackHandler(enabled = currentScreen != AppScreen.LOGIN && currentScreen != AppScreen.HOME) {
        val handled = viewModel.navigateBack()
        if (!handled) {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Crossfade(targetState = currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
            AppScreen.HUB -> DashboardHubScreen(viewModel = viewModel)
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.ADD_EXPENSE -> AddExpenseScreen(viewModel = viewModel)
            AppScreen.AGENDA -> AgendaScreen(viewModel = viewModel)
            AppScreen.ACCOUNT -> AccountScreen(viewModel = viewModel)
            AppScreen.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
        }
    }
}
