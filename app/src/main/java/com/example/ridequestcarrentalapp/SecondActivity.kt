package com.example.ridequestcarrentalapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ridequestcarrentalapp.data.repository.AuthRepository
import com.example.ridequestcarrentalapp.ui.feature.onbound.*
import com.example.ridequestcarrentalapp.ui.login.LoginScreenFirebase
import com.example.ridequestcarrentalapp.ui.login.SignUpScreenFirebase
import com.example.ridequestcarrentalapp.ui.theme.RideQuestCarRentalAppTheme
import kotlinx.coroutines.launch

class SecondActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository = (application as RideQuestApplication).authRepository

        // Check if already logged in
        if (authRepository.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            RideQuestCarRentalAppTheme {
                SecondActivityContent(
                    authRepository = authRepository,
                    onLoginSuccess = { isAdmin ->
                        if (isAdmin) {
                            startActivity(Intent(this, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SecondActivityContent(
    authRepository: AuthRepository,
    onLoginSuccess: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "onboard"
    ) {
        // Onboarding Screens
        composable("onboard") {
            OnBoundScreen { navController.navigate("onboard1") }
        }
        composable("onboard1") {
            OnBoundScreen1 { navController.navigate("onboard2") }
        }
        composable("onboard2") {
            OnBoundScreen2 { navController.navigate("onboard3") }
        }
        composable("onboard3") {
            OnBoundScreen3 { navController.navigate("onboard4") }
        }
        composable("onboard4") {
            OnBoundScreen4 { navController.navigate("login") }
        }

        // Login Screen
        composable("login") {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            LoginScreenFirebase(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClick = { email, password ->
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        // Check if admin login
                        if (email == AuthRepository.ADMIN_EMAIL) {
                            authRepository.signInAdmin(email, password)
                                .onSuccess { onLoginSuccess(true) }
                                .onFailure { errorMessage = it.message }
                        } else {
                            authRepository.signIn(email, password)
                                .onSuccess { onLoginSuccess(false) }
                                .onFailure { errorMessage = it.message }
                        }
                        isLoading = false
                    }
                },
                onSignUpClick = { navController.navigate("signup") },
                onForgotPasswordClick = { /* TODO */ },
                onAdminLoginClick = { navController.navigate("admin_login") }
            )
        }

        // Sign Up Screen
        composable("signup") {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            SignUpScreenFirebase(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onSignUpClick = { firstName, lastName, email, phone, password ->
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signUp(email, password, firstName, lastName, phone)
                            .onSuccess { onLoginSuccess(false) }
                            .onFailure { errorMessage = it.message }
                        isLoading = false
                    }
                },
                onLoginClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Admin Login
        composable("admin_login") {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            AdminLoginScreen(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClick = { email, password ->
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.signInAdmin(email, password)
                            .onSuccess { onLoginSuccess(true) }
                            .onFailure { errorMessage = it.message }
                        isLoading = false
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}