package mx.tec.sabores.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.RestaurantEnLista
import mx.tec.sabores.domain.Review
import retrofit2.HttpException
import java.io.IOException

data class Detalle(
    val restaurant: Restaurant,
    val reviews: List<Review>
) {
    val summary: RatingSummary = RatingSummary.from(reviews)
}

class SaboresViewModel : ViewModel() {

    private val repository = RestaurantRepository()

    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set

    var detalle by mutableStateOf<UiState<Detalle>>(UiState.Cargando)
        private set

    var misResenas by mutableStateOf<UiState<List<Review>>>(UiState.Cargando)
        private set

    // Shim transicional: la lista ya cargada resuelve el detalle sin pedir de nuevo.
    private var restaurantesCache: List<Restaurant> = emptyList()

    init { cargarRestaurantes() }

    fun cargarRestaurantes() {
        viewModelScope.launch {
            restaurantes = UiState.Cargando
            restaurantes = try {
                val datos = repository.getAllForList()
                restaurantesCache = datos.map { it.restaurant }
                UiState.Exito(datos)
            } catch (e: IOException) {
                UiState.Error("No hay conexión. Revisa tu internet.")
            } catch (e: HttpException) {
                UiState.Error("El servidor respondió ${e.code()}.")
            }
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            detalle = UiState.Cargando
            detalle = pedir { Detalle(repository.getById(id), repository.getReviews(id)) }
        }
    }

    fun cargarMisResenas() {
        viewModelScope.launch {
            misResenas = UiState.Cargando
            misResenas = pedir { repository.getMyReviews() }
        }
    }

    fun restaurantById(id: Int): Restaurant? = restaurantesCache.firstOrNull { it.id == id }

    // --- eventos que llegan desde la UI ---

    fun editarResena(id: Int, stars: Int?, comment: String?, alTerminar: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.editReview(id, stars, comment)
                cargarMisResenas()
                alTerminar()
            } catch (e: IOException) {
                misResenas = UiState.Error("No hay conexión. No se pudo editar.")
            } catch (e: HttpException) {
                misResenas = UiState.Error(mensajeDe(e))
            }
        }
    }

    private suspend fun <T> pedir(block: suspend () -> T): UiState<T> = try {
        UiState.Exito(block())
    } catch (e: IOException) {
        UiState.Error("No hay conexión. Revisa tu internet.")
    } catch (e: HttpException) {
        UiState.Error(mensajeDe(e))
    }
}
