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
    id("nowinandroid.kmp.multiplatform")
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.model)
            api(projects.core.common)
            api(libs.androidx.room.runtime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.metro.runtime)
        }
        // The bundled SQLite is a native library, so it is a per-platform dependency rather than a
        // common one; each platform's binding container is what names the driver.
        androidMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        getByName("wasmJsMain").dependencies {
            implementation(libs.androidx.sqlite.web)
            implementation(libs.kotlinx.browser)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Room's bundled SQLite driver needs a JNI library an Android host unit test cannot load.
        getByName("nonAndroidTest").dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
        "kspDesktop",
        "kspWasmJs",
    ).forEach { add(it, libs.androidx.room.compiler) }
}
