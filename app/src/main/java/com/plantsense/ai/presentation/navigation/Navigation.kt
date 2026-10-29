package com.plantsense.ai.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.plantsense.ai.presentation.camera.CameraRoute
import com.plantsense.ai.presentation.disease.DiseaseRoute
import com.plantsense.ai.presentation.identification.IdentificationRoute
import com.plantsense.ai.presentation.history.HistoryRoute
import com.plantsense.ai.presentation.profile.ProfileRoute
import com.plantsense.ai.presentation.home.HomeRoute
import com.plantsense.ai.presentation.splash.SplashScreen

private fun isBottomBarRoute(destination: NavDestination?): Boolean {
    return destination?.hasRoute<AppRoute.Home>() == true ||
           destination?.hasRoute<AppRoute.History>() == true ||
           destination?.hasRoute<AppRoute.Profile>() == true
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selectedRoute = when {
        currentDestination?.hasRoute<AppRoute.Home>() == true -> AppRoute.Home
        currentDestination?.hasRoute<AppRoute.History>() == true -> AppRoute.History
        currentDestination?.hasRoute<AppRoute.Profile>() == true -> AppRoute.Profile
        else -> null
    }

    val bottomBar: @Composable () -> Unit = {
        BottomNavigationBar(
            selectedRoute = selectedRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash,
        enterTransition = {
            if (isBottomBarRoute(initialState.destination) && isBottomBarRoute(targetState.destination)) {
                EnterTransition.None
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = EaseInOutSine)
                ) + fadeIn(animationSpec = tween(400))
            }
        },
        exitTransition = {
            if (isBottomBarRoute(initialState.destination) && isBottomBarRoute(targetState.destination)) {
                ExitTransition.None
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400, easing = EaseInOutSine)
                ) + fadeOut(animationSpec = tween(400))
            }
        },
        popEnterTransition = {
            if (isBottomBarRoute(initialState.destination) && isBottomBarRoute(targetState.destination)) {
                EnterTransition.None
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = EaseInOutSine)
                ) + fadeIn(animationSpec = tween(400))
            }
        },
        popExitTransition = {
            if (isBottomBarRoute(initialState.destination) && isBottomBarRoute(targetState.destination)) {
                ExitTransition.None
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400, easing = EaseInOutSine)
                ) + fadeOut(animationSpec = tween(400))
            }
        }
    ) {
        composable<AppRoute.Splash> {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable<AppRoute.Home> {
            HomeRoute(
                onNavigateToCamera = { navController.navigate(AppRoute.Camera) },
                onNavigateToHistory = { navController.navigate(AppRoute.History) },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile) },
                onNavigateToIdentify = { path, id -> 
                    navController.navigate(AppRoute.Identify(path = path, historyId = id)) 
                },
                onNavigateToDisease = { path, id -> 
                    navController.navigate(AppRoute.Disease(path = path, historyId = id)) 
                },
                bottomBar = bottomBar
            )
        }
        composable<AppRoute.Camera> {
            CameraRoute(
                onNavigateToHistory = { navController.navigate(AppRoute.History) },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile) },
                onNavigateToIdentify = { path ->
                    navController.navigate(AppRoute.Identify(path = path, historyId = -1)) {
                        popUpTo(AppRoute.Camera) { inclusive = true }
                    }
                },
                onNavigateToDisease = { path ->
                    navController.navigate(AppRoute.Disease(path = path, historyId = -1)) {
                        popUpTo(AppRoute.Camera) { inclusive = true }
                    }
                }
            )
        }
        composable<AppRoute.Identify> { backStackEntry ->
            val key: AppRoute.Identify = backStackEntry.toRoute()
            IdentificationRoute(
                imagePath = key.path,
                historyId = key.historyId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppRoute.Disease> { backStackEntry ->
            val key: AppRoute.Disease = backStackEntry.toRoute()
            DiseaseRoute(
                imagePath = key.path,
                historyId = key.historyId,
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppRoute.History> {
            HistoryRoute(
                onNavigateToHome = { navController.navigate(AppRoute.Home) },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile) },
                onNavigateToIdentify = { path, id -> 
                    navController.navigate(AppRoute.Identify(path = path, historyId = id)) 
                },
                onNavigateToDisease = { path, id -> 
                    navController.navigate(AppRoute.Disease(path = path, historyId = id)) 
                },
                onBack = { navController.popBackStack() },
                bottomBar = bottomBar
            )
        }
        composable<AppRoute.Profile> {
            ProfileRoute(
                onNavigateToHome = { navController.navigate(AppRoute.Home) },
                onNavigateToHistory = { navController.navigate(AppRoute.History) },
                onBack = { navController.popBackStack() },
                bottomBar = bottomBar
            )
        }
    }
}
