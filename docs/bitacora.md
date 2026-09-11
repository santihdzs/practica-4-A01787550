# Bitácora — Práctica 4: Sabores en red

## Ejercicio 0 — De endpoint a pantalla

| Endpoint | Pantalla que lo usa |
|---|---|
| `GET /restaurants` | Lista de restaurantes |
| `GET /restaurants/{id}` | Detalle del restaurante |
| `GET /reviews?restaurantId={id}` | Detalle (reseñas del lugar) |
| `GET /me/reviews` | Mis reseñas |
| `POST /reviews` | Nueva reseña |
| `PATCH /reviews/{id}` | Editar reseña |
| `DELETE /reviews/{id}` | Borrar reseña |

## Ejercicio A2 — ¿Quién calcula el promedio?

El servidor. El `RestaurantDto` trae `ratingAverage` y `ratingCount` listos, el mapper `toSummary()` los pasa al dominio. La app no promedia nada, solo muestra lo que el servidor manda.

## Ejercicio B1 — Los tres estados

La app mostró "No hay conexión. Revisa tu internet." con un botón "Reintentar". Al restaurar la red y tocar Reintentar, la lista cargó normal. Tres estados (Cargando, Error, Éxito) se manejan con `UiState`, la app nunca se queda en blanco.

## Ejercicio C2 — Romper la validación

En la rama `experimento-c2` comenté el guard `if (!uiState.canSave) return` de `publicar`. Aun así el formulario siguió protegido, el botón "Publicar reseña" usa `enabled = canSave`, un chequeo aparte, así que con estrellas o comentario vacíos el botón queda deshabilitado y no se puede enviar. Una reseña válida se publicó y apareció en la lista. Conclusión: quitar una sola validación no basta, había una segunda barrera en el `enabled`.