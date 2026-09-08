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

import com.skydoves.nowinandroid.buildlogic.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("nowinandroid.spotless")

            extensions.configure<KotlinMultiplatformExtension> { configureKotlinMultiplatform(this) }

            // AGP's lint tasks read KSP's generated source directories but do not declare the
            // dependency. Gradle 9 fails the build on that rather than warning, so the edge is
            // added here for whichever KSP tasks a module happens to register.
            val lintTasks = tasks.matching {
                (it.name.startsWith("generate") && it.name.endsWith("LintModel")) ||
                    it.name.startsWith("lintAnalyze") ||
                    it.name.startsWith("lintVitalAnalyze")
            }
            lintTasks.configureEach {
                dependsOn(tasks.matching { task -> task.name.startsWith("ksp") })
            }
        }
    }
}
