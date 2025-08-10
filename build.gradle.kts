import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}



kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        // Fixes Hidpi issues, has better hot reload support.
        vendor = JvmVendorSpec.JETBRAINS
    }
    compilerOptions {
        freeCompilerArgs.add("-Xnon-local-break-continue")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

loom {
//    splitEnvironmentSourceSets()
    accessWidenerPath.set(file("src/main/resources/${rootProject.property("mod_id")}.accesswidener"))

    mods {
        register("hardcraft") {
            sourceSet("main")
//            sourceSet("client")
        }
    }
}

repositories {
    maven {
        url  = uri("https://maven2.bai.lol")
        content {
            includeGroup ("lol.bai")
            includeGroup ("mcp.mobius.waila")
        }
    }
    mavenLocal() // For minecraft kotlin serialization
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft(libs.minecraft)
    mappings("net.fabricmc:yarn:${libs.versions.yarn.mappings.get()}:v2")
    modImplementation(libs.kotlin.serialization.minecraft)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.language.kotlin)
    modImplementation(libs.fabric.api)
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation(kotlin("test"))
//    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
//    testImplementation( "net.fabricmc:fabric-loader-junit:${libs.versions.fabric.loader.get()}")
    modCompileOnly(libs.wthit.api)
    modRuntimeOnly(libs.wthit.fabric)
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "test-${project.name}"
        eula = true
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", libs.versions.minecraft.get())
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to libs.versions.minecraft.get(),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

tasks.withType<Test> {
    val jdksDir = rootProject.layout.projectDirectory.dir("jdks").asFile
    if (jdksDir.exists()) {
        val specificJdk = jdksDir.listFiles()?.firstOrNull { it.isDirectory && it.name.startsWith("jbr") }
        if (specificJdk != null) {
            val exe = specificJdk.resolve("bin/java.exe")
            println("Use JBR 25: $exe")
            executable = exe.toString()
            jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:HotswapAgent=core", "--enable-native-access=ALL-UNNAMED")
        } else {
            println("Warn: no JBR under jdks/")
        }
    } else {
        println("Warn: missing jdks dir")
    }

    useJUnitPlatform()
    maxParallelForks = 1
    testLogging { // credits: https://stackoverflow.com/a/36130467/5917497
        // set options for log level LIFECYCLE
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        // set options for log level DEBUG and INFO
        debug {
            events = setOf(
                TestLogEvent.STARTED,
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat
        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) { // will match the outermost suite
                val pass = "${Color.GREEN}${result.successfulTestCount} passed${Color.NONE}"
                val fail = "${Color.RED}${result.failedTestCount} failed${Color.NONE}"
                val skip = "${Color.YELLOW}${result.skippedTestCount} skipped${Color.NONE}"
                val type = when (val r = result.resultType) {
                    TestResult.ResultType.SUCCESS -> "${Color.GREEN}$r${Color.NONE}"
                    TestResult.ResultType.FAILURE -> "${Color.RED}$r${Color.NONE}"
                    TestResult.ResultType.SKIPPED -> "${Color.YELLOW}$r${Color.NONE}"
                }
                val output = "Results: $type (${result.testCount} tests, $pass, $fail, $skip)"
                val startItem = "|   "
                val endItem = "   |"
                val repeatLength = startItem.length + output.length + endItem.length - 36
                println("")
                println("\n" + ("-" * repeatLength) + "\n" + startItem + output + endItem + "\n" + ("-" * repeatLength))
            }
        }))
    }
    onOutput(KotlinClosure2({ _: TestDescriptor, event: TestOutputEvent ->
        if (event.destination == TestOutputEvent.Destination.StdOut) {
            logger.lifecycle(event.message.replace(Regex("""\s+$"""), ""))
        }
    }))
}

operator fun String.times(x: Int): String {
    return List(x) { this }.joinToString("")
}

internal enum class Color(ansiCode: Int) {
    NONE(0),
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    PURPLE(35),
    CYAN(36),
    WHITE(37);

    private val ansiString: String = "\u001B[${ansiCode}m"

    override fun toString(): String {
        return ansiString
    }
}

gradle.taskGraph.whenReady {
    if (hasTask(tasks.named("test").get())) {
        tasks.named("runGameTest").configure { enabled = false }   // or onlyIf { false }
    }
}