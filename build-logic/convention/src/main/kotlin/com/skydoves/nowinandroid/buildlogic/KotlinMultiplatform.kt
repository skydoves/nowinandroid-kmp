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

package com.skydoves.nowinandroid.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * The Android namespace every module derives from its Gradle path, so no module has to repeat it.
 * `:core:model` becomes `com.skydoves.nowinandroid.core.model`, `:feature:foryou`
 * becomes `com.skydoves.nowinandroid.feature.foryou`.
 */
internal val Project.derivedNamespace: String
    get() = "com.skydoves.nowinandroid" + path.replace(":", ".").replace("-", "")

/**
 * Declares the three target platforms plus the intermediate source sets that carry dependencies
 * shared by some, but not all, of them.
 *
 * Since AGP 9 the Android side of a multiplatform module comes from
 * `com.android.kotlin.multiplatform.library`, which contributes an `android` target to the `kotlin`
 * extension instead of a separate `android { }` block. Its test compilations are opt in, so
 * [withHostTest] and [withDeviceTest] are what create `androidHostTest` and `androidDeviceTest`.
 */
internal fun Project.configureKotlinMultiplatform(extension: KotlinMultiplatformExtension) =
    extension.apply {
        val jvmTargetVersion = libs.version("jvmTarget")

        androidLibraryTarget(this@configureKotlinMultiplatform, jvmTargetVersion)

        jvm("desktop") {
            compilerOptions { jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion)) }
        }

        // No iosX64: Room 3 and androidx.sqlite 2.7.0 stopped publishing that target, and an Intel
        // simulator is not something this app needs to support.
        iosArm64()
        iosSimulatorArm64()

        wasmJs { browser() }

        applyDefaultHierarchyTemplate()

        compilerOptions {
            freeCompilerArgs.addAll("-Xexpect-actual-classes")
        }

        with(sourceSets) {
            all {
                languageSettings.optIn("kotlin.RequiresOptIn")
                languageSettings.optIn("kotlin.time.ExperimentalTime")
            }

            // Everything Skiko renders: no Android framework, no `android.content.Context`.
            // `dependOn` skips names that do not exist, so a target left out of this list silently
            // loses every actual declared here.
            val nonAndroidMain = create("nonAndroidMain") { dependsOn(getByName("commonMain")) }
            dependOn(nonAndroidMain, "iosMain", "desktopMain", "wasmJsMain")

            // The test counterpart. Room's bundled SQLite ships a JNI library that an Android
            // *host* unit test cannot load, so tests that touch a real database live here and run
            // on the desktop JVM and on iOS instead.
            val nonAndroidTest = create("nonAndroidTest") { dependsOn(getByName("commonTest")) }
            dependOn(nonAndroidTest, "iosTest", "desktopTest")

            // Android + desktop share a JVM runtime, so they share JVM-only libraries.
            val jvmSharedMain = create("jvmSharedMain") { dependsOn(getByName("commonMain")) }
            dependOn(jvmSharedMain, "androidMain", "desktopMain")
        }
    }

/**
 * Wires [parent] into each named source set that exists. Test source sets only materialise once
 * `withHostTest`/`withDeviceTest` have run, so a missing name is skipped rather than failing.
 */
private fun NamedDomainObjectContainer<KotlinSourceSet>.dependOn(
    parent: KotlinSourceSet,
    vararg names: String,
) = names.forEach { name -> findByName(name)?.dependsOn(parent) }

private fun KotlinMultiplatformExtension.androidLibraryTarget(
    project: Project,
    jvmTargetVersion: String,
) {
    val android = (this as ExtensionAware).extensions
        .getByName("android") as KotlinMultiplatformAndroidLibraryTarget

    android.apply {
        namespace = project.derivedNamespace
        compileSdk = project.libs.version("androidCompileSdk").toInt()
        minSdk = project.libs.version("androidMinSdk").toInt()
        compilerOptions { jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion)) }
        androidResources.enable = true
        lint { abortOnError = false }

        withHostTest {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
}
