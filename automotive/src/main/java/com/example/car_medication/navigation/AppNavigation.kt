package com.example.phone_medicatios.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phone_medicatios.screen.DashboardScreen
import com.example.phone_medicatios.screen.ReminderFormScreen
import com.example.phone_medicatios.screen.ReminderScheduleScreen
import com.example.phone_medicatios.screen.ReminderProgramScreen
import com.example.phone_medicatios.screen.SplashScreen
import com.example.phone_medicatios.screen.HistoryScreen
import com.example.phone_medicatios.screen.MedicationsScreen
import com.example.phone_medicatios.screen.RemindersScreen
import com.example.phone_medicatios.viewmodel.ReminderViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object ReminderForm : Screen("reminder_form")
    object ReminderSchedule : Screen("reminder_schedule")
    object ReminderProgram : Screen("reminder_program")
    object History : Screen("history")
    object Medications : Screen("medications")
    object Reminders : Screen("reminders")
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: ReminderViewModel) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController, viewModel) }
        composable(Screen.ReminderForm.route) { backStackEntry ->
            ReminderFormScreen(navController, viewModel, backStackEntry)
        }
        composable(Screen.ReminderSchedule.route) { ReminderScheduleScreen(navController, viewModel) }
        composable(Screen.ReminderProgram.route) { ReminderProgramScreen(navController, viewModel) }
        composable(Screen.History.route) { HistoryScreen(navController, viewModel) }
        composable(Screen.Medications.route) { MedicationsScreen(navController, viewModel) }
        composable(Screen.Reminders.route) {
            RemindersScreen(navController, viewModel)
        }
    }
}
