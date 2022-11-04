package fudge.notenoughcrashes.stacktrace;

import fudge.notenoughcrashes.NecConfig;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.ModsByLocation;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModIdentifier {

    private static final Map<CrashReport, Set<CommonModMetadata>> suspectedModsCache = new HashMap<>();

    private static final Map<IMixinConfig, Set<CommonModMetadata>> mixinConfigToModsCache = new HashMap<>();

    public static Set<CommonModMetadata> getSuspectedModsOf(CrashReport report) {
        return suspectedModsCache.computeIfAbsent(report, (ignored) -> identifyFromStacktrace(report.getCause()));
    }

    @NotNull
    private static Set<CommonModMetadata> identifyFromStacktrace(Throwable e) {
        Set<CommonModMetadata> mods = new HashSet<>();
        // Include suppressed exceptions too
        visitChildrenThrowables(e, throwable -> {
            for (var newMod : identifyFromThrowable(throwable)) {
                if (mods.stream().noneMatch(mod -> mod.id().equals(newMod.id()))) {
                    mods.add(newMod);
                }
            }
        });
        return mods;
    }

    private static void visitChildrenThrowables(Throwable e, Consumer<Throwable> visitor) {
        visitor.accept(e);
        for (Throwable child : e.getSuppressed()) visitChildrenThrowables(child, visitor);
    }

    private static Set<CommonModMetadata> identifyFromThrowable(Throwable e) {
        ModsByLocation modMap = NecPlatform.instance().getModsAtLocationsInDisk();

        Set<String> involvedClasses = new LinkedHashSet<>();
        Set<IMixinInfo> involvedMixins = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                involvedClasses.add(element.getClassName());
                involvedMixins.add(getMixinInfo(element));
            }
            e = e.getCause();
        }

        Set<CommonModMetadata> mods = new LinkedHashSet<>();
        for (String className : involvedClasses) {
            Set<CommonModMetadata> classMods = identifyFromClass(className, modMap);
            mods.addAll(classMods);
        }
        for (IMixinInfo mixinName : involvedMixins) {
            if (mixinName == null) continue;
            Set<CommonModMetadata> mixinMods = identifyFromMixin(mixinName);
            mods.addAll(mixinMods);
        }
        debug(modMap::toString);
        return mods;
    }

    private static final boolean FORCE_DEBUG = false;

    private static void debug(Supplier<String> message) {
        if (FORCE_DEBUG || NecConfig.instance().debugModIdentification) NotEnoughCrashes.getLogger().info(message.get());
    }

    @NotNull
    private static Set<CommonModMetadata> identifyFromClass(String className, ModsByLocation modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            debug(() -> "Ignoring class " + className + " for identification because it is a mixin class");
            return Collections.emptySet();
        }

        try {
            // Get the URL of the class
            Class<?> clazz = Class.forName(className);
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                debug(() -> "Ignoring class " + className + " for identification because the code source could not be found");
                return Collections.emptySet(); // Some internal native sun classes
            }
            URL url = codeSource.getLocation();

            if (url == null) {
                NotEnoughCrashes.getLogger().warn("Failed to identify mod for " + className);
                return Collections.emptySet();
            }

            // Get the mod containing that class
            Set<CommonModMetadata> mods = getModsAt(Paths.get(url.toURI()), modMap);
            if (NecConfig.instance().debugModIdentification && !mods.isEmpty()){
                debug(() -> "Successfully placed blame of '" + className + "' on '"
                        + mods.stream().findFirst().get().name() + "'");
            }
            return mods;
        } catch (URISyntaxException | ClassNotFoundException | NoClassDefFoundError e) {
            debug(() -> "Ignoring class " + className + " for identification because an error occurred");
            if (NecConfig.instance().debugModIdentification) {
                e.printStackTrace();
            }
            return Collections.emptySet(); // we cannot do it
        }
    }

    @Nullable
    private static MixinMerged findMixinMerged(StackTraceElement element) {
        try {
            Class<?> clazz = Class.forName(element.getClassName());
            // Walk through methods because we don't know parameter types
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(element.getMethodName())) {
                    MixinMerged mixinMerged = method.getAnnotation(MixinMerged.class);
                    if (mixinMerged != null) {
                        return mixinMerged;
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    @NotNull
    private static Set<CommonModMetadata> identifyFromMixin(IMixinInfo mixin) {
        return mixinConfigToModsCache.computeIfAbsent(mixin.getConfig(), config -> {
            String mixinFileName = config.getName();
            Set<CommonModMetadata> modsWithMixinFile = new LinkedHashSet<>();
            for (CommonModMetadata mod : NecPlatform.instance().getAllMods()) {
                Path mixinFile = mod.rootPath().resolve(mixinFileName);
                if (Files.exists(mixinFile)) {
                    modsWithMixinFile.add(mod);
                }
            }
            return modsWithMixinFile;
        });
    }

    @Nullable
    private static IMixinInfo getMixinInfo(StackTraceElement element) {
        MixinMerged mixinMerged = findMixinMerged(element);
        if (mixinMerged != null) {
            // Mixin does a great job of obscuring mixins - see the "Reflection" javadoc for more info
            ClassInfo classInfo = ClassInfo.forName(mixinMerged.mixin().replace('.', '/'));
            if (classInfo != null) {
                return Reflection.getMixinInfo(classInfo);
            }
        }
        return null;
    }

    @NotNull
    private static Set<CommonModMetadata> getModsAt(Path path, ModsByLocation modMap) {
        Set<CommonModMetadata> mod = modMap.get(path);
        if (mod != null) return mod;
        else if (NecPlatform.instance().isDevelopmentEnvironment()) {

            // For some reason, in dev, the mod being tested has the 'resources' folder as the origin instead of the 'classes' folder.
            String resourcesPathString = path.toString().replace("\\", "/")
                    // Make it work with Architectury as well
                    .replace("common/build/classes/java/main", "fabric/build/resources/main")
                    .replace("common/build/classes/kotlin/main", "fabric/build/resources/main")
                    .replace("classes/java/main", "resources/main")
                    .replace("classes/kotlin/main", "resources/main");
            Path resourcesPath = Paths.get(resourcesPathString);
            return modMap.getOrEmpty(resourcesPath);
        } else {
            debug(() -> "Mod at path '" + path.toAbsolutePath() + "' is at fault," +
                    " but it could not be found in the map of mod paths: " /*+ modMap*/);
            return Collections.emptySet();
        }
    }

    /**
     * Mixin is rather protective of its internal mixin structures.
     * <p>
     * Mixin has some useful classes:
     * <ul>
     *     <li>{@link ClassInfo} - Represents a class</li>
     *     <li>{@link IMixinInfo} - Represents a mixin</li>
     * </ul>
     * The issue arises when we have a {@link ClassInfo} of a mixin and want to get its {@link IMixinInfo}.
     * The ClassInfo class has a {@link ClassInfo#mixin mixin} field, used to link an IMixinInfo to a mixin's ClassInfo.
     * However, this field is private and there is no getter for it. We would have to use reflection to get it.
     * <p>
     * Another option would be to get the ClassInfo of the mixin's target class to get mixins into that class.
     * This has a problem, though, when Mixin does not consider the mixin to have been applied to the target class.
     * Since it only has a getter for applied mixins and not all mixins, reflection is the only option.
     * <p>
     * Therefore, the easiest option is to use reflection to get the IMixinInfo from a mixin's ClassInfo.
     */
    private static class Reflection {
        static final Field classInfoMixin;

        static {
            try {
                classInfoMixin = ClassInfo.class.getDeclaredField("mixin");
                classInfoMixin.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @Nullable
        static IMixinInfo getMixinInfo(ClassInfo classInfo) {
            try {
                return (IMixinInfo) classInfoMixin.get(classInfo);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
