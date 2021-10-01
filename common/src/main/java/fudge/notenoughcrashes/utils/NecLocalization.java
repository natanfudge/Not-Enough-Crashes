package fudge.notenoughcrashes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * We use custom localization to not rely on fabric-resource-loader
 * Only use this for NEC translation string, not for Minecraft ones
 */
public class NecLocalization {
    private static final String DEFAULT_LANGUAGE_CODE = "en_us";

    @NotNull
    public static String localize(String translationKey) {
        String currentLanguageCode = getCurrentLanguageCode();
        String translationForChosenLanguage = localize(translationKey, currentLanguageCode);
        if (translationForChosenLanguage != null) return translationForChosenLanguage;
        else {
            String englishTranslation = localize(translationKey, DEFAULT_LANGUAGE_CODE);
            return englishTranslation == null ? translationKey : englishTranslation;
        }
    }

    @Nullable
    private static String localize(String translationKey, String languageCode) {
        LanguageTranslations translations = storedLanguages.computeIfAbsent(
                languageCode, (ignored) -> loadLanguage(languageCode)
        );
        return translations.get(translationKey);
    }

    public static Text translatedText(String translationKey) {
        return new LiteralText(localize(translationKey));
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class LanguageTranslations {
        private final Map<String, String> translations;

        private LanguageTranslations(Map<String, String> translations) {
            this.translations = translations;
        }

        @Nullable String get(String translationKey) {
            return translations.get(translationKey);
        }
    }

    private static final Map<String, LanguageTranslations> storedLanguages = new HashMap<>();

    private static String getCurrentLanguageCode() {
        return MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode();
    }

    private static LanguageTranslations loadLanguage(String code) {
        Map<String, String> translations;
        try {
            Path localizationsPath = getLocalizationPath(code);
            if (localizationsPath == null) {
                translations = new HashMap<>();
                NotEnoughCrashes.logDebug("No localization for language code: " + code);
            } else {
                String content = Files.readString(localizationsPath);
                translations = parseTranslations(content);
            }
        } catch (IOException e) {
            NotEnoughCrashes.getLogger().error("Could not load translations: ", e);
            translations = new HashMap<>();
        }
        return new LanguageTranslations(translations);
    }

    @Nullable
    private static Path getLocalizationPath(String code) {
        Path relativePath = Paths.get("assets", NotEnoughCrashes.MOD_ID, "lang", code + ".json");
        return NecPlatform.instance().getResource(relativePath);
    }

    private static final Gson gson = new Gson();

    private static Map<String, String> parseTranslations(String raw) {
        JsonObject jsonObject = gson.fromJson(raw, JsonObject.class);
        Map<String, String> translations = new HashMap<>();
        for (var child : jsonObject.entrySet()) {
            translations.put(child.getKey(), child.getValue().getAsString());
        }
        return translations;
    }

}
