package fudge.notenoughcrashes.platform.forge;

import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NecPlatformImpl implements NecPlatform {


    @Override
    public Map<URI, Set<CommonModMetadata>> getModsAtLocationsInDisk() {
        Map<URI, Set<CommonModMetadata>> modMap = new HashMap<>();

        for (ModFileInfo modFile : ModList.get().getModFiles()) {
            URI modJar = modFile.getFile().getFilePath().toUri();
            for (IModInfo modInfo : modFile.getMods()) {
                modMap.computeIfAbsent(modJar, f -> new HashSet<>()).add(toCommon(modInfo));
            }
        }

        return modMap;
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
        ModFileInfo mod = ModList.get().getModFileById(modId);
        if (mod == null) return Collections.emptyList();
        return mod.getMods().stream().map(NecPlatformImpl::toCommon).collect(Collectors.toList());
    }

    private static CommonModMetadata toCommon(IModInfo imod) {
        if (!(imod instanceof ModInfo)) return CommonModMetadata.STUB;
        ModInfo mod = (ModInfo) imod;
        URL issueUrl = mod.getOwningFile().getIssueURL();
        Object authorsObj = mod.getConfigElement("authors").orElse(null);
        List<String> authors = authorsObj instanceof String ? Collections.singletonList((String) authorsObj) : (List<String>) authorsObj;
        return new CommonModMetadata(mod.getModId(), mod.getDisplayName(), issueUrl == null ? null : issueUrl.toExternalForm(), authors);
    }
}
