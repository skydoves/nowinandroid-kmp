/*
 * Designed and developed by 2026 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
                includeGroupByRegex("com\\.google\\.testing\\.platform")
            }
            mavenContent { releasesOnly() }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS rather than FAIL_ON_PROJECT_REPOS: the Kotlin/Wasm plugin registers its own
    // Node distribution repository on the root project and cannot be told not to. Preferring
    // settings keeps resolution owned here — the ivy repositories below are what actually serve
    // Node and Yarn — while letting that registration exist instead of failing the build.
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
                includeGroupByRegex("com\\.google\\.testing\\.platform")
            }
            mavenContent { releasesOnly() }
        }
        mavenCentral()

        // The Kotlin/Wasm browser toolchain downloads its own Node and Yarn. With
        // FAIL_ON_PROJECT_REPOS the Kotlin plugin cannot add those repositories itself, so they are
        // declared here and scoped to exactly the two modules they serve.
        ivy("https://nodejs.org/dist") {
            name = "Node Distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn Distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen Distributions"
            patternLayout { artifact("version_[revision]/[artifact]-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

rootProject.name = "nowinandroid-kmp"

include(":core:common")
include(":core:model")
include(":core:datastore")
include(":core:database")
include(":core:network")
include(":core:analytics")
include(":core:notifications")
include(":core:data")
include(":core:domain")
include(":core:navigation")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")

// The api/impl split is kept from the Android original: an `api` module holds only a feature's
// `NavKey` and the strings the app shell needs, so features can navigate to each other without
// depending on each other's implementation.
include(":feature:foryou:api")
include(":feature:foryou:impl")
include(":feature:interests:api")
include(":feature:interests:impl")
include(":feature:bookmarks:api")
include(":feature:bookmarks:impl")
include(":feature:topic:api")
include(":feature:topic:impl")
include(":feature:search:api")
include(":feature:search:impl")
include(":feature:settings:impl")

include(":app:shared")
include(":app:androidApp")
include(":app:desktopApp")
include(":app:webApp")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    Now in Android KMP requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
}
