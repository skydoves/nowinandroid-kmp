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

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

/**
 * Every module that owns `composeResources/` gets its own generated `Res` class, named after the
 * module's Gradle path. Without this they would all land on the same fully qualified name and the
 * last one to be compiled would win.
 */
internal fun Project.configureComposeResources() {
    val compose = extensions.getByType(ComposeExtension::class.java)
    val resources = (compose as ExtensionAware).extensions
        .getByName("resources") as ResourcesExtension

    resources.publicResClass = true
    resources.packageOfResClass = "com.skydoves.nowinandroid" +
        path.replace(":", ".").replace("-", "") +
        ".generated.resources"
    resources.generateResClass = ResourcesExtension.ResourceClassGeneration.Always
}
