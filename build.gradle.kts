plugins {
    java
}

group = "com.camdaloon"
version = "1.0.0-2_beta"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("pluginVersion" to project.version)
    }
}

tasks.jar {
    archiveBaseName.set("SimpleFlags")
}
