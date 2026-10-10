package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        val repository = AppRepository(database.appDao())
        CashManagerViewModelFactory(
            repository,
            com.example.notifications.AndroidClassReminderScheduler(applicationContext)
        )
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        val message = toastMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearToast()
    }

    // Hardware Back Button handling
    BackHandler(enabled = currentScreen != AppScreen.LOGIN && currentScreen != AppScreen.HOME) {
        val handled = viewModel.navigateBack()
        if (!handled) {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 76.dp)
        )
    }
}
