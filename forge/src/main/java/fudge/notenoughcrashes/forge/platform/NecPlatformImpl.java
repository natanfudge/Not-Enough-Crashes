package fudge.notenoughcrashes.forge.platform;

import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.ModsByLocation;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NecPlatformImpl implements NecPlatform {


    @Override
    public ModsByLocation getModsAtLocationsInDisk() {
        Map<Path, Set<CommonModMetadata>> modMap = new HashMap<>();

        for (IModFileInfo modFile : ModList.get().getModFiles()) {
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
    public List<CommonModMetadata> getModMetadatas(String modId) {
        IModFileInfo file = ModList.get().getModFileById(modId);
        if (file == null) return Collections.emptyList();
        return file.getMods().stream().map(NecPlatformImpl::toCommon).collect(Collectors.toList());
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
