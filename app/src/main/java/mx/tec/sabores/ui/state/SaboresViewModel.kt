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
import mx.tec.sabores.domain.ReviewValidator
import retrofit2.HttpException
import java.io.IOException

data class MyReviewItem(val restaurantName: String, val review: Review)

class SaboresViewModel : ViewModel() {

    private val repository = RestaurantRepository()

    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set

    // Shim transicional: la lista ya cargada resuelve el detalle sin pedir de nuevo.
    private var restaurantesCache: List<Restaurant> = emptyList()

    // Estado que SI cambia: Compose se suscribe y recompone solo.
    var reviews by mutableStateOf<List<Review>>(emptyList())
        private set

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

    fun restaurantById(id: Int): Restaurant? = restaurantesCache.firstOrNull { it.id == id }

    fun reviewsOf(restaurantId: Int): List<Review> =
        reviews.filter { it.restaurantId == restaurantId }

    fun summaryOf(restaurantId: Int): RatingSummary =
        RatingSummary.from(reviewsOf(restaurantId))

    val myReviews: List<MyReviewItem>
        get() = reviews.reversed().mapNotNull { review ->
            restaurantById(review.restaurantId)?.let { MyReviewItem(it.name, review) }
        }

    // --- eventos que llegan desde la UI ---

    fun addReview(restaurantId: Int, stars: Int, comment: String) {
        if (!ReviewValidator.isValid(stars, comment)) return
        reviews = reviews + Review(id = 0, restaurantId = restaurantId, author = "", stars = stars, comment = comment.trim())
    }
}
