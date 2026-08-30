# Not Enough Crashes
Discord:  [![Discord](https://img.shields.io/discord/219787567262859264.svg)](https://discord.gg/CFaCu97)  
Fabric download:  [![CurseForge](https://cf.way2muchnoise.eu/353890.svg)](https://curseforge.com/minecraft/mc-mods/not-enough-crashes)

NeoForge download:  [![CurseForge](https://cf.way2muchnoise.eu/442354.svg)](https://curseforge.com/minecraft/mc-mods/not-enough-crashes-forge)

Not Enough Crashes improves crashes in Minecraft significantly. For example, it returns the user to the title screen when crashing, instead of closing the game. 

Features: 
- When crashing, you can go back to the title screen and keep playing, without needing to restart.
- A convenient way to submit syntax-highlighted crash reports via a special crash screen.
- Display a list of mods that were involved in the crash, and can be clicked to go to their issue tracker.
- More useful stack traces that are deobfuscated and include additional information such as NBT for mod developers.
- Force crash logs to always appear (forceCrashScreen). [See the configuration guide](Configuring%20Not%20Enough%20Crashes.md).

# Not Enough Crashes API

Not Enough Crashes stopped providing an API for resetting state when the game crashes because of lack of usage. If you need it back, open an issue.

## Building

The build requires Java 25. Run `bash ./gradlew build` (`gradlew.bat build` on Windows); the Gradle wrapper and Java toolchain configuration provide the remaining build tools.
