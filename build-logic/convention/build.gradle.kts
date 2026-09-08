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
    `kotlin-dsl`
}

group = "com.skydoves.nowinandroid.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "nowinandroid.kmp.multiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "nowinandroid.kmp.multiplatform.compose"
            implementationClass = "KotlinMultiplatformComposeConventionPlugin"
        }
        register("kotlinMultiplatformFeature") {
            id = "nowinandroid.kmp.feature"
            implementationClass = "KotlinMultiplatformFeatureConventionPlugin"
        }
        register("kotlinMultiplatformFeatureApi") {
            id = "nowinandroid.kmp.feature.api"
            implementationClass = "KotlinMultiplatformFeatureApiConventionPlugin"
        }
        register("androidApplication") {
            id = "nowinandroid.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("spotless") {
            id = "nowinandroid.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}
