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

plugins {
    id("nowinandroid.kmp.multiplatform.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.metro)
    // The navgraph processor is a KSP processor, and the plugin cannot apply KSP itself because
    // its version is tied to the Kotlin version.
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.nav.graph)
}

kotlin {
    // The Xcode project links against this framework; `embedAndSignAppleFrameworkForXcode` is what
    // the "Compile Kotlin" build phase runs.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "NiaKit"
            isStatic = true
            binaryOption("bundleId", "com.skydoves.nowinandroid.shared")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.data)
            api(projects.core.domain)
            api(projects.core.designsystem)
            api(projects.core.ui)
            api(projects.core.navigation)
            api(projects.core.analytics)

            api(projects.feature.foryou.impl)
            api(projects.feature.interests.impl)
            api(projects.feature.bookmarks.impl)
            api(projects.feature.topic.impl)
            api(projects.feature.search.impl)
            api(projects.feature.settings.impl)

            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.navigation3.ui)
            api(libs.androidx.lifecycle.viewmodel.navigation3)
            api(libs.compose.adaptive.navigation3)
            api(libs.metro.runtime)
            api(libs.metro.viewmodel)
            api(libs.metro.viewmodel.compose)
            api(libs.compose.nav.graph.annotations)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.landscapist.image)
            implementation(libs.landscapist.svg)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.kotlinx.browser)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        getByName("desktopTest").dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.compose.ui.test)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

// The app shell owns the app-level strings, so its `Res` gets the root package rather than the
// path-derived `...app.shared.generated.resources` the convention plugin would give it.
compose.resources {
    publicResClass = true
    packageOfResClass = "com.skydoves.nowinandroid.generated.resources"
}
