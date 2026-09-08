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
    id("nowinandroid.android.application")
    alias(libs.plugins.metro)
    // Compose HotSwan hot-reloads the running app on a real device. It rewrites Compose call
    // sites, so it is opt in: `./gradlew -Photswan.enabled=true :app:androidApp:installDebug`.
    alias(libs.plugins.hotswan.compiler) apply false
}

val hotSwanEnabled = providers.gradleProperty("hotswan.enabled").orNull == "true"

if (hotSwanEnabled) {
    apply(plugin = "com.github.skydoves.compose.hotswan.compiler")
}

android {
    namespace = "com.skydoves.nowinandroid"

    defaultConfig {
        applicationId = "com.skydoves.nowinandroid"
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Signed with the debug key so `assembleRelease` produces an installable APK.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.coroutines.android)
    if (hotSwanEnabled) {
        debugImplementation(libs.hotswan.preview)
    }
}
