plugins {
    alias(libs.plugins.conventions.core)
}

dependencies {
    testImplementation(project(":api"))
    testImplementation("io.papermc.paper:paper-api:${property("minecraft_version")}.build.+")
    testImplementation("com.h2database:h2:2.3.232")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(project(":modelengine:legacy"))
    compileOnly(project(":modelengine:current"))
    compileOnly(project(":nms:v26_R3"))

    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.6.0")
    compileOnly("io.lumine:MythicLib-dist:1.7.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOCore-API:1.13.1-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.10.1-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    compileOnly("com.nexomc:nexo:1.29.1")
    compileOnly("net.luckperms:api:5.5")
}
