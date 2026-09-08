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

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                val buildDirectory = layout.buildDirectory.asFileTree
                kotlin {
                    target("**/*.kt")
                    targetExclude(buildDirectory)
                    ktlint().editorConfigOverride(
                        mapOf(
                            "ktlint_standard_function-naming" to "disabled",
                            "ktlint_standard_property-naming" to "disabled",
                            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                            "max_line_length" to "120",
                        ),
                    )
                    licenseHeaderFile(rootProject.file("spotless/spotless.license.kt"))
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                format("kts") {
                    target("**/*.kts")
                    targetExclude(buildDirectory)
                    licenseHeaderFile(
                        rootProject.file("spotless/spotless.license.kt"),
                        // First line that is neither blank nor part of a block comment.
                        "(^(?!\\s*$)(?![\\/ ]\\*).*$)",
                    )
                }
                format("xml") {
                    target("**/*.xml")
                    targetExclude(buildDirectory)
                    licenseHeaderFile(rootProject.file("spotless/spotless.license.xml"), "(<[^!?])")
                }
            }
        }
    }
}
