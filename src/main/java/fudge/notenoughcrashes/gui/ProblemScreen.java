package fudge.notenoughcrashes;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fudge.notenoughcrashes.gui.util.Widget;
import fudge.utils.HasteUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Environment(EnvType.CLIENT)
public abstract class ProblemScreen extends Screen {

    private final List<Widget> widgets = new ArrayList<>();

    protected void addWidget(Widget widget) {
        widgets.add(widget);
    }

    private static final Logger LOGGER = LogManager.getLogger();



    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString = null;
    protected int xLeft = Integer.MAX_VALUE;
    protected int xRight = Integer.MIN_VALUE;
    protected int yTop = Integer.MAX_VALUE;
    protected int yBottom = Integer.MIN_VALUE;

    protected ProblemScreen(CrashReport report) {
        super(new LiteralText(""));
        this.report = report;
    }

    private 

    @Override
    public void init() {
        addButton(new ButtonWidget(width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, I18n.translate("notenoughcrashes.gui.getLink"),
                        buttonWidget -> {
                            try {
                                if (hasteLink == null) {
                                    hasteLink = HasteUpload.uploadToHaste(ModConfig.instance().hasteURL, "mccrash", report.asString());
                                }
                                Field uriField;
                                //noinspection JavaReflectionMemberAccess
                                uriField = Screen.class.getDeclaredField("clickedLink");
                                uriField.setAccessible(true);
                                uriField.set(ProblemScreen.this, new URI(hasteLink));
                                minecraft.openScreen(new ConfirmChatLinkScreen(b -> {
                                    if (b) {
                                        Util.getOperatingSystem().open(hasteLink);
                                    }

                                    this.minecraft.openScreen(this);
                                }, hasteLink, true));
                            } catch (Throwable e) {
                                LOGGER.error("Exception when crash menu button clicked:", e);
                                buttonWidget.setMessage(I18n.translate("notenoughcrashes.gui.failed"));
                                buttonWidget.active = false;
                            }
                        }));
    }

    @Override
    public boolean mouseClicked(double x, double y, int int_1) {
        for (Widget widget : widgets) widget.draw();
        if (x >= xLeft && x <= xRight && y >= yTop && y <= yBottom) {
            File file = report.getFile();
            if (file != null) {
                Util.getOperatingSystem().open(file);
            }
        }
        return super.mouseClicked(x, y, int_1);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }


    private static final String MINECRAFT_ID = "minecraft";
    private static final String LOADER_ID = "fabricloader";


    protected Text getModListString() {
        if (modListString == null) {
            //TODO: seperate minecraft and fabric loader because they always exist
            // If it's only fabric-loader and/or minecraft - show them as the suspected
            // If there's something else - show it as the suspected mods and note it can be fabric-loader or minecraft.
            //TODO also need to take care to not NPE here
            Set<ModMetadata> suspectedMods = ((PatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                modListString = I18n.translate("notenoughcrashes.crashscreen.identificationErrored");
            } else {
                if (suspectedMods.isEmpty()) return I18n.translate("notenoughcrashes.crashscreen.unknownCause");

                // Minecraft exists and basically any stack trace, it's better not to include it in the list of mods.
                suspectedMods.removeIf(mod -> mod.getId().equals(MINECRAFT_ID));

                List<String> modNames = suspectedMods.stream().map(ModMetadata::getName).collect(Collectors.toList());

                if (modNames.isEmpty()) {
                    modListString = I18n.translate("notenoughcrashes.crashscreen.noModsErrored");
                } else {
                    modListString = StringUtils.join(modNames, ", ");
                }
            }


        }
        return modListString;
    }

    protected void drawFileNameString(int y) {
        String fileNameString =
                        report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.translate("notenoughcrashes.crashscreen.reportSaveFailed");
        int stLen = font.getStringWidth(fileNameString);
        xLeft = width / 2 - stLen / 2;
        xRight = width / 2 + stLen / 2;
        drawString(font, fileNameString, xLeft, y += 11, 0x00FF00);
        yTop = y;
        yBottom = y + 10;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        for (Widget widget : widgets) widget.draw();
        super.render(mouseX, mouseY, delta);
    }
}
