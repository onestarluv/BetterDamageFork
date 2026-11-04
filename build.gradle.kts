plugins {
    alias(libs.plugins.conventions.standard)
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.4.0"
    id("com.gradleup.shadow") version "9.6.1"
}

val minecraft = property("minecraft_version").toString()
val adventure = libs.versions.adventure.get()

val pluginVersion = version.toString()
val groupString = group.toString()

val dependenciesContent = libs.bundles.library.standard.map {
    it.map(Any::toString)
}.get()

dependencies {
    implementation(project(":api")) { isTransitive = false }
    implementation(project(":core")) { isTransitive = false }

    implementation(project(":nms:v26_R3")) { isTransitive = false }

    implementation(project(":modelengine:legacy")) { isTransitive = false }
    implementation(project(":modelengine:current")) { isTransitive = false }
}

tasks {
    runServer {
        pluginJars(fileTree("plugins"))
        minecraftVersion(minecraft)
        downloadPlugins {
            hangar("ViaVersion", "5.11.0")
            hangar("ViaBackwards", "5.11.0")
        }
    }
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        manifest {
            attributes(
                "paperweight-mappings-namespace" to "spigot",
                "Version" to pluginVersion,
                "Author" to "pingpong",
                "Url" to "https://github.com/toxicity188/BetterDamage",
                "Created-By" to "Gradle $gradle",
                "Build-Jdk" to "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}",
                "Build-OS" to "${System.getProperty("os.arch")} ${System.getProperty("os.name")}"
            )
        }
        archiveClassifier = ""
        dependencies {
            exclude(dependency("org.jetbrains:annotations:13.0"))
        }
        fun prefix(pattern: String) {
            relocate(pattern, "$groupString.shaded.$pattern")
        }
        prefix("kotlin")
    }
}

bukkitPluginYaml {
    main = "${project.group}.BetterDamagePluginImpl"
    version = project.version.toString()
    website = "https://wiki.egoverse.kr"
    name = rootProject.name
    foliaSupported = true
    apiVersion = "26.3"
    author = "pingpong"
    description = "에고버스 플러그인"
    depend = listOf("Kokonut-Core")
    softDepend = listOf(
        "LuckPerms",
        "BetterModel",
        "MMOCore",
        "MMOItems",
        "ModelEngine",
        "CraftEngine",
        "Nexo"
    )
    libraries = listOf(
        "net.kyori:adventure-api:$adventure",
        "net.kyori:adventure-text-serializer-gson:$adventure"
    ) + dependenciesContent
}

// Collect the deployable artifact without copying plain/intermediate jars.
val collectPluginArtifact = tasks.register<Copy>("collectPluginArtifact") {
    val artifact = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("shadowJar")
    dependsOn(artifact)
    from(artifact.flatMap { it.archiveFile })
    into(layout.projectDirectory.dir("../core/plugin"))
}
tasks.named("shadowJar") { finalizedBy(collectPluginArtifact) }