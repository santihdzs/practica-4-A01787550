package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.RestaurantEnLista
import mx.tec.sabores.ui.components.ErrorView
import mx.tec.sabores.ui.components.RestaurantCard
import mx.tec.sabores.ui.state.UiState
import mx.tec.sabores.ui.theme.SaboresTheme

@Composable
fun RestaurantListScreen(
    estado: UiState<List<RestaurantEnLista>>,
    onReintentar: () -> Unit,
    onRestaurantClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (estado) {
        is UiState.Cargando -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is UiState.Error -> ErrorView(
            mensaje = estado.mensaje,
            onReintentar = onReintentar,
            modifier = modifier
        )

        is UiState.Exito -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(estado.datos, key = { it.restaurant.id }) { item ->
                RestaurantCard(
                    restaurant = item.restaurant,
                    summary = item.summary,
                    onClick = { onRestaurantClick(item.restaurant.id) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListPreview() {
    SaboresTheme {
        RestaurantListScreen(
            estado = UiState.Exito(
                listOf(
                    RestaurantEnLista(
                        Restaurant(1, "La Chinampa", "Mexicana", "Av. Garza Sada 300",
                            "Cocina de mercado: tacos de guisado, sopes y agua del día.", 1, "🌮"),
                        RatingSummary(4.2, 3)
                    )
                )
            ),
            onReintentar = {},
            onRestaurantClick = {}
        )
    }
}
