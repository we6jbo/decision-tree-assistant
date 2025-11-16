// MARK: JBO1|actor=Jeremiah ONeal|ts=2025-09-01T10:46-07:00|note=All changes from T14 committed to Github on this date.|license=GPL-3.0-or-later|deliver_to=Lenovo ideacentre|deliver_by=2025-09-01T13:00-07:00|verification=unverified | CARD=A8E9FF8891C.json
// build.gradle.kts (Project) - Root file

plugins {
    // Corrected Kotlin DSL syntax for plugins block: use double quotes and parentheses
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}

// REMOVE the allprojects { repositories { ... } } block entirely.
// Repositories must be declared in settings.gradle.kts in modern Gradle.

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

