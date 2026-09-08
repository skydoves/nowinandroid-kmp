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
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.animation)
            api(compose.ui)
            api(compose.components.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material.icons.core)
            api(libs.compose.material.icons.extended)
            api(libs.compose.adaptive)
            api(libs.compose.adaptive.layout)
            api(libs.compose.adaptive.navigation)
            api(compose.material3AdaptiveNavigationSuite)
            api(libs.landscapist.image)
            api(libs.landscapist.placeholder)
            api(libs.landscapist.animation)
        }
        androidMain.dependencies {
            implementation(compose.uiTooling)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
