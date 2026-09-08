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

package com.skydoves.nowinandroid.core.common.network

import dev.zacsweers.metro.Qualifier
import kotlinx.coroutines.CoroutineDispatcher

/**
 * The dispatcher used for disk and network IO.
 *
 * The Android original used a single parameterised `@Dispatcher(IO)` qualifier. Metro resolves
 * qualifiers structurally, so two marker annotations read more clearly at the injection site and
 * avoid depending on qualifier-argument equality.
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** The dispatcher used for CPU bound work. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

/** A [kotlinx.coroutines.CoroutineScope] that lives as long as the application process. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

/**
 * `Dispatchers.IO` only exists on the JVM, so each platform contributes its own definition of
 * "somewhere it is safe to block".
 */
expect val ioDispatcher: CoroutineDispatcher
