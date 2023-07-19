package fudge.notenoughcrashes.fabric.platform;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.ModsByLocation;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FabricPlatform implements NecPlatform {
    private boolean irisExists = false;

    public void setIrisExists() {
        irisExists = true;
    }

    @Override
    public boolean isForge() {
        return false;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public ModsByLocation getModsAtLocationsInDisk() {
        Map<Path, Set<CommonModMetadata>> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            Path path = mod.getRootPath();
            modMap.computeIfAbsent(path, f -> new HashSet<>()).add(toCommon(mod));
        }

        return new ModsByLocation(modMap);
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    @Nullable
    public InputStream getResource(Path relativePath) {
        // Don't resolve the root path directly with a normal Path, they are incompatible because the root path is often in a Zip FS
        Path path = NotEnoughCrashes.getMetadata().rootPath().resolve(relativePath.toString());
        if (!Files.exists(path)) return null;
        else {
            try {
                return Files.newInputStream(path);
            } catch (IOException e) {
                return null;
            }
        }
    }

    @Override
    public List<CommonModMetadata> getModMetadatas(String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        return mod.map(modContainer -> Collections.singletonList(toCommon(modContainer))).orElse(new ArrayList<>());
    }

    @Override
    public List<CommonModMetadata> getAllMods() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(FabricPlatform::toCommon)
                .collect(Collectors.toList());
    }

    @Override
    public boolean modContainsFile(CommonModMetadata mod, String path) {
        return FabricLoader.getInstance().getModContainer(mod.id()).flatMap(modContainer -> {
            try {
                return modContainer.findPath(path);
            } catch (InvalidPathException e) {
                // Sometimes weird mixin file fuck up this method call and throw a InvalidPathException
                return Optional.empty();
            }
        }).isPresent();
    }

    @Override
    public boolean irisExists() {
        return irisExists;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    // Earlier elements will be used first, may need to add more elements if people start using weird shit
    private static final List<String> possibleIssuesFieldsByPriority = Arrays.asList(
            "issues", "sources", "homepage"
    );

    /**
     * Loader doesn't provide us with a proper api to get a singular link to an issues page, so we need to guess.
     */
    private static String getIssuesPage(ContactInformation contactInformation) {
        for (String possibleField : possibleIssuesFieldsByPriority) {
            Optional<String> value = contactInformation.get(possibleField);
            if (value.isPresent()) return value.get();
        }
        return null;
    }

    private static CommonModMetadata toCommon(ModContainer modContainer) {
        ModMetadata mod = modContainer.getMetadata();
        return new CommonModMetadata(mod.getId(), mod.getName(), getIssuesPage(mod.getContact()),
                mod.getAuthors().stream().map(Person::getName).collect(Collectors.toList()), modContainer.getRootPath()
        );
    }
}
