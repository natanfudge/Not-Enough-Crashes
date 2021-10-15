## 3.7.0
- Introducing: [Crashy](https://crashy.net/)! Crashy is a crash hosting site designed specifically for Not Enough Crashes and Minecraft crashes in general. 
It shows crashes in an organized GUI that is easily readable, and has some other nice features. [Example](https://crashy.net/2c2vAe5oUVgiNck3NfXU).  
There is now a button for uploading directly to crashy in the crash screen. 
- Fixed UTF-specific characters turning into '?' when uploading crash logs.
- Fixed 'Continuing the game after crashing will cause a crash report to be logged later when the game exits normally'.
### 3.6.5
- Forge for 1.17.1! As this is the first Forge version in a while it may have some issues, so comment if you've encountered anything (or better, open a Github issue).
### 3.6.4
- Added some Quilt-specific features, courtesy of @Siuolthepic!
### 3.6.3
- Quilt now absolutely, officially, works, just as well as Fabric does. 
### 3.6.2
- Fixed text not being localized to English when the translation is not available for the chosen language.
### 3.6.1
- Should now work with the Quilt mod loader, with `deobfuscateStackTrace` set to false in the config.
  Currently, catching initialization errors and deobfuscation is not supported in Quilt.
## 3.6.0
- ~~Should now work with the Quilt mod loader~~ not yet
- Fixed Jar-in-jar mods not being blamed for crashes
## 3.5.0
- No longer depends on Fabric API for localization to work properly.
### 3.4.5
- Fixed some regressions in cleaning up after crash. This fixes not being disconnected from servers.
### 3.4.4
- Fixed integrated server crashes not being caught.
- Fixed state sometimes not being cleaned up properly which could cause the game to infinitely crash.
### 3.4.3
- Fixed deobfuscation not working in dedicated servers, courtesy of @Fourmisain!
### 3.4.2
- Prevented extreme cases where the crash log could become incredibly large. 
- Fixed the crash screen not showing suspected mods in cases where adding them to the crash log was prevented by conflicting mods.
### 3.4.1
- Improved internal error message.
## 3.4.0
- Provided many configuration options for uploading the crash logs, see NecConfig.java, thanks to The456Gamer!
- The config format for uploading crash logs has changed, refer to NecConfig.java for the new format. 
## 3.3.1
- Fixed mod identification not working.
- Fixed deobfuscation sometimes not working.
- Promoted to Release!
## 3.3.0
- Updated to Minecraft 1.17, Java 16. 
- Currently buggy, this is a minimum viable alpha release.
## 3.2.0
- Added a new option `forceCrashScreen` that will prevent cases in which the game closes with no crash log. Instead, the game will crash normally.
### 3.1.9
- Fixed additional crash stack traces appearing when debugModIdentification is false. 
### 3.1.8
- The 1.16.5 Fabric version will no longer deliberately crash in 1.17 snapshots. This means that it may work if nothing broke the mod in a snapshot.
### 3.1.7
- Added a new config option: debugModIdentification, that will hopefully help in discovering mods in more cases.
### 3.1.6
- (Forge) Fixed being unable to identify crashing mods that have multiple authors.
### 3.1.5
- Made it so the Curseforge page can be reached through the Mod Menu entry.
### 3.1.4
- Fixed Forge version not working at all.
### 3.1.3
- Made NEC more compatible with other mods, specifically with [Structure Gel API](https://www.curseforge.com/minecraft/mc-mods/structure-gel-api).
### 3.1.1
- Fixed mod not loading.
## 3.1.0
- Added extra info for feature/structure crashes to make it much easier to find the problematic mod, courtesy of TelepathicGrunt!.
# 3.0.0
- Now supports Forge!
## 2.2.0
- Removed 'feature' that would instantly crash the game when pressing F3+C instead of after 6 seconds, since that hotkey is used to copy location information.
- Added a proper mod icon.
### 2.1.4
- Improved Simplified Chinese localization, courtesy of @WuzgXY!
### 2.1.3
- Fixed "upload crashlog" not working :) .
### 2.1.1
- Fixed "upload crashlog" not working.
## 2.1.0
- Added Estonian localization, courtesy of @Madis0!
# 2.0.0
- Fixed the mod not working in Fabric Loader 0.9.0+, however, this and the following versions will only work for the 0.9.0+ Fabric Loader versions.
### 1.2.4
- Will no longer blame jumploader for errors all the time
### 1.2.3
- Fixed an incompatibility with LambdaControls (mostly just a mistake, not really an incompatibility).
- Crash logs will now be uploaded as gists instead of to the dimdev haste. This can be reverted by setting `uploadCrashLogTo` to `DIMDEV_HASTE` in the config.
- Fixed some GUI bugs in the crash scree
### 1.2.1
- Compatibility with [Multiconnect](https://www.curseforge.com/minecraft/mc-mods/multiconnect/files).  
  Note: Informed Load has yet to publish the compatible version.
## 1.2.0
- Compatibility with [Informed Load](https://www.curseforge.com/minecraft/mc-mods/informed-load).
### 1.1.5
- Fixed a rare error.
### 1.1.4
- Fixed the window not closing when pre-initialization errors occur.
### 1.1.3
- Initialization errors will now be printed as soon as an error is caught, instead of only when displaying the crash screen.
- The init error screen will now display in more disaster cases.
### 1.1.2
- Fixed the entry point catcher not being enabled...
### 1.1.1
- Forgot a debug flag
## 1.1.0
- Added an API for running code when the game crashes to prevent the window getting stuck in weird states.
### 1.0.11
- Fixed two "Not Enough Crashes deobfuscated stack trace" lines appearing (instead of just one).
### 1.0.10
- Fixed an exception while starting game.
# 1.0.9
Released
