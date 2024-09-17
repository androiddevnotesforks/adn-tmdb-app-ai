package com.example.tmdbapp.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tmdbapp.ui.theme.ThemeMode
import com.example.tmdbapp.utils.Constants
import com.example.tmdbapp.viewmodel.MovieViewModel

@Composable
fun NavGraph(
  navController: NavHostController,
  movieViewModel: MovieViewModel,
  currentThemeMode: ThemeMode,
  onThemeChange: () -> Unit,
  viewType: String,
  onViewTypeChange: (String) -> Unit,
  application: Application,
) {
  NavHost(navController = navController, startDestination = "movieList") {
    composable("movieList") {
      MovieListScreen(
        viewModel = movieViewModel,
        onMovieClick = { movie ->
          navController.navigate("movieDetail/${movie.id}")
        },
        onFavoritesClick = {
          navController.navigate("favorites") {
            popUpTo(navController.graph.findStartDestination().id) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        },
        screenTitle = Constants.SCREEN_TITLE_DISCOVER,
        viewType = viewType,
        onViewTypeChange = onViewTypeChange,
        onThemeChange = onThemeChange,
        currentThemeMode = currentThemeMode,
        onCreateListClick = {
          navController.navigate("createList") {
            popUpTo(navController.graph.findStartDestination().id) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        },
      )
    }
    composable(
      "movieDetail/{movieId}",
      arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
    ) { backStackEntry ->
      val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
      LaunchedEffect(movieId) {
        movieViewModel.fetchMovieDetails(movieId)
      }
      MovieDetailScreen(
        viewModel = movieViewModel,
        onBackPress = { navController.popBackStack() },
      )
    }
    composable("favorites") {
      FavoritesScreen(
        viewModel = movieViewModel,
        onMovieClick = { movieId ->
          navController.navigate("movieDetail/$movieId")
        },
        onBackPress = { navController.popBackStack() },
      )
    }
    composable("createList") {
      ListCreationScreen(
        viewModel = movieViewModel,
        onNavigateBack = { navController.popBackStack() },
        application = application,
      )
    }
  }
}
