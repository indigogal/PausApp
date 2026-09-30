# AGENTS.md

## Project overview

**PausApp** — a single-module Android app (Kotlin + Jetpack Compose, Material 3) for guided stretching routines with daily streak ("racha") tracking. The entire UI is in **Spanish**; new UI copy must be Spanish to match.

Tech stack: AGP 9.3.2, Gradle wrapper 9.5.0, Kotlin 2.2.10 (Compose plugin), KSP for Room 2.8.5, Navigation Compose 2.7.7, Media3 ExoPlayer, Glance (declared in deps; no widget code on master yet).

## Commands (verified working)

```bash
./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:testDebugUnitTest      # run unit tests (JUnit; requires JVM 17)
./gradlew --console=plain :app:testDebugUnitTest  # quiet output
```

- Java **17** (Temurin) with Kotlin jvmToolchain(17) is required. `compileSdk 37`, `minSdk 29`, `targetSdk 34`.
- `gradle.properties` disables configuration cache (`org.gradle.configuration-cache=false`) — don't "fix" it.
- No CI config exists (no `.github/`). Feature branches (`NavController`, `ExercisePage`, ...) are merged into `master`; origin is the only remote.

## Code organization

```
app/src/main/java/com/github/indigogal/pausapp/
├── MainActivity.kt          # single activity: NavHost + ViewModels
├── RachaPage.kt             # RachaScreen (streak home), StreakCalendar, calculateStreakDays
├── RegisterScreen.kt        # RegisterForm (registration + reminder time picker)
├── Exercise_Page.kt         # ExerciseScreen (active exercise UI, timer, video)
├── DEPRECATED_ExercisePage.kt  # old static exercise UI; reference only — do not use
├── Open_Buttons.kt          # legacy nav-button helper; contains DEAD route "racha/$nombre/$numDias"
├── model/                   # Exercise, ExerciseSet (pure data)
├── data/                    # User entity + UserDAO, AppDatabase, Converters, ExerciseRepository, FakeUserDAO
├── viewmodel/               # UserViewModel (+Factory), ExerciseViewModel
└── ui/theme/                # Color.kt (Material Design color schemes), Theme.kt, Type.kt
```

Screen files live in the package root with `Name_Page.kt` filenames; the exported composables are `NameScreen`/`NameForm`. Everything else is namespaced.

## Control / data flow

1. **Startup** (`MainActivity`): DB singleton `AppDatabase.getInstance()` → `UserDAO` → `UserViewModel` (via custom `UserViewModelFactory`). `ExerciseViewModel` is obtained with Compose `viewModel()` (it's an `AndroidViewModel`). If a registered user exists, a `LaunchedEffect` jumps `register` → `racha` (`popUpTo("register") { inclusive = true }`).
2. **Registration** (`RegisterScreen`): writes `User(uid = 1, streakStart = null, streakEnd = null, reminderTime = ...)` via `userVM.addUser(...)` (which also calls `deleteTempUser()`), navigates to `racha`.
3. **Home** (`RachaScreen`): reads streak from `UserViewModel.user` StateFlow; "Iniciar rutina" → `exerciseViewModel.loadRandomExerciseSet()` + navigate `exercise`.
4. **Exercise** (`ExerciseScreen`): renders `ExerciseSet` from `ExerciseViewModel.currentExerciseSet`; per-exercise **fixed 20-second** timer (loop of `delay(16)` + `System.currentTimeMillis` deltas; see gotchas); video loops under it via `REPEAT_MODE_ONE`. When finished → calls `userViewModel.completeRoutine()` and `popBackStack()`.

## Key patterns & conventions

- **Temp-user pattern**: `User.uid == 0` marks an unregistered placeholder; `isRegistered = uid != 0`. DAO `getUser()` filters `uid != 0 LIMIT 1`; `deleteTempUser()` removes the placeholder after registration.
- **Streak semantics** (core logic in `UserViewModel.completeRoutine` and top-level `calculateStreakDays` in RachaPage.kt, under test):
  - `streakStart`/`streakEnd` are **null until the first routine is completed**.
  - Completing today when `streakEnd == today` or `streakEnd + 1 day == today` keeps the streak; any other gap starts a fresh streak at today.
  - `calculateStreakDays` returns 0 for nulls, invalid ranges (end < start), or when more than 1 day has elapsed since `streakEnd`, and counts inclusively.
- **ViewModels** expose `StateFlow`/`MutableStateFlow` (no Flows/Room-returned flows), launch DB work on `Dispatchers.IO`.
- **ExerciseRepository** is an `object` that caches its exercise list in memory on first load; scans `assets/` for `*.mp4` (subfolder `exercises/` if it exists, otherwise the assets root — the root fallback is currently the active path), measures duration via `MediaMetadataRetriever` (fallback 10 s), sorts by name, and `createRandomExerciseSet` picks 3 shuffled.
- **Previews**: stateless overloads of screens take the data (e.g. `RachaScreen(user, ...)`) so previews don't need ViewModels. `RegisterScreen`'s preview was removed (TODO comment); restore it the way RachaPage does it if you need one.

## Gotchas

- **Media3 versions are duplicated/inconsistent**: `gradle/libs.versions.toml` pins media3 `1.4.1` (used by `media3-exoplayer`/`media3-ui`), while `app/build.gradle.kts` hardcodes `val media3_version = "1.11.1"` for the `media3-ui-compose` artifacts. Change both in lockstep; bumping/hand-editing one side alone can break the build.
- **Exercise timer**: `totalTimeSeconds = 20` with a stale comment claiming 30. The timer state is keyed on `remember(currentExercise)`; if you change durations, keep the delta-time loop (real elapsed time, not tick counting), which is what makes it accurate.
- **Assets**: video filenames contain Spanish accents and spaces (`Estiramiento de cuello.mp4`). `asset:///` prefix is added at runtime in `ExerciseScreen`; paths come from `exercise.assetPath` — match the `assets/` names exactly.
- **App DB is `userDB`** (Room). The untracked `kls_database.db` SQLite file in the repo root is a stray leftover, not the app's database — leave it alone.
- **Fonts come from Google Play Services** (`com.google.android.gms.fonts` provider): "Workbench" for display, "IBM Plex Mono" for body. They are not bundled; on devices without the fonts service they won't render.
- **Dead route**: `Open_Buttons.kt` navigates to `"racha/$nombre/$numDias"` which has no `composable` in MainActivity's NavHost (routes: `register`, `racha`, `exercise`). This file is legacy; don't build on it.
- `Exercise.id` and `Exercise.durationSeconds` are typed as `Number`, not `Int` — match that if you touch the models.
- Dynamic color is enabled (`dynamicColor = true` on Android 12+); theme colors defined via Material Design color schemes in `ui/theme/Color.kt`.

## Testing

- Only plain JUnit unit tests exist under `app/src/test/` (no Android instrumented tests of substance; Espresso deps are declared but unused).
- `RachaStreakLogicTest` covers `calculateStreakDays` (top-level function in RachaPage.kt) — the only real test suite. Add streak-edge-case tests there.
- Run with `./gradlew :app:testDebugUnitTest` (verified passing).