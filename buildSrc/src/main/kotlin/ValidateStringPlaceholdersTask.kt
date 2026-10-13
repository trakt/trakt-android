import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.util.IllegalFormatException
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

abstract class ValidateStringPlaceholdersTask : DefaultTask() {
    @get:InputFiles
    abstract val resDir: ConfigurableFileCollection

    @get:Input
    abstract val ignoredFormatKeys: SetProperty<String>

    init {
        ignoredFormatKeys.convention(emptySet())
    }

    @TaskAction
    fun validate() {
        val sourceFile = resDir.asFileTree.matching {
            include("values/strings.xml")
        }.singleFile

        val sourceStrings = parseStrings(sourceFile)
        val sourcePlaceholders = sourceStrings.mapValues { (_, value) -> extractPlaceholders(value) }
            .filter { it.value.isNotEmpty() }
        val ignoredKeys = ignoredFormatKeys.get()
        val sourceFormatArgs = sourceStrings.mapValues { (_, value) -> buildFormatArgs(value) }
            .filter { it.value.isNotEmpty() && it.key !in ignoredKeys }

        val translationFiles = resDir.asFileTree.matching {
            include("values-*/strings.xml")
        }.files

        val errors = mutableListOf<String>()

        errors += formatErrors("values", sourceStrings, sourceFormatArgs)

        for (translationFile in translationFiles.sortedBy { it.path }) {
            val locale = translationFile.parentFile.name
            val translationStrings = parseStrings(translationFile)

            for ((name, translationValue) in translationStrings) {
                val sourcePlaceholderMap = sourcePlaceholders[name] ?: continue
                val translationPlaceholderMap = extractPlaceholders(translationValue)

                for ((position, sourceType) in sourcePlaceholderMap) {
                    val translationType = translationPlaceholderMap[position]
                    if (translationType == null) {
                        errors += "[$locale] '$name': missing placeholder %$position\$$sourceType"
                    } else if (translationType != sourceType) {
                        errors +=
                            "[$locale] '$name': placeholder %$position expected type '$sourceType' but got '$translationType'"
                    }
                }
            }

            errors += formatErrors(locale, translationStrings, sourceFormatArgs)
        }

        if (errors.isNotEmpty()) {
            throw GradleException(
                "String placeholder validation failed:\n" + errors.joinToString("\n") { "  $it" },
            )
        }

        logger.lifecycle("String placeholder validation passed (${translationFiles.size} locales checked)")
    }

    private fun parseStrings(file: java.io.File): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.documentElement.getElementsByTagName("string")
        return buildMap {
            for (i in 0 until nodes.length) {
                val el = nodes.item(i) as Element
                val name = el.getAttribute("name").takeIf { it.isNotEmpty() } ?: continue
                if (el.getAttribute("formatted") == "false") continue
                put(name, el.textContent)
            }
        }
    }

    private fun extractPlaceholders(text: String): Map<String, String> {
        val regex = Regex("""%(\d+)\$([a-zA-Z])""")
        return regex.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
    }

    // Mirrors the runtime String.format call so unescaped % signs fail the build instead of crashing the app.
    private fun formatErrors(
        locale: String,
        strings: Map<String, String>,
        formatArgs: Map<String, Array<Any>>,
    ): List<String> {
        return strings.mapNotNull { (name, value) ->
            val args = formatArgs[name] ?: return@mapNotNull null
            try {
                String.format(Locale.ROOT, value, *args)
                null
            } catch (e: IllegalFormatException) {
                "[$locale] '$name': invalid format string (${e::class.simpleName}: ${e.message})." +
                    " Escape a literal percent sign as %%"
            }
        }
    }

    private fun buildFormatArgs(text: String): Array<Any> {
        val specifiers = Regex("""%%|%(?:(\d+)\$)?([a-zA-Z])""").findAll(text)
            .filter { it.value != "%%" }
            .toList()

        var ordinaryIndex = 0
        val conversions = specifiers.associate { match ->
            val index = match.groupValues[1].toIntOrNull() ?: ++ordinaryIndex
            index to match.groupValues[2].single()
        }

        val count = conversions.keys.maxOrNull() ?: return emptyArray()
        return Array(count) { sampleArg(conversions[it + 1]) }
    }

    private fun sampleArg(conversion: Char?): Any {
        return when (conversion?.lowercaseChar()) {
            'd', 'o', 'x' -> 1
            'e', 'f', 'g', 'a' -> 1.0
            'c' -> 'a'
            'b' -> true
            else -> "a"
        }
    }
}
