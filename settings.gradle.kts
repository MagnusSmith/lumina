rootProject.name = "meter-config-service"
include ("application", "infrastructure")

project(":application").projectDir = file("application")
project(":infrastructure").projectDir = file("infrastructure")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}



