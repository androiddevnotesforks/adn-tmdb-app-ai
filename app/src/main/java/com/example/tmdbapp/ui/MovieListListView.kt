package com.example.tmdbapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.example.tmdbapp.models.*
import com.example.tmdbapp.ui.components.*
import com.example.tmdbapp.utils.*
import com.example.tmdbapp.viewmodel.*

@Composable
fun MovieListListView(
  movies: List<Movie>,
  viewModel: MovieViewModel,
  onMovieClick: (Movie) -> Unit,
  viewType: String,
  searchQuery: String,
  listState: LazyListState,
) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    state = listState,
  ) {
    itemsIndexed(
      items = movies,
      key = { index, movie -> "${movie.id}_$index" },
    ) { index, movie ->
      if (index >= movies.size - 1 && !viewModel.isLastPage) {
        viewModel.loadMoreMovies()
      }
      SimpleItemUi(
        title = movie.title,
        overview = movie.overview,
        posterPath = movie.posterPath,
        voteAverage = movie.voteAverage,
        isFavorite = movie.isFavorite,
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable {
              viewModel.setLastViewedItemIndex(index)
              onMovieClick(movie)
            },
        onFavoriteClick = {
          viewModel.toggleFavorite(movie)
        },
      )
    }
  }
}
