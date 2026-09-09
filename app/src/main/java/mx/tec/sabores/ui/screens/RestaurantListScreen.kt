package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.ui.components.RestaurantCard
import mx.tec.sabores.ui.theme.SaboresTheme

@Composable
fun RestaurantListScreen(
    restaurants: List<Restaurant>,
    summaryOf: (Int) -> RatingSummary,
    onRestaurantClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(restaurants, key = { it.id }) { restaurant ->
            RestaurantCard(
                restaurant = restaurant,
                summary = summaryOf(restaurant.id),
                onClick = { onRestaurantClick(restaurant.id) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListPreview() {
    SaboresTheme {
        RestaurantListScreen(
            restaurants = listOf(
                Restaurant(1, "La Chinampa", "Mexicana", "Av. Garza Sada 300",
                    "Cocina de mercado: tacos de guisado, sopes y agua del día.", 1, "🌮"),
                Restaurant(2, "Nonna Rosa", "Italiana", "Río Nazas 118",
                    "Pasta fresca hecha en casa y horno de leña a la vista.", 3, "🍝")
            ),
            summaryOf = { RatingSummary(4.2, 3) },
            onRestaurantClick = {}
        )
    }
}
