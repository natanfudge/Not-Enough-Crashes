package fudge.notenoughcrashes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    private static final boolean useCustomLocalization = !NecPlatform.instance().isForge()
            && !NecPlatform.instance().isModLoaded("fabric-resource-loader-v0");

    public static String localize(String translationKey) {
        if (useCustomLocalization) return localizeCustom(translationKey);
        else return I18n.translate(translationKey);
    }

    @NotNull
    private static String localizeCustom(String translationKey) {
        String currentLanguageCode = getCurrentLanguageCode();
        String translationForChosenLanguage = localizeCustom(translationKey, currentLanguageCode);
        if (translationForChosenLanguage != null) return translationForChosenLanguage;
        else {
            String englishTranslation = localizeCustom(translationKey, DEFAULT_LANGUAGE_CODE);
            return englishTranslation == null ? translationKey : englishTranslation;
        }
    }

    @Nullable
    private static String localizeCustom(String translationKey, String languageCode) {
        LanguageTranslations translations = storedLanguages.computeIfAbsent(
                languageCode, (ignored) -> loadLanguage(languageCode)
        );
        return translations.get(translationKey);
    }

    public static Text translatedText(String translationKey) {
        if (useCustomLocalization) return Text.of(localize(translationKey));
        else return Text.translatable(translationKey);
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
        return MinecraftClient.getInstance().getLanguageManager().getLanguage();
    }

    private static LanguageTranslations loadLanguage(String code) {
        Map<String, String> translations;
        try (InputStream localizations = getLocalizations(code)) {
            if (localizations == null) {
                translations = new HashMap<>();
                NotEnoughCrashes.logDebug("No localization for language code: " + code);
            } else {
                String content = IOUtils.toString(localizations, StandardCharsets.UTF_8); /*Java11.readString();*/
                translations = parseTranslations(content);
            }
        } catch (IOException e) {
            NotEnoughCrashes.getLogger().error("Could not load translations: ", e);
            translations = new HashMap<>();
        }
        return new LanguageTranslations(translations);
    }

    @Nullable
    private static InputStream getLocalizations(String code) throws IOException {
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
