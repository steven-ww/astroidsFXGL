# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Build, run, format, and test

This is a Gradle-based Java/JavaFX project targeting Java 25 via the Gradle toolchain (see `build.gradle`). Use the Gradle wrapper for all commands from the project root.

- **Run the game** (primary command during development)
  - macOS/Linux: `./gradlew run`
  - Windows: `gradlew.bat run`

- **Build the project** (compiles and runs tests)
  - `./gradlew clean build`

- **Run tests only**
  - All tests: `./gradlew test`
  - Single test class (when tests exist): `./gradlew test --tests 'za.co.webber.asteroidsfxgl.SomeTestClass'`

- **Code formatting / linting (Spotless)**
  - Check formatting: `./gradlew spotlessCheck`
  - Auto-format Java sources: `./gradlew spotlessApply`

Prerequisites from `README.md`: JDK 21+ with JavaFX support and Gradle (or an IDE like IntelliJ that can import and run the Gradle project).

## High-level architecture

The codebase is a small FXGL-based Asteroids-style game. Most logic is in a single FXGL `GameApplication` subclass plus a set of entity factories and components.

- **Entry point & game loop**
  - `za.co.webber.asteroidsfxgl.MainApp` extends `GameApplication` and is the main entry point (`application.mainClass` in `build.gradle`).
  - Key lifecycle overrides:
    - `initSettings` – sets window title and resolution.
    - `initUI` – configures the black background and a simple debug text bound to the `pixelsMoved` game variable.
    - `initGame` – registers entity factories, spawns the player at screen center, initializes HUD (lives/score), and spawns an initial off-screen asteroid.
    - `initPhysics` – configures collision handling between `PLAYER` and `ASTEROID`, including life loss, respawn timing, and game-over notification.
    - `initInput` – binds WASD + SPACE to ship rotation, thrust, and shooting using FXGL’s input DSL.
    - `initGameVars` – initializes world properties (currently just `pixelsMoved`).

- **Entity types and factories**
  - `EntityType` enum defines the three core types: `PLAYER`, `ASTEROID`, `BULLET`.
  - Factories under `components/` are responsible for constructing entities and wiring components:
    - `PlayerFactory` (`@Spawns("player")`) builds the player ship as an outlined "A"-shaped `Path` with a `Polyline` thrust flame, adds a `CollidableComponent`, and attaches `PlayerComponent`.
    - `AsteroidFactory` (`@Spawns("asteroid")`) builds a large irregular `Polygon` rock, adds collision, and attaches `AsteroidComponent`.
    - `BulletFactory` is a static helper (not an `EntityFactory`) that builds `BULLET` entities given a spawn position, rotation, and current ship velocity. Bullets are manually added to the world from `MainApp`’s input handler.

- **Gameplay components**
  - `PlayerComponent`
    - Maintains ship velocity (as `Vec2`), handles rotation (`turnLeft`/`turnRight`), thrust (including showing/hiding the flame), screen-wrapping at 1280×720, and invincibility logic after respawn (timed with a blinking opacity effect).
    - Implements `explode()` to replace the ship with three drifting/fading line fragments using `DriftAndFadeComponent`, and temporarily disables collisions.
    - `respawn(x, y)` recreates the ship visuals, resets velocity, re-enables collisions, and activates a short invincibility window.
    - Provides helper methods `getNosePosition(distance)`, `getVelocity()`, and `getRotation()` for bullet spawning.
  - `AsteroidComponent`
    - On creation, computes a velocity vector that roughly points toward the screen center with a random angular offset and speed, plus a random spin rate.
    - On update, translates and rotates the asteroid and performs screen wrapping with a margin beyond the view borders.
  - `BulletComponent`
    - Sets bullet velocity to a fast forward vector in the ship’s facing direction plus the ship’s current velocity (so bullets inherit ship movement).
    - On update, moves the bullet, wraps it around the screen like the player, and removes it after a short lifetime.
  - `DriftAndFadeComponent`
    - Used for explosion fragments. Moves the fragment with a given velocity and spin, gradually reduces opacity over its configured lifetime, then removes the entity.

- **HUD and UI**
  - `hud.HudDisplay` is a static utility for drawing and updating the HUD:
    - `drawLives(int lives)` – shows a row of small ship icons (excluding the current ship) in the upper-left, tagged via `Node.setUserData("LIFE")` so they can be cleared before redrawing.
    - `drawScore(int score)` – renders a monospaced score text in the upper-left, also tagged as `"SCORE"` for easy replacement.
  - `MainApp` calls these helpers when initializing and whenever lives change.

## FXGL-specific notes

- FXGL entity creation is split between:
  - Declarative factories (`PlayerFactory`, `AsteroidFactory`) registered with `getGameWorld().addEntityFactory(...)` and used via `FXGL.spawn("player"/"asteroid", ...)`.
  - Ad-hoc builders (`BulletFactory`, `DriftAndFadeComponent`’s fragments) that use `FXGL.entityBuilder()` and attach entities directly.
- Screen size assumptions (1280×720) are duplicated in several components for wrapping logic; if you change resolution in `MainApp.initSettings`, also update wrapping code in `PlayerComponent`, `AsteroidComponent`, and `BulletComponent`.
- The `README.md` includes a step-by-step outline for evolving the game (entities, input, collisions, HUD, audio, etc.) based on FXGL docs. When adding new features, prefer following those idiomatic FXGL patterns and consult the FXGL wiki and samples as referenced there.
