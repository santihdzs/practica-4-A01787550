package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Review
import mx.tec.sabores.ui.components.ErrorView
import mx.tec.sabores.ui.components.StarsRow
import mx.tec.sabores.ui.state.UiState

@Composable
fun MyReviewsScreen(
    estado: UiState<List<Review>>,
    nombreDe: (Int) -> String,
    onReintentar: () -> Unit,
    onEdit: (Review) -> Unit,
    onDelete: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    when (estado) {
        is UiState.Cargando -> Box(
            modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is UiState.Error -> ErrorView(
            mensaje = estado.mensaje,
            onReintentar = onReintentar,
            modifier = modifier
        )

        is UiState.Exito -> if (estado.datos.isEmpty()) {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todavía no has reseñado ningún lugar.",
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.datos, key = { it.id }) { review ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                nombreDe(review.restaurantId),
                                style = MaterialTheme.typography.titleMedium
                            )
                            StarsRow(review.stars)
                            Spacer(Modifier.height(6.dp))
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                            // En esta pantalla todas las resenas son tuyas: no hay que filtrar.
                            Row {
                                TextButton(onClick = { onEdit(review) }) { Text("Editar") }
                                TextButton(onClick = { onDelete(review) }) { Text("Borrar") }
                            }
                        }
                    }
                }
            }
        }
    }
}
