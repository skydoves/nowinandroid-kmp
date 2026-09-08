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
 * Every `:feature:*` module needs exactly the same slice of the app: the design system, the shared
 * UI widgets, the data/domain layers, a multiplatform `ViewModel`, and a Navigation 3 entry point.
 * Declaring that once here keeps the six feature build scripts down to a `plugins { }` block.
 */
class KotlinMultiplatformFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("nowinandroid.kmp.multiplatform.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("dev.zacsweers.metro")
            // The navgraph processor is a KSP processor and reads only its own module's sources,
            // so it is applied per feature: each one owns and checks its slice of the graph.
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.github.skydoves.navgraph")

            dependencies {
                add("commonMainApi", project(":core:ui"))
                add("commonMainApi", project(":core:designsystem"))
                add("commonMainApi", project(":core:data"))
                add("commonMainApi", project(":core:domain"))
                add("commonMainApi", project(":core:analytics"))

                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("commonMainApi", libs.findLibrary("androidx-navigation3-ui").get())
                add("commonMainApi", libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
                add("commonMainApi", libs.findLibrary("compose-adaptive-navigation3").get())
                add("commonMainApi", libs.findLibrary("metro-runtime").get())
                add("commonMainApi", libs.findLibrary("metro-viewmodel").get())
                add("commonMainApi", libs.findLibrary("metro-viewmodel-compose").get())
                add("commonMainApi", libs.findLibrary("compose-nav-graph-annotations").get())

                add("commonTestImplementation", libs.findLibrary("kotlin-test").get())
                add("commonTestImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("commonTestImplementation", libs.findLibrary("turbine").get())
                add("commonTestImplementation", project(":core:testing"))
            }
        }
    }
}
