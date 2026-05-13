import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

abstract class ValidateStringPlaceholdersTask : DefaultTask() {
    @get:InputFiles
    abstract val resDir: ConfigurableFileCollection

    @TaskAction
    fun validate() {
        val sourceFile = resDir.asFileTree.matching {
            include("values/strings.xml")
        }.singleFile

        val sourceStrings = parseStrings(sourceFile)
        val sourcePlaceholders = sourceStrings.mapValues { (_, value) -> extractPlaceholders(value) }
            .filter { it.value.isNotEmpty() }

        val translationFiles = resDir.asFileTree.matching {
            include("values-*/strings.xml")
        }.files

        val errors = mutableListOf<String>()

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
                put(name, el.textContent)
            }
        }
    }

    private fun extractPlaceholders(text: String): Map<String, String> {
        val regex = Regex("""%(\d+)\$([a-zA-Z])""")
        return regex.findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
    }
}
