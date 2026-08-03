package tv.trakt.trakt.common.model.pagination

data class Pagination(
    val page: Int = 1,
    val limit: Int = 100,
) {
    init {
        require(page > 0) { "Page number must be greater than 0." }
        require(limit > 0) { "Limit must be greater than 0." }
    }

    companion object {
        val Default = Pagination()
    }
}
