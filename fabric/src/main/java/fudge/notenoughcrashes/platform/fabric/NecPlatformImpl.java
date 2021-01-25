package fudge.notenoughcrashes.platform.fabric;

import fudge.notenoughcrashes.mixins.client.SplashScreenMixin;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.platform.NecPlatform;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class NecPlatformImpl implements NecPlatform {
    @Override
    public void setSplashScreenLogo(Identifier newLogo) {
        SplashScreenMixin.setLogo(newLogo);
    }

    @Override
    public Map<URI, Set<CommonModMetadata>> getModsAtLocationsInDisk() {
        Map<URI, Set<CommonModMetadata>> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (!(mod instanceof net.fabricmc.loader.ModContainer)) {
                continue;
            }
            try {
                URI modJar = ModIdentifier.jarFromUrl(((net.fabricmc.loader.ModContainer) mod).getOriginUrl());
                modMap.computeIfAbsent(modJar, f -> new HashSet<>()).add(toCommon(mod.getMetadata()));
            } catch (URISyntaxException | IOException ignored) {
                // cannot find jar, so bruh
            }
        }

        return modMap;
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

    private static CommonModMetadata toCommon(ModMetadata mod) {
        return new CommonModMetadata(mod.getId(), mod.getName(), getIssuesPage(mod.getContact()),
                mod.getAuthors().stream().map(Person::getName).collect(Collectors.toList()));
    }
}
