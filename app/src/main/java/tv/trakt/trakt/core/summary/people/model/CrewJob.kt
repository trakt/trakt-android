package tv.trakt.trakt.core.summary.people.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

private val nonAlphanumeric = Regex("[^a-zA-Z0-9]")

/**
 * Resolves an API crew job (for example "Second Unit Director") to its translated string.
 * Returns null for jobs without a translation.
 */
@StringRes
internal fun crewJobStringRes(job: String): Int? {
    return crewJobs[job.replace(nonAlphanumeric, "_").lowercase()]
}

private val crewJobs: Map<String, Int> = mapOf(
    "action_director" to R.string.translated_value_job_action_director,
    "adaptation" to R.string.translated_value_job_adaptation,
    "additional_second_assistant_director" to R.string.translated_value_job_additional_second_assistant_director,
    "additional_third_assistant_director" to R.string.translated_value_job_additional_third_assistant_director,
    "assistant_director" to R.string.translated_value_job_assistant_director,
    "assistant_director_trainee" to R.string.translated_value_job_assistant_director_trainee,
    "author" to R.string.translated_value_job_author,
    "book" to R.string.translated_value_job_book,
    "characters" to R.string.translated_value_job_characters,
    "co_director" to R.string.translated_value_job_co_director,
    "co_writer" to R.string.translated_value_job_co_writer,
    "comic_book" to R.string.translated_value_job_comic_book,
    "continuity" to R.string.translated_value_job_continuity,
    "creative_producer" to R.string.translated_value_job_creative_producer,
    "creator" to R.string.translated_value_job_creator,
    "crowd_assistant_director" to R.string.translated_value_job_crowd_assistant_director,
    "dialogue" to R.string.translated_value_job_dialogue,
    "director" to R.string.translated_value_job_director,
    "executive_story_editor" to R.string.translated_value_job_executive_story_editor,
    "field_director" to R.string.translated_value_job_field_director,
    "first_assistant_director" to R.string.translated_value_job_first_assistant_director,
    "first_assistant_director_trainee" to R.string.translated_value_job_first_assistant_director_trainee,
    "first_assistant_director_trainee_prep" to R.string.translated_value_job_first_assistant_director_trainee_prep,
    "graphic_novel" to R.string.translated_value_job_graphic_novel,
    "head_of_story" to R.string.translated_value_job_head_of_story,
    "idea" to R.string.translated_value_job_idea,
    "insert_unit_director" to R.string.translated_value_job_insert_unit_director,
    "insert_unit_first_assistant_director" to R.string.translated_value_job_insert_unit_first_assistant_director,
    "junior_story_editor" to R.string.translated_value_job_junior_story_editor,
    "layout" to R.string.translated_value_job_layout,
    "lyricist" to R.string.translated_value_job_lyricist,
    "musical" to R.string.translated_value_job_musical,
    "novel" to R.string.translated_value_job_novel,
    "opera" to R.string.translated_value_job_opera,
    "original_concept" to R.string.translated_value_job_original_concept,
    "original_film_writer" to R.string.translated_value_job_original_film_writer,
    "original_original_story" to R.string.translated_value_job_original_original_story,
    "original_series_creator" to R.string.translated_value_job_original_series_creator,
    "other" to R.string.translated_value_job_other,
    "scenario_writer" to R.string.translated_value_job_scenario_writer,
    "screenplay" to R.string.translated_value_job_screenplay,
    "screenstory" to R.string.translated_value_job_screenstory,
    "script_consultant" to R.string.translated_value_job_script_consultant,
    "script_coordinator" to R.string.translated_value_job_script_coordinator,
    "script_editor" to R.string.translated_value_job_script_editor,
    "script_supervisor" to R.string.translated_value_job_script_supervisor,
    "second_assistant_director" to R.string.translated_value_job_second_assistant_director,
    "second_assistant_director_trainee" to R.string.translated_value_job_second_assistant_director_trainee,
    "second_second_assistant_director" to R.string.translated_value_job_second_second_assistant_director,
    "second_unit_director" to R.string.translated_value_job_second_unit_director,
    "second_unit_first_assistant_director" to R.string.translated_value_job_second_unit_first_assistant_director,
    "senior_story_editor" to R.string.translated_value_job_senior_story_editor,
    "series_composition" to R.string.translated_value_job_series_composition,
    "series_director" to R.string.translated_value_job_series_director,
    "series_guest_director" to R.string.translated_value_job_series_guest_director,
    "short_story" to R.string.translated_value_job_short_story,
    "staff_writer" to R.string.translated_value_job_staff_writer,
    "stage_director" to R.string.translated_value_job_stage_director,
    "story" to R.string.translated_value_job_story,
    "story_story_artist" to R.string.translated_value_job_story_story_artist,
    "story_story_consultant" to R.string.translated_value_job_story_story_consultant,
    "story_story_coordinator" to R.string.translated_value_job_story_story_coordinator,
    "story_story_developer" to R.string.translated_value_job_story_story_developer,
    "story_story_editor" to R.string.translated_value_job_story_story_editor,
    "story_story_manager" to R.string.translated_value_job_story_story_manager,
    "story_story_supervisor" to R.string.translated_value_job_story_story_supervisor,
    "storyboard" to R.string.translated_value_job_storyboard,
    "teleplay" to R.string.translated_value_job_teleplay,
    "texte" to R.string.translated_value_job_texte,
    "theatre_play" to R.string.translated_value_job_theatre_play,
    "third_assistant_director" to R.string.translated_value_job_third_assistant_director,
    "writer" to R.string.translated_value_job_writer,
    "writers_assistant" to R.string.translated_value_job_writers_assistant,
    "writers_production" to R.string.translated_value_job_writers_production,
)
