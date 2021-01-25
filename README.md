# Not Enough Crashes
[![Discord](https://img.shields.io/discord/219787567262859264.svg)](https://discord.gg/CFaCu97)
[![CurseForge](http://cf.way2muchnoise.eu/not-enough-crashes.svg)](https://minecraft.curseforge.com/projects/not-enough-crashes)

Not Enough Crashes is a fork of TooManyCrashes, a port of VanillaFix's crash improvements to Fabric, for Minecraft 1.15+.
The original license is available at LICENSE_TMC. 

Features: 
- Special crash screen when game fails to start (not in dev).
- Deobfuscates stack trace (not in dev).
- Allows going back to the title screen after crashing in game (in dev too).
- Additional NBT information of entities and block entities in crash reports (in dev too).

# Using the Not Enough Crashes API
You can include the 4KB Not Enough Crashes API to reset any mod state that needs to be cleared by running disposing code whenever the game crashes. You don't need to check if Not Enough Crashes itself is present.
## Gradle Setup
```groovy
dependencies {
    modImplementation ("com.lettuce.fudge:notenoughcrashes-api:1.1.0")
    include ("com.lettuce.fudge:notenoughcrashes-api:1.1.0")
}
```
## Code
```java
class MyModState {
    private static List<String> dataThatIWantToReset = new ArrayList<>();
    
    static {
        MinecraftCrashes.onEveryCrash(() -> dataThatIWantToReset.clear());
    }
}
```
You can also use `MinecraftCrashes.onNextCrash` to only call the code once in the next crash. For more information consult the javadocs of `MinecraftCrashes`.


The mod is useful in development environments too:
# Using Not Enough Crashes in a development environment
```groovy
repositories {
    // [...]
    jcenter()
}
```

```groovy
dependencies {
    modRuntime ("com.lettuce.fudge:notenoughcrashes:2.2.0+1.16.5")
}
```

