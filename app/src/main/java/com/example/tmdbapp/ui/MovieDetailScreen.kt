package com.example.tmdbapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.example.tmdbapp.models.Movie
import com.example.tmdbapp.ui.components.*
import com.example.tmdbapp.viewmodel.*
import com.example.tmdbapp.viewmodel.AIResponseState.Loading
import com.example.tmdbapp.viewmodel.MovieDetailState.Error

@Composable
fun MovieDetailScreen(
  viewModel: MovieViewModel,
  onBackPress: () -> Unit,
) {
  val movieState by viewModel.movieDetailState.collectAsState()
  val aiResponse by viewModel.aiResponse.collectAsState()
  val aiResponseState by viewModel.aiResponseState.collectAsState()

  DisposableEffect(Unit) {
    onDispose {
      viewModel.clearAIResponse()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    when (movieState) {
      is MovieDetailState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is MovieDetailState.Success -> {
        val movie = (movieState as MovieDetailState.Success<Movie>).data
        MovieDetailContent(
          movie = movie,
          onBackPress = onBackPress,
          onFavoriteClick = { viewModel.toggleFavorite(movie) },
          onDownloadClick = viewModel::downloadImage,
          onAskAIClick = { viewModel.askAIAboutMovie(movie) },
          aiResponse = aiResponse,
          aiResponseState = aiResponseState,
        )
      }
      is MovieDetailState.Error -> {
        ErrorContent(
          error = (movieState as MovieDetailState.Error).error,
          onRetry = { viewModel.retryFetchMovieDetails() },
          onBackPress = onBackPress,
        )
      }
    }

    ShimmeringOverlay(
      isVisible = aiResponseState == Loading,
    )

    if (aiResponseState == Loading) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter,
      ) {
        AIScanningIndicator()
      }
    }
  }
}
