pluginManagement {
    repositories {
<<<<<<< HEAD
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
=======
        google()
>>>>>>> 026e7bf (Task: Create SunPortal app with Svitgrid theme — Base structure with solar monitoring UI)
        mavenCentral()
        gradlePluginPortal()
    }
}
<<<<<<< HEAD

=======
>>>>>>> 026e7bf (Task: Create SunPortal app with Svitgrid theme — Base structure with solar monitoring UI)
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

<<<<<<< HEAD
rootProject.name = "SunPortal"
include(":app")
=======
rootProject.name = "Deye Mobile"
include(":app")
>>>>>>> 026e7bf (Task: Create SunPortal app with Svitgrid theme — Base structure with solar monitoring UI)
