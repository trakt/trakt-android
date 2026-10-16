package tv.trakt.trakt.core.reactions.data

internal enum class ReactionSource(
    val analyticsName: String,
) {
    AllComments("ALL_COMMENTS"),
    CommentDetails("COMMENT_DETAILS"),
    ShowComments("SHOW_COMMENTS"),
    MovieComments("MOVIE_COMMENTS"),
    EpisodeComments("EPISODE_COMMENTS"),
}
