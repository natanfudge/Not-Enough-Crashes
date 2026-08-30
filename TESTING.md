# Testing Not Enough Crashes
This document describes the production and development crash scenarios for Not Enough Crashes.

## Setup

- Build Not Enough Crashes and the active Fabric test fixture with `./gradlew clean build` (`gradlew.bat clean build` on Windows).
- For production-client tests, install both `fabric/build/libs/notenoughcrashes-fabric-4.4.9+26.2.jar` and `TestFabricMod/build/libs/nec_testmod-1.0.0.jar` in a Minecraft 26.2 Fabric Loader 0.19.5 client.
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
    - Click 'Get link' and verify in the log:
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
    - Verify in the **Log**, **TXT File**, and **Get Link Site**:
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
  - Click 'Get link' and verify in the log:
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
