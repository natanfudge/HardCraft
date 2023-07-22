import java.io.FileInputStream
import java.net.URI
import java.util.*

plugins {
    java
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.loom.vineflower) apply false
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

architectury {
    minecraft = libs.versions.minecraft.get()
}
val yarnVersion: String = libs.versions.yarn.mappings.get()
val minecraftLib: Provider<MinimalExternalModuleDependency> = libs.minecraft
val kotlinSerialization: Provider<MinimalExternalModuleDependency> = libs.kotlin.serialization.minecraft
val kotlinTest: Provider<MinimalExternalModuleDependency> = libs.kotlin.test
subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "maven-publish")
    apply(plugin = "io.github.juuxel.loom-vineflower")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenLocal()
        maven {
            name = "wthit"
            url = URI("https://maven2.bai.lol")
            content {
                includeGroup ("lol.bai")
                includeGroup ("mcp.mobius.waila")
            }
        }
    }

    dependencies {
        "minecraft"(minecraftLib)
        "mappings"("net.fabricmc:yarn:${yarnVersion}:v2")
        "modImplementation"(kotlinSerialization)
        testImplementation(kotlinTest)
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    }

    tasks.test {
        useJUnit()
    }
}

extra["mod_properties"] = mapOf(
    "mod_id" to property("mod_id"),
    "display_name" to property("display_name"),
    "group" to property("maven_group"),
    "version" to libs.versions.mod.version.get(),
    "minecraft_version" to libs.versions.minecraft.get(),
    "architectury_version" to libs.versions.architectury.api.get(),
    "mod_description" to file("description.md").readText(),
    "license" to property("license_name"),
    "github_repo" to property("github_repo"),
    "authors" to property("authors")
)


val modVersion: String = libs.versions.mod.version.get()
allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "architectury-plugin")

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    version = modVersion
    group = rootProject.property("maven_group").toString()

    repositories {
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    kotlin.target.compilations.all {
        kotlinOptions.jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }

    java {
        withSourcesJar()
    }
}


fun getSecretProperty(path: String, key: String): String {
    val rootPath = System.getenv("SECRETS_PATH")
    val file = File("${rootPath}/${path}.properties")
    if (!file.exists()) return ""
    val properties = Properties()
    FileInputStream(file).use { properties.load(it) }
    return properties[key].toString()
}

fun getSecretFile(path: String): String {
    val rootPath = System.getenv("SECRETS_PATH")
    val file = File("${rootPath}/${path}")
    if (!file.exists()) return ""
    return String(file.readBytes())
}

