package mx.tec.sabores.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.tec.sabores.ui.screens.MyReviewsScreen
import mx.tec.sabores.ui.screens.NewReviewScreen
import mx.tec.sabores.ui.screens.RestaurantDetailScreen
import mx.tec.sabores.domain.Review
import mx.tec.sabores.ui.components.ErrorView
import mx.tec.sabores.ui.components.StarPicker
import mx.tec.sabores.ui.screens.RestaurantListScreen
import mx.tec.sabores.ui.state.NewReviewViewModel
import mx.tec.sabores.ui.state.SaboresViewModel
import mx.tec.sabores.ui.state.UiState

@Composable
fun SaboresApp() {
    val nav = rememberNavController()
    val viewModel: SaboresViewModel = viewModel()

    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = MenuItem.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MenuItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo(Route.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Route.HOME,
            modifier = Modifier.padding(padding)
        ) {

            composable(Route.HOME) {
                RestaurantListScreen(
                    estado = viewModel.restaurantes,
                    onReintentar = { viewModel.cargarRestaurantes() },
                    onRestaurantClick = { id -> nav.navigate(Route.detail(id)) }
                )
            }

            composable(Route.MY_REVIEWS) {
                LaunchedEffect(Unit) { viewModel.cargarMisResenas() }

                var enEdicion by remember { mutableStateOf<Review?>(null) }

                MyReviewsScreen(
                    estado = viewModel.misResenas,
                    nombreDe = { id -> viewModel.restaurantById(id)?.name ?: "Restaurante #$id" },
                    onReintentar = { viewModel.cargarMisResenas() },
                    onEdit = { enEdicion = it },
                    onDelete = { review -> viewModel.borrarResena(review.id) }
                )

                val resena = enEdicion
                if (resena != null) {
                    EditarResenaDialog(
                        resena = resena,
                        onGuardar = { stars, comment ->
                            viewModel.editarResena(resena.id, stars, comment) { enEdicion = null }
                        },
                        onDismiss = { enEdicion = null }
                    )
                }
            }

            composable(
                route = Route.DETAIL,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable

                LaunchedEffect(id) { viewModel.cargarDetalle(id) }

                var enEdicionDetalle by remember { mutableStateOf<Review?>(null) }

                when (val estado = viewModel.detalle) {
                    is UiState.Cargando -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    is UiState.Error -> ErrorView(
                        mensaje = estado.mensaje,
                        onReintentar = { viewModel.cargarDetalle(id) }
                    )

                    is UiState.Exito -> RestaurantDetailScreen(
                        restaurant = estado.datos.restaurant,
                        summary = estado.datos.summary,
                        reviews = estado.datos.reviews,
                        alumno = viewModel.alumno,
                        onWriteReviewClick = { nav.navigate(Route.newReview(id)) },
                        onEditReview = { enEdicionDetalle = it },
                        onDeleteReview = { review ->
                            viewModel.borrarResena(review.id) { viewModel.cargarDetalle(id) }
                        },
                        onBack = { nav.popBackStack() }
                    )
                }

                val enDetalle = enEdicionDetalle
                if (enDetalle != null) {
                    EditarResenaDialog(
                        resena = enDetalle,
                        onGuardar = { stars, comment ->
                            viewModel.editarResena(enDetalle.id, stars, comment) {
                                enEdicionDetalle = null
                                viewModel.cargarDetalle(id)
                            }
                        },
                        onDismiss = { enEdicionDetalle = null }
                    )
                }
            }

            composable(
                route = Route.NEW_REVIEW,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                val restaurant = viewModel.restaurantById(id) ?: return@composable

                val formViewModel: NewReviewViewModel = viewModel()

                NewReviewScreen(
                    restaurant = restaurant,
                    uiState = formViewModel.uiState,
                    onStarsChange = formViewModel::onStarsChange,
                    onCommentChange = formViewModel::onCommentChange,
                    onSave = {
                        formViewModel.publicar(id) { nav.popBackStack() }
                    },
                    onCancel = { nav.popBackStack() }
                )
            }
        }
    }
}

// El mismo dialogo sirve para mis-resenas y para el detalle: la pantalla solo avisa
// que se toco "Editar"; quien guarda es el ViewModel.
@Composable
private fun EditarResenaDialog(
    resena: Review,
    onGuardar: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var stars by remember(resena.id) { mutableStateOf(resena.stars) }
    var comment by remember(resena.id) { mutableStateOf(resena.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar reseña") },
        text = {
            Column {
                StarPicker(value = stars, onValueChange = { stars = it })
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Tu reseña") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(stars, comment) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
