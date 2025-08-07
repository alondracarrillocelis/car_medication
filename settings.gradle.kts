pluginManagement {
    repositories {
        google() // No limitar con 'content' para evitar errores
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    versionCatalogs {
//        create("libs") {
//            from(files("gradle/libs.versions.toml"))
//        }
//    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "car_medication"
include(":automotive")
