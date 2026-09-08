<h1 align="center">Now in Android KMP</h1>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://kotlinlang.org/docs/multiplatform.html"><img alt="Kotlin Multiplatform" src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg"/></a>
  <a href="https://github.com/skydoves"><img alt="Profile" src="https://skydoves.github.io/badges/skydoves.svg"/></a>
</p>

<p align="center">
📱 Now in Android KMP is a full Kotlin Multiplatform port of Google's Now in Android sample, running the same screens, ViewModels, navigation, and data layer on <b>Android, iOS, desktop, and the browser</b> from one shared codebase.
</p>

> [!IMPORTANT]
> This project is a port of [**Now in Android**](https://github.com/android/nowinandroid), designed and built by the Android team at Google and licensed under Apache 2.0. All of the product design, the app's content, its architecture guidance, and its demo data come from that project. This repository only moves it onto Kotlin Multiplatform; the original is the reference implementation and the place to look for the canonical Android version.

> [!NOTE]
> Every migration decision can be diffed against the source it came from. The upstream app is not
> vendored here, it is its own repository, so clone it wherever you like:
> `git clone https://github.com/android/nowinandroid.git`. The port was made against
> [`12f80da`](https://github.com/android/nowinandroid/commit/12f80da6518e161ed16a06a68e71fb8a873576d6),
> which is the commit to check out for a like-for-like diff.

<p align="center">
<img src="previews/screenshot.png"/>
</p>

## Platforms

The same design system, feature modules, and data layer produce four applications:

| Platform | Module | Run |
|---|---|---|
| Android | `app/androidApp` | `./gradlew :app:androidApp:installDebug` |
| iOS | `app/iosApp` | `./scripts/generate-xcodeproj.sh` then open `app/iosApp/NowInAndroid.xcodeproj` |
| Desktop | `app/desktopApp` | `./gradlew :app:desktopApp:run` |
| Web | `app/webApp` | `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` |

The iOS project is generated from `app/iosApp/project.yml` with [XcodeGen](https://github.com/yonaskolb/XcodeGen), and its "Compile Kotlin" build phase runs `:app:shared:embedAndSignAppleFrameworkForXcode`.

<a href="https://www.android.skydoves.me/">
<img src="https://github.com/user-attachments/assets/e014ce01-3461-40af-bb2a-eb44f3f55f36" width="13%" align="right"/>
</a>

## 📘 Manifest Android Interview

[Manifest Android Interview](https://android.skydoves.me/) is a comprehensive guide designed to enhance your Android development expertise through 108 interview questions with detailed answers, 162 additional practical questions, and 50+ "Pro Tips for Mastery" sections. The interview questions primarily focus on Android development—including the Framework, UI, Jetpack Libraries, and Business Logic—as well as Jetpack Compose, covering Fundamentals, Runtime, and UI.

<a href="https://howcomposeworks.com/">
<img src="https://github.com/user-attachments/assets/0f0f72fc-49ce-48b5-b3dd-f2c04e907f80" width="13%" align="right"/>
</a>

## 📗 Jetpack Compose Mechanisms Book

[Jetpack Compose Mechanisms](https://howcomposeworks.com/) takes you from "how to use Compose" into "how Compose actually works," tracing the AOSP source line by line through the compiler, runtime, and UI layers beneath every Composable, with practical, production-ready examples from the author's own Compose tooling and libraries. It then ties all three layers together into deep, real-world performance tuning, from stability inference to the skip decision. Fully updated for Kotlin 2.4.0 and Compose Compiler 2.4.0. [The Course: Jetpack Compose Mechanisms](https://doveletter.dev/course/compose) with 120+ practical questions with full answers and 240+ interactive assessments, +880 PDF page equivalents will enhance your Compose internals skills, and you can claim the certificate at the end.

<a href="https://github.com/doveletter">
<img src="https://github.com/user-attachments/assets/3ecd2a7b-9713-40cd-8817-fa568271cefa" width="13%" align="right"/>
</a>

## 🕊️ Dove Letter

If you're eager to dive deeper into Kotlin and Android, explore [Dove Letter](https://github.com/doveletter), a private subscription repository where you can learn, discuss, and share knowledge. To get more details about this unique opportunity, check out the [Learn Kotlin and Android With Dove Letter](https://medium.com/@skydoves/learn-kotlin-and-android-with-dove-letter-26265da11903) article.

## Tech stack & Open-source libraries

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), targeting Android, iOS (arm64 + simulator arm64), desktop (JVM), and the browser (wasmJs).
- [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous work, exactly as upstream.
- [Metro](https://github.com/ZacSweers/metro): compile-time dependency injection, replacing Hilt, which has no Kotlin/Native support. One `@DependencyGraph` per platform, every binding resolved at compile time, including the assisted `TopicViewModel` and `InterestsViewModel`.
- [Ktor](https://ktor.io/): the HTTP client, with the engine each platform ships (OkHttp on Android and desktop, Darwin on iOS, `fetch` in the browser). Replaces Retrofit + OkHttp.
- [Sandwich](https://github.com/skydoves/sandwich): every network call returns `ApiResponse`, so an HTTP error and a transport failure are two inspectable results rather than one thrown exception. `changeListSync` logs the typed failure before it unwraps.
- [Room](https://developer.android.com/kotlin/multiplatform/room) 3: the offline-first cache on every platform, FTS search included. The bundled native SQLite driver on Android, iOS and desktop; `androidx.sqlite:sqlite-web` over a Web Worker in the browser.
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore): user preferences, with a kotlinx.serialization `OkioSerializer` in place of the JVM-only protobuf one. The browser has no filesystem, so it stores the same serialized bytes in `localStorage`.
- [Landscapist](https://github.com/skydoves/landscapist): image loading on every target through `LandscapistImage` and its own engine. The engine decodes no SVG and every topic icon is one, so `landscapist-svg`'s `SvgImageDecoder` wraps the raster decoder and rasterises them (AndroidSVG on Android, Skia's `SVGDOM` elsewhere). Loading states use `ShimmerPlugin` from `landscapist-placeholder`, which takes the shape of the image it replaces. Upstream's fixed 80.dp spinner covered half of a news header and was clamped to a ring on a 32.dp topic icon.
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3): the back stack is a list of route objects the app owns. Upstream had already migrated to Navigation 3, so its multi-back-stack `Navigator`/`NavigationState` port over essentially unchanged, list-detail scene strategy and all.
- [Compose Navigation Graph](https://github.com/skydoves/compose-nav-graph): extracts the navigation graph at compile time from `@NavDestination`/`@NavEdge`, so a destination or transition cannot change unnoticed.
- [Compose HotSwan](https://hotswan.dev/): hot reload for the running Android app, wired as an opt-in Gradle flag.
- [Turbine](https://github.com/cashapp/turbine) and [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test) for testing `Flow`.
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON, preferences, and the saved back stack, plus [KSP](https://github.com/google/ksp) for Room and the navigation graph.

## Architecture

The layering is unchanged from [Google's official architecture guidance](https://developer.android.com/topic/architecture) and from the upstream app: a UI layer over a data layer, with a small domain layer of use cases, and dependencies pointing only downward.

<p align="center">
<img src="figure/architecture.png" width="880"/>
</p>

Every module above is a Kotlin Multiplatform module whose `commonMain` holds the real implementation. Platform source sets exist only where a platform genuinely differs, and each one is a named seam rather than a scattering of `if (isAndroid)` checks.

### What was platform-specific, and what replaced it

| Concern | Android original | Here |
|---|---|---|
| DI | Hilt / Dagger | Metro `@DependencyGraph`, one per platform |
| Networking | Retrofit + OkHttp | Ktor + Sandwich, engine per platform |
| Demo data | `assets/` + `AssetManager` | Compose Resources `Res.readBytes("files/…")` |
| Database | Room 2 (Android) | Room 3; `sqlite-bundled` natively, `sqlite-web` in a Web Worker in the browser |
| Preferences | Proto DataStore | DataStore + `OkioStorage`; `localStorage` in the browser |
| Images | Coil 2 | Landscapist + `landscapist-svg` |
| Resources | `R.string` / `R.drawable` | Compose Resources, one `Res` per module |
| Connectivity | `ConnectivityManager` | `ConnectivityManager` / `NWPathMonitor` / interface poll / `navigator.onLine` |
| Time zone | `ACTION_TIMEZONE_CHANGED` | broadcast on Android, poll elsewhere |
| Notifications | `NotificationCompat` | `NotificationCompat` / `UNUserNotificationCenter` / AWT tray |
| Background sync | WorkManager | WorkManager on Android, a mutex-guarded app-scope coroutine elsewhere |
| Deep links | Activity `SavedStateHandle` | app-scoped `DeepLinkStore` written by each platform's entry point |
| Jank tracking | JankStats | `JankMetricsState`, a no-op off Android |
| Open a link | Chrome Custom Tabs | Custom Tabs / `UIApplication.openURL` / `java.awt.Desktop` / `window.open` |

### Running in a browser

The wasm target reuses `commonMain` unchanged; `wasmJsMain` joins the same `nonAndroidMain` source
set as iOS and desktop, so most platform seams are already satisfied. Three things are genuinely
browser-only:

- **The database runs in a Web Worker.** `androidx.sqlite:sqlite-web` is the only wasm driver, and it
  ships the Kotlin half of a worker protocol without the worker, so `app/webApp` supplies one over
  the official SQLite WASM build. That build is compiled with FTS5 and no FTS4, while Room only
  offers `@Fts3`/`@Fts4`, so the worker rewrites `USING FTS4(...)` to `USING fts5(...)`. The app only
  ever asks the FTS tables for `MATCH`, `count(*)` and inserts, which behave the same either way.
- **The database is in memory.** Persisting to OPFS requires the page to be cross-origin isolated,
  so the browser build re-syncs on load instead. Preferences still persist, in `localStorage`.
- **Topic icons are served from this origin.** The demo data points them at a Firebase Storage bucket
  that answers browsers without an `Access-Control-Allow-Origin` header, so the fetch is blocked
  before any decoder sees it. The nineteen icons are copied into the web app and requests rewritten
  to point there. Some news header images come from hosts with the same restriction and fall back to
  the placeholder; fixing those needs a proxy rather than a code change.

### Offline first

Reads come exclusively from the local database, so every screen renders without a network round trip. `changeListSync` fetches the change list since the last sync, deletes what the server deleted, pulls the changed models in batches of 40, and only then advances the stored version. That is the same "git fetch / git pull / move HEAD" shape as upstream, now with the fetch modelled as an `ApiResponse`.

Search is Room's FTS4 index over titles, content, and topic descriptions. FTS4 honours a trailing `*` only, so `"*query*"` is really a prefix match; that is equally true on Android, and the upstream spelling is kept so behaviour matches.

## Building

```bash
# Android
./gradlew :app:androidApp:installDebug

# Desktop
./gradlew :app:desktopApp:run

# Desktop installers (dmg / msi / deb)
./gradlew :app:desktopApp:packageDistributionForCurrentOS

# Web (wasmJs), served at localhost:8080
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun

# iOS: generate the Xcode project, then build or open it
./scripts/generate-xcodeproj.sh
open app/iosApp/NowInAndroid.xcodeproj
```

Requires JDK 21 (the Gradle daemon toolchain is pinned in `gradle/gradle-daemon-jvm.properties`), the Android SDK, and Xcode plus [XcodeGen](https://github.com/yonaskolb/XcodeGen) for iOS.

> [!NOTE]
> There is no `iosX64` target: Room 3 and `androidx.sqlite` 2.7 stopped publishing it, so the simulator slice is Apple Silicon only and `EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64` is set in the Xcode project.

## Testing

**157 unit tests**, almost all of them in `commonTest`, so the same suite runs on three runtimes:

```bash
./gradlew desktopTest              # 157 tests on the desktop JVM
./gradlew testAndroidHostTest      # 117 of them on the Android host JVM
./gradlew iosSimulatorArm64Test    # 138 of them on the iOS simulator
./gradlew spotlessCheck            # formatting and license headers
./gradlew navCheck                 # navigation graph baselines (all seven modules)
```

They cover the data layer (`changeListSync` incremental sync, deletion, first-run read marking,
notification fan-out), the real Room SQL including the `CASE WHEN` filters and the many-to-many
join, the DataStore serializer, the Ktor + Sandwich success/HTTP-error/transport-failure branches,
all six feature ViewModels, the multi-back-stack `Navigator`, and `NiaAppState` under a real
composition via `runComposeUiTest`.

Three groups cannot run everywhere, and each is in a source set that says so:

| Where | Why |
|---|---|
| `nonAndroidTest`, Room DAO tests | the bundled SQLite driver loads a JNI library an Android *host* test cannot |
| `nonAndroidTest`, `runComposeUiTest` cases | an Android host test has no window to compose into |
| `desktopTest`, sync tests over the bundled demo JSON | Compose Resources needs a `Context` on Android, and a Kotlin/Native test binary is not handed a dependency's resource bundle |

The browser target is deliberately not in that matrix. `wasmJsMain` joins `nonAndroidMain` so it
inherits the shared platform code, but `wasmJsTest` does not join `nonAndroidTest`, whose reason for
existing is Room's bundled SQLite JNI library. The browser has no JNI and no bundled driver.

## Hot reload

[Compose HotSwan](https://hotswan.dev/) reloads Compose changes into the app already running on the
device. Edit a composable, save, and the screen updates in place, keeping the state you had: the
topics you followed, how far you had scrolled, which screen you were on. No rebuild, no reinstall,
no navigating back to where you were.

<p align="center">
<img src="previews/hot-reload.gif" width="820"/>
</p>

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/nowinandroid-kmp/stargazers)__ for this repository. :star: <br>
And __[follow](https://github.com/skydoves)__ me for my next creations! 🤩

## License

```
Designed and developed by 2026 skydoves (Jaewoong Eum)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
