package fudge.notenoughcrashes.forge.platform;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.ModsByLocation;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ForgePlatform implements NecPlatform {
    @Override
    public boolean isForge() {
        return true;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public ModsByLocation getModsAtLocationsInDisk() {
        Map<Path, Set<CommonModMetadata>> modMap = new HashMap<>();
        var mods = ModList.get();
        if (mods == null) return new ModsByLocation(modMap);

        for (IModFileInfo modFile : mods.getModFiles()) {
            Path modJar = modFile.getFile().getFilePath();
            for (IModInfo modInfo : modFile.getMods()) {
                modMap.computeIfAbsent(modJar, f -> new HashSet<>()).add(toCommon(modInfo));
            }
        }

        return new ModsByLocation(modMap);
    }

    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public @Nullable InputStream getResource(Path relativePath) {
        return NecPlatform.class.getResourceAsStream("/" + relativePath.toString());
    }

    @Override
    public List<CommonModMetadata> getModMetadatas(String modId) {
        IModFileInfo file = ModList.get().getModFileById(modId);
        if (file == null) return Collections.emptyList();
        return file.getMods().stream().map(ForgePlatform::toCommon).collect(Collectors.toList());
    }

    @Override
    public List<CommonModMetadata> getAllMods() {
        return ModList.get().getMods().stream()
                .map(ForgePlatform::toCommon)
                .collect(Collectors.toList());
    }

    @Override
    public boolean modContainsFile(CommonModMetadata mod, String path) {
        if (Files.isDirectory(mod.rootPath())) {
            return Files.exists(mod.rootPath().resolve(path));
        }

        try (FileSystem fs = FileSystems.newFileSystem(mod.rootPath())) {
            Path filePath = fs.getPath(path);
            return Files.exists(filePath);
        } catch (IOException e) {
            NotEnoughCrashes.getLogger().error("Failed to open mod jar, assuming it doesn't contain file " + path, e);
            return false;
        }
    }

    private static CommonModMetadata toCommon(IModInfo imod) {
        Optional<String> issueUrl = imod.getOwningFile().getConfig().getConfigElement("issueTrackerURL");
        Object authorsObj = imod.getConfig().getConfigElement("authors").orElse(null);
        List<String> authors = authorsObj instanceof String ? Collections.singletonList((String) authorsObj) : (List<String>) authorsObj;
        return new CommonModMetadata(imod.getModId(),
                imod.getDisplayName(),
                issueUrl.orElse(null),
                authors,
                imod.getOwningFile().getFile().getFilePath()
        );
    }
}
