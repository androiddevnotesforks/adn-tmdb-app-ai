@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tmdbapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tmdbapp.R
import com.example.tmdbapp.models.Movie
import com.example.tmdbapp.viewmodel.MovieUiState
import com.example.tmdbapp.viewmodel.MovieViewModel
import com.example.tmdbapp.ui.components.MovieItem
import kotlinx.coroutines.flow.distinctUntilChanged
import com.example.tmdbapp.utils.Constants
import com.example.tmdbapp.ui.theme.ThemeMode

@Composable
fun MovieListScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onFavoritesClick: () -> Unit,
    screenTitle: String,
    viewType: String,
    onViewTypeChange: (String) -> Unit,
    onThemeChange: () -> Unit,
    currentThemeMode: ThemeMode
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridScrollPosition by viewModel.gridScrollPosition.collectAsState()
    val listScrollPosition by viewModel.listScrollPosition.collectAsState()
    
    val gridState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = gridScrollPosition.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = gridScrollPosition.firstVisibleItemScrollOffset
    )
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = listScrollPosition.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = listScrollPosition.firstVisibleItemScrollOffset
    )

    LaunchedEffect(gridState, viewType) {
        if (viewType == Constants.VIEW_TYPE_GRID) {
            snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
                .distinctUntilChanged()
                .collect { (index, offset) ->
                    viewModel.saveGridScrollPosition(index, offset)
                }
        }
    }

    LaunchedEffect(listState, viewType) {
        if (viewType == Constants.VIEW_TYPE_LIST) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .distinctUntilChanged()
                .collect { (index, offset) ->
                    viewModel.saveListScrollPosition(index, offset)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = {
                        onViewTypeChange(if (viewType == Constants.VIEW_TYPE_GRID) Constants.VIEW_TYPE_LIST else Constants.VIEW_TYPE_GRID)
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (viewType == Constants.VIEW_TYPE_GRID) 
                                    R.drawable.view_list_24px 
                                else 
                                    R.drawable.grid_view_24px
                            ),
                            contentDescription = Constants.CONTENT_DESC_SWITCH_VIEW,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = Constants.CONTENT_DESC_FAVORITES)
                    }
                    IconButton(onClick = onThemeChange) {
                        Icon(
                            painter = painterResource(
                                id = when (currentThemeMode) {
                                    ThemeMode.LIGHT -> R.drawable.dark_mode_24px
                                    ThemeMode.DARK -> R.drawable.light_mode_24px
                                    ThemeMode.SYSTEM -> R.drawable.contrast_24px
                                }
                            ),
                            contentDescription = "Toggle theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is MovieUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MovieUiState.Success -> {
                    val movies = (uiState as MovieUiState.Success).movies
                    when (viewType) {
                        Constants.VIEW_TYPE_GRID -> {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Adaptive(150.dp),
                                state = gridState,
                                contentPadding = PaddingValues(Constants.PADDING_MEDIUM),
                                horizontalArrangement = Arrangement.spacedBy(Constants.PADDING_MEDIUM),
                                verticalItemSpacing = Constants.PADDING_MEDIUM
                            ) {
                                itemsIndexed(movies) { index, movie ->
                                    if (index >= movies.size - 1) {
                                        viewModel.loadMoreMovies()
                                    }
                                    MovieItem(
                                        movie = movie,
                                        modifier = Modifier.clickable {
                                            viewModel.selectMovie(movie)
                                            onMovieClick(movie)
                                        },
                                        onFavoriteClick = { viewModel.toggleFavorite(movie) },
                                        isListView = false
                                    )
                                }
                            }
                        }
                        Constants.VIEW_TYPE_LIST -> {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(Constants.PADDING_MEDIUM),
                                verticalArrangement = Arrangement.spacedBy(Constants.PADDING_MEDIUM)
                            ) {
                                items(movies) { movie ->
                                    MovieItem(
                                        movie = movie,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectMovie(movie)
                                                onMovieClick(movie)
                                            },
                                        onFavoriteClick = { viewModel.toggleFavorite(movie) },
                                        isListView = true
                                    )
                                }
                                item {
                                    if (movies.isNotEmpty()) {
                                        viewModel.loadMoreMovies()
                                    }
                                }
                            }
                        }
                    }
                }
                is MovieUiState.Error -> {
                    val error = (uiState as MovieUiState.Error).error
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = error.message, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM))
                            Button(onClick = { viewModel.loadMoreMovies() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}