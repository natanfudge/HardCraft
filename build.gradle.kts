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
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xnon-local-break-continue")
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

