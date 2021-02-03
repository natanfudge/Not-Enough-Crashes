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
