# Testing Not Enough Crashes
This documents details how to test Not Enough Crashes after making changes.

Apply these tests in the production environment, with the test mod:
- Fabric: `cd TestFabricMod`, Forge: `cd TestForgeMod`
- `gradlew build`
  For dev use:
- `gradlew addTestMods`

## Crash Handling

### Client Initialization Crashes
- In the game config folder, have a file named `nec_test_mode.txt` with the content `init_crash`.
  - Start the game
  - Verify:
    - The game crashes.
    - The crash screen appears.
    - 'Not Enough Crashes Test Mod' is blamed.
    - In the terminal, no information is repeated.
    - Click 'Get link' and verify in the log:
      - No information is repeated.
      - System details are present.
      - Suspected Mods: Not Enough Crashes Test Mod (nec_testmod)

  - Click 'Quit Game' and verify the game closes without any exceptions being logged.
### Integrated Server Crashes
- In the game config folder, have a file named `nec_test_mode.txt` with the content `server_crash`.
  - Open a Minecraft World.
  - Verify:
    - The game crashes.
    - The crash screen appears.
    - No mods are blamed.
    - In the terminal, no information is repeated.
    - Click 'Get link' and verify in the log:
      - No information is repeated.
      - System details are present.
      - Suspected Mods: None
      - Client Crashes Since Restart: 0
      - Integrated Server Crashes Since Restart: 1

  - Click 'Back to title screen' and re-enter the world.
  - Verify the game is working fine.

### Dedicated Server Crashes

### Client Reported Crashes
- Open a Minecraft world.
- Press the left square bracket key.
- Verify:
  - The game crashes.
  - The crash screen appears.
  - 'Not Enough Crashes Test Mod' is blamed (and maybe fabric lifecycle events).
  - Click 'Get link' and verify in the log:
    - No information is repeated.
    - System details are present.
    - Suspected Mods: Not Enough Crashes Test Mod (nec_testmod)
    - Client Crashes Since Restart: 1
    - Integrated Server Crashes Since Restart: 0
- Click 'Back to title screen' and re-enter the world.
- Verify the game is working fine.

### Client Unreported Crashes