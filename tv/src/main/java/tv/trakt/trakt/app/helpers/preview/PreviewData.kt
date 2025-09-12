package tv.trakt.trakt.app.helpers.preview

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.Comment
import tv.trakt.trakt.app.core.episodes.model.Episode
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Type
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Rating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TmdbId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.TvdbId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortType
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes

internal object PreviewData {
    val show1 = Show(
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("slug2"),
            tvdb = TvdbId(1),
            tmdb = TmdbId(1),
            imdb = ImdbId("tt1234562"),
        ),
        title = "Show Title",
        overview = "This is a sample movie overview that provides a brief description of the movie's plot " +
            "and main themes. It is intended to give viewers an idea of what to expect without revealing too much.",
        year = 2024,
        released = ZonedDateTime.now(),
        genres = listOf("Comedy", "Drama").toImmutableList(),
        images = Images(
            fanart = listOf(
                "walter-r2.trakt.tv/images/shows/000/142/611/fanarts/medium/5248d0dfec.jpg.webp",
            ).toImmutableList(),
            logo = listOf(
                "walter-r2.trakt.tv/images/shows/000/142/611/fanarts/medium/5248d0dfec.jpg.webp",
            ).toImmutableList(),
        ),
        colors = null,
        rating = Rating(
            rating = 8.5f,
            votes = 1234,
        ),
        certification = "PG-18",
        runtime = 90.minutes,
        airedEpisodes = 28,
    )

    val show2 = show1.copy(
        ids = show1.ids.copy(trakt = TraktId(2)),
    )

    val movie1 = Movie(
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("slug2"),
            tvdb = TvdbId(1),
            tmdb = TmdbId(1),
            imdb = ImdbId("tt1234562"),
        ),
        title = "Movie Title",
        overview = "This is a sample movie overview that provides a brief description of the movie's plot " +
            "and main themes. It is intended to give viewers an idea of what to expect without revealing too much.",
        year = 2024,
        released = nowLocalDay(),
        genres = listOf("Comedy", "Drama").toImmutableList(),
        images = Images(
            poster = listOf(
                "walter-r2.trakt.tv/images/movies/000/142/611/fanarts/medium/5248d0dfec.jpg.webp",
            ).toImmutableList(),
            fanart = listOf(
                "walter-r2.trakt.tv/images/movies/000/142/611/fanarts/medium/5248d0dfec.jpg.webp",
            ).toImmutableList(),
        ),
        colors = null,
        rating = Rating(
            rating = 8.5f,
            votes = 1234,
        ),
        certification = "PG-18",
        runtime = 90.minutes,
    )

    val movie2 = movie1.copy(
        ids = movie1.ids.copy(trakt = TraktId(2)),
        images = null,
    )

    val episode1 = Episode(
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("slug2"),
            tvdb = TvdbId(1),
            tmdb = TmdbId(1),
            imdb = ImdbId("tt1234562"),
        ),
        number = 12,
        season = 2,
        title = "Some Episode Title",
        numberAbs = null,
        overview = "John Doe is a fictional character often used as a placeholder name in various contexts. " +
            "He represents an average person and is commonly used in legal cases, examples, and discussions.",
        rating = Rating(rating = 4.34f, votes = 5394),
        commentCount = 4424,
        runtime = 24.minutes,
        episodeType = null,
        originalTitle = "Episode Original Title",
        images = Images(
            screenshot = listOf(
                "walter-r2.trakt.tv/images/movies/000/142/611/fanarts/medium/5248d0dfec.jpg.webp",
            ).toImmutableList(),
        ),
        firstAired = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    val person1 = Person(
        name = "John Doe",
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("john-doe"),
            imdb = ImdbId("tt1234567"),
            tmdb = TmdbId(67890),
            tvdb = TvdbId(112233),
        ),
        knownForDepartment = "Acting",
        birthday = LocalDate.of(1980, 5, 15),
        biography = "John Doe is a fictional character often used as a placeholder name in various contexts. " +
            "He represents an average person and is commonly used in legal cases, examples, and discussions.",
        images = Images(
            headshot = listOf("walter-r2.trakt.tv/images/people/000/414/068/headshots/thumb/85af40e2cb.jpg.webp")
                .toImmutableList(),
        ),
    )

    val person2 = person1.copy(
        ids = person1.ids.copy(trakt = TraktId(2)),
    )

    val user1 = User(
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("john-doe"),
            imdb = ImdbId("tt1234567"),
            tmdb = TmdbId(67890),
            tvdb = TvdbId(112233),
        ),
        name = "John Doe",
        username = "johndoe69",
        location = "New York, USA",
        isVip = false,
        isVipEp = false,
        isVipOg = false,
        isPrivate = false,
        images = null,
        streamings = null,
    )

    val comment1 = Comment(
        id = 1,
        parentId = 0,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
        comment = "This is a sample comment that provides feedback or discussion about a movie, show, or episode. " +
            "It is intended to give viewers an idea of what others think without revealing too much.",
        isSpoiler = false,
        isReview = false,
        replies = 23,
        likes = 12450,
        userRating = 2,
        user = user1,
    )

    val customList1 = CustomList(
        ids = Ids(
            trakt = TraktId(1),
            slug = SlugId("my-custom-list"),
        ),
        name = "My Custom List",
        description = "This is a custom list that contains movies and shows curated by the user. " +
            "It allows for personalized organization of content.",
        privacy = "public",
        displayNumbers = true,
        allowComments = true,
        sortType = SortType.RANK,
        sortOrder = SortOrder.ASCENDING,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
        itemCount = 10,
        shareLink = "",
        type = Type.OFFICIAL,
        commentCount = 123,
        likes = 12,
        images = null,
        user = user1,
    )
}
