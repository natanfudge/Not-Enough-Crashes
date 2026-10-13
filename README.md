# Not Enough Crashes
Discord:  [![Discord](https://img.shields.io/discord/219787567262859264.svg)](https://discord.gg/CFaCu97)  
Fabric download:  [![CurseForge](https://cf.way2muchnoise.eu/353890.svg)](https://curseforge.com/minecraft/mc-mods/not-enough-crashes)

NeoForge download:  [![CurseForge](https://cf.way2muchnoise.eu/442354.svg)](https://curseforge.com/minecraft/mc-mods/not-enough-crashes-forge)

Not Enough Crashes improves crashes in Minecraft significantly. For example, it returns the user to the title screen when crashing, instead of closing the game. 

Features: 
- When crashing, you can go back to the title screen and keep playing, without needing to restart.
- Crash reports stay local. Open the saved report, or explicitly upload a copy to [pastes.dev](https://pastes.dev/).
- Export a local reproduction ZIP with mod versions and notes, plus optional configs and the crashed world. Review it before sharing.
- Modpack authors can configure a dedicated issue-tracker button.
- Display a list of mods that were involved in the crash, and can be clicked to go to their issue tracker.
- Crash stack traces show Mixin source filenames and line numbers, plus additional information such as block entity NBT.
- Command exceptions and missing loot/structure data are logged; world-generation reports identify features, structures and processors.
- [Configure crash recovery and modpack reporting](Configuring%20Not%20Enough%20Crashes.md).

# Not Enough Crashes API

Not Enough Crashes stopped providing an API for resetting state when the game crashes because of lack of usage. If you need it back, open an issue.

## Building

The build requires Java 25. Run `bash ./gradlew build` (`gradlew.bat build` on Windows); the Gradle wrapper and Java toolchain configuration provide the remaining build tools.
