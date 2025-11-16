// MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json
include(":mobile")
pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
// #viar - Per Google AI suggestion: Build was configured to prefer settings and therefore this resolved the problem.
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // This line enforces the rule
  repositories {
    google()
    mavenCentral()
  }
}



rootProject.name = "decision-tree-assistant"
include(":app")

