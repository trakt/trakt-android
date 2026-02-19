package tv.trakt.trakt.common.model.pagination

data class Pagination(
    val page: Int = 1,
    val limit: Int = 100,
)
