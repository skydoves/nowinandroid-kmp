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

import com.skydoves.nowinandroid.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * The `api` half of a feature: its `NavKey`, the navigation extensions other features call, and the
 * strings the app shell shows for it. Deliberately has no design system, no data layer and no DI,
 * so any feature can depend on any other feature's `api` without pulling in its implementation.
 */
class KotlinMultiplatformFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nowinandroid.kmp.multiplatform.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("commonMainApi", project(":core:navigation"))
                add("commonMainApi", libs.findLibrary("androidx-navigation3-runtime").get())
                add("commonMainApi", libs.findLibrary("compose-runtime").get())
                add("commonMainApi", libs.findLibrary("kotlinx-serialization-json").get())
            }
        }
    }
}
