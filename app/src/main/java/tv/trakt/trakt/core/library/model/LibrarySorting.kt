package tv.trakt.trakt.core.library.model

internal fun getLibrarySorting(): Comparator<LibraryItem> {
    return compareByDescending { it.collectedAt }
}
