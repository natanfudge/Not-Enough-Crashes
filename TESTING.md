# Testing Not Enough Crashes
This document describes the production and development crash scenarios for Not Enough Crashes.

## Setup

- Build Not Enough Crashes and the active Fabric test fixture with `./gradlew clean build` (`gradlew.bat clean build` on Windows).
- For production-client tests, install both `fabric/build/libs/notenoughcrashes-fabric-4.5.0+26.3.jar` and `TestFabricMod/build/libs/nec_testmod-1.0.0.jar` in a Minecraft 26.3 Fabric Loader 0.19.5 client.
- For development tests, pass `-PincludeTestMod` to the Fabric run task, for example `./gradlew :fabric:runClient -PincludeTestMod` or `./gradlew :fabric:runServer -PincludeTestMod`.
- Run every scenario in a fresh process. Set the scenario by writing the documented value to `config/nec_test_mode.txt`; use an empty file for key-triggered scenarios.
- `TestForgeMod` remains a legacy Forge fixture and is not part of the active build.


## Crash Handling

### Client Initialization Crashes [Fabric-Only]
- In the game config folder, have a file named `nec_test_mode.txt` with the content `init_crash`.
  - Start the game
  - Verify:
    - The game crashes.
    - The crash screen appears.
    - Wait at least five seconds and verify the crash screen remains visible instead of being replaced by the title screen. This specifically covers the `Gui.setScreen` guard.
    - 'Not Enough Crashes Test Mod' is blamed.
    - In the terminal, no information is repeated.
    - Click 'Show crash log' and verify in the log:
      - No information is repeated.
      - System details are present.
      - Suspected Mods: Not Enough Crashes Test Mod (nec_testmod) (may contain ignored mods)

  - Click 'Quit Game' and verify the game closes without any exceptions being logged.
### Initialization Suppressed Exception [Fabric-Only]
- In the game config folder, have a file named `nec_test_mode.txt` with the content `suppressed_crash`.
  - Start the game
  - Verify:
    - The 'Test Main Exception' exception is present in the log as a normal exception.
    - The 'Test Suppressed Exception' exception is present in the log as a suppressed exception.

  - Click 'Quit Game' and verify the game closes without any exceptions being logged.
### Integrated Server Crashes
- In the game config folder, have a file named `nec_test_mode.txt` with the content `server_crash`.
  - Open a Minecraft World.
  - Verify:
    - The game crashes.
    - The crash screen appears.
    - "Not Enough Crashes Test Mod" is blamed.
    - In the terminal, no information is repeated.
    - Verify in the **Log**, **TXT File**, and **saved local report**:
      - No information is repeated.
      - System details are present.
      - Suspected Mods: Not Enough Crashes Test Mod (nec_testmod) (may contain ignored mods)
      - Client Crashes Since Restart: 0
      - Integrated Server Crashes Since Restart: 1
    - Verify the txt file has a -server ending

  - Click 'Back to title screen' and re-enter the world.
  - Verify the game is working fine.

### Dedicated Server Crashes [Can be tested in dev only]
- In the game config folder, have a file named `nec_test_mode.txt` with the content `server_crash`.
  - Start the server
  - Verify:
    - The game crashes.
    - In the terminal, no information is repeated.
    - Verify a -server crash report is generated with:
      - Suspected mods: Not Enough Crashes Test Mod (nec_testmod) (may contain ignored mods)
      - No information is repeated.
      - System details are present.
### Client Reported Crashes
- In the game config folder, have the file named `nec_test_mode.txt` be empty or non-existent.
- Open a Minecraft world.
- Press the left square bracket key.
- Verify:
  - The game crashes.
  - The crash screen appears.
  - 'Not Enough Crashes Test Mod' is blamed (and maybe fabric lifecycle events).
  - Click 'Show crash log' and verify in the log:
    - No information is repeated.
    - System details are present.
    - Suspected Mods: Not Enough Crashes Test Mod (nec_testmod) (may contain ignored mods)
    - Client Crashes Since Restart: 1
    - Integrated Server Crashes Since Restart: 0
- Click 'Back to title screen' and re-enter the world.
- Verify the game is working fine.

### Client Unreported Crashes
- In the game config folder, have the file named `nec_test_mode.txt` be empty or non-existent.
- Open a Minecraft world.
- Press the right square bracket key.
- Verify:
  - The game crashes.
  - The crash screen appears.
  - 'Not Enough Crashes Test Mod' is blamed.
  - Verify in the **Log** and **TXT File**:
    - `Test Unreported Game Loop Crash` is present.
    - No information is repeated.
    - System details are present.
    - Client Crashes Since Restart: 1
    - Integrated Server Crashes Since Restart: 0
- Click 'Back to title screen' and re-enter the world.
- Verify the game is working fine.


## Automated 26.3 maintenance scenarios

Build with `gradlew.bat :common:test :fabric:build :neoforge:build :TestFabricMod:build`. Use an isolated game directory with Fabric API and the Fabric fixture installed. Write one of these values to `config/nec_test_mode.txt`, then start a fresh client:

| Mode | Check |
| --- | --- |
| `maintenance_title` | Recover at the title screen; startup event fires once; normal exit. |
| `maintenance_client` | Client tick crash, disconnect, return to title, rejoin. |
| `maintenance_render` | Throw from a Mixin during rendering with a GPU surface acquired; readable crash screen and visible player after rejoin. |
| `maintenance_screen` | Recover from a custom screen whose rendering always throws. |
| `maintenance_entity` | Recover from a server-player ticking exception and rejoin. |
| `maintenance_server` | Recover from an integrated-server exception and rejoin. |
| `maintenance_stack` | Recover from a simulated `StackOverflowError` and rejoin. |
| `maintenance_export` | Exercise the export screen; opt into configs/world; inspect ZIP entries; rejoin. |
| `maintenance_links` | With `modpackIssueTracker` set, exercise modpack and suspected-mod link confirmation screens. |
| `maintenance_diagnostics` | Exercise command failure, missing loot/template, invalid spawn, feature, structure, processor and finalizer diagnostics. |
| `maintenance_multiplayer` | Connect to a local server on `127.0.0.1:25569`, crash, assert disconnection, reconnect and render the player. |
| `maintenance_quit` | Start and exit without a crash. |

Successful scenarios print `NEC_TEST PASS`; failure or a 180-second timeout prints `NEC_TEST FAIL`. Screenshots are saved under `screenshots/`. Check both markers and process exit status. `maintenance_stack` simulates the throwable type; it does not exhaust native thread stack space.

For NeoForge, build `:neoforge:testmodJar` and install `neoforge/build/libs/nec-neoforge-test-fixture-1.0.0.jar` in the test client's mods folder. It shares the same scenarios except Fabric initialization tests and `maintenance_server`. Test fixtures are excluded from the published jars. `TestForgeMod` is historical and is not used for 26.3.

For a multiplayer test, use a separate 26.3 server bound to loopback with port 25569 and both `online-mode=false` and `white-list=false`. Confirm its log records disconnection immediately after the client crash and a second successful join. Stop it after testing.

### Unit tests

`:common:test` checks UTF-8 upload bodies, response/error handling, Mixin source-map ranges and throwable cycles, ZIP opt-in boundaries and symbolic-link exclusion. Upload tests use a local HTTP server; they do not create public pastes.

### 2026-10-13 verification

- Baseline 4.4.9+26.3 reproduced repeated `CLIENT_STARTED` after a caught crash. The new loop recovery fires it once.
- Fabric production client: title, render, custom screen, entity, integrated-server, simulated stack-overflow, export, links and diagnostics checks passed. Recovery clears world, server and connection references; screenshots show readable menus and a visible player after rejoin; processes exit normally.
- Manually verified Fabric initialization error screen, export navigation, link confirmation/cancellation, and Quit. Startup failures intentionally exit with status -1.
- Live synthetic pastes.dev request round-tripped UTF-8 text. No real crash report was uploaded in that test.
- Compatibility set: Fabric API 0.162.0+26.3, FastBack 0.35.0+26.3.0, Puzzles Lib 26.3.14, Forge Config API Port 26.3.1, MiniHUD 0.41.2, MaLiLib 0.30.2, C2ME 0.4.2-alpha.0.90+26.3, Distant Horizons 3.3.4-26.3, BisectHosting Menu 3.0.0, and Sodium 0.9.2. Render crash, rejoin and process exit passed on Vulkan.
- Multiplayer: a separate 26.3 Fabric server recorded immediate disconnect after a client crash and a second successful join; client screenshot showed the player after rejoining; client exited 0.
- Packaged NeoForge 26.3.0.8-beta: render crash/rejoin, suspected-mod and modpack link buttons, and all diagnostic context checks passed; processes exited normally.
- All runtime testing uses Minecraft 26.3. Historical modpacks and Android/Kirin hardware were not tested. Native GPU/JVM failures cannot be recovered as Java exceptions.

- Final 4.5.0 build and seven unit tests passed. Both release jars were checked for valid translations, declared Mixin classes, and absence of test fixtures and legacy upload code.
- The exact resource-pack attachment from #135 is a RAR archive named .zip. Minecraft 26.3 rejects it as an invalid ZIP and removes it from the selected packs; render recovery still passed.
- BisectHosting Menu 3.0.0: manually opened Multiplayer, clicked the hosting entry, waited for its player-count screen, and returned successfully.
