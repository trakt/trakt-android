// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.openapi.generator)
}

openApiGenerate {
    inputSpec = "$rootDir/openapi/openapi.json"
    generatorName = "kotlin"
    validateSpec = true
    cleanupOutput = true
    generateApiTests = false
    generateModelTests = false
    typeMappings = mapOf(
        "number+int64" to "kotlin.Long",
    )
    configOptions = mapOf(
        "library" to "jvm-ktor",
        "dateLibrary" to "kotlinx-datetime",
        "serializationLibrary" to "kotlinx_serialization",
        "sortModelPropertiesByRequiredFlag" to "true",
        "sortParamsByRequiredFlag" to "true",
        "enumPropertyNaming" to "UPPERCASE",
    )
}

// Workaround https://github.com/OpenAPITools/openapi-generator/issues/18904
tasks.register<ReplaceInFilesTask>("dedupeSerializable") {
    group = "Custom"
    description = "Replaces occurrences of @Serializable interface in source files " +
        "to work around the bug described at https://github.com/OpenAPITools/openapi-generator/issues/18904"

    sourceDir.set(file("build/generate-resources/main/src"))
    targetString.set("@Serializable\n\ninterface")
    replacementString.set("interface")
}

// Make APiClient HttpClient public
tasks.register<ReplaceInFilesTask>("publicApiClient") {
    group = "Custom"
    description = "Makes ApiClient class and its HttpClient property public"

    sourceDir.set(file("build/generate-resources/main/src"))
    targetString.set("private val client: HttpClient by lazy")
    replacementString.set("val client: HttpClient by lazy")
}

abstract class ReplaceInFilesTask : DefaultTask() {
    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:Input
    abstract val targetString: Property<String>

    @get:Input
    abstract val replacementString: Property<String>

    @TaskAction
    fun replaceStrings() {
        val target = targetString.get()
        val replacement = replacementString.get()
        val sourceDir = sourceDir.get().asFile

        if (sourceDir.exists()) {
            sourceDir.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val content = file.readText()
                    val newContent = content.replace(target, replacement)
                    if (content != newContent) {
                        file.writeText(newContent)
                        println("Replaced in: ${file.absolutePath}")
                    }
                }
        } else {
            println("Source directory does not exist: $sourceDir")
        }
    }
}

tasks.getByName("openApiGenerate")
    .finalizedBy(
        "dedupeSerializable",
        "publicApiClient"
    )
