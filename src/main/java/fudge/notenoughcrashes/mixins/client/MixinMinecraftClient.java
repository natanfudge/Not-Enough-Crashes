package fudge.notenoughcrashes.mixins.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fudge.notenoughcrashes.CrashUtils;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.PatchedClient;
import fudge.notenoughcrashes.StateManager;
import fudge.notenoughcrashes.gui.CrashScreen;
import fudge.notenoughcrashes.gui.InitErrorScreen;
import fudge.utils.GlUtil;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.SharedConstants;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.HotbarStorage;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.FirstPersonRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.resource.ClientResourcePackProfile;
import net.minecraft.client.resource.FoliageColormapResourceSupplier;
import net.minecraft.client.resource.Format4ResourcePack;
import net.minecraft.client.resource.GrassColormapResourceSupplier;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.PaintingManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.fabricmc.fabric.mixin.resource.loader.MixinFormat4ResourcePack;

@Mixin(MinecraftClient.class)
@SuppressWarnings("StaticVariableMayNotBeInitialized")
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor<Runnable> implements PatchedClient {

    @Shadow
    public static byte[] memoryReservedForCrash;
    @Shadow
    @Final
    public static Identifier DEFAULT_TEXT_RENDERER_ID;
    @Shadow
    @Final
    public static boolean IS_SYSTEM_MAC;
    // @formatter:off
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    public GameOptions options;
    @Shadow
    public InGameHud inGameHud;
    @Shadow
    public Screen currentScreen;
    @Shadow
    public TextureManager textureManager;
    @Shadow
    public TextRenderer textRenderer;
    //    @Shadow public abstract void method_1550(ClientWorld world, Gui loadingGui);
    @Shadow
    public Window window;
    @Shadow
    public Mouse mouse;
    @Shadow
    @Final
    public File runDirectory;
    @Shadow
    public Keyboard keyboard;
    @Shadow
    volatile boolean running;
    @Shadow
    private CrashReport crashReport;
    @Shadow
    private int attackCooldown;
    @Shadow
    private Framebuffer framebuffer;
    @Shadow
    private ReloadableResourceManager resourceManager;
    @Shadow
    private SoundManager soundManager;
    @Shadow
    private LanguageManager languageManager;
    @Shadow
    private FontManager fontManager;
    @Shadow
    @Final
    private ResourcePackManager<ClientResourcePackProfile> resourcePackManager;
    @Shadow
    @Final
    private Queue<Runnable> renderTaskQueue;

    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    @Final
    private RenderTickCounter renderTickCounter;
    @Shadow
    private WindowProvider windowProvider;

    @Shadow
    private Thread thread;

    @Shadow
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private HotbarStorage creativeHotbarStorage;

    @Shadow
    private PlayerSkinProvider skinProvider;

    @Shadow
    private LevelStorage levelStorage;

    @Shadow
    private SplashTextResourceSupplier splashTextLoader;

    @Shadow
    private MusicTracker musicTracker;

    @Shadow
    private BlockColors blockColorMap;
    @Shadow
    private ItemColors itemColorMap;
    @Shadow
    private BakedModelManager bakedModelManager;

    @Shadow
    private ItemRenderer itemRenderer;

    @Shadow
    private EntityRenderDispatcher entityRenderManager;
    @Shadow
    private FirstPersonRenderer firstPersonRenderer;

    @Shadow
    private BlockRenderManager blockRenderManager;

    @Shadow
    public ParticleManager particleManager;

    @Shadow
    private PaintingManager paintingManager;
    @Shadow
    @Final
    private SearchManager searchManager;

    @Shadow
    private StatusEffectSpriteManager statusEffectSpriteManager;

    @Shadow
    public DebugRenderer debugRenderer;

    private int clientCrashCount = 0;
    private int serverCrashCount = 0;
    @Shadow
    private static CompletableFuture<Unit> COMPLETED_UNIT_FUTURE;


    public MixinMinecraftClient(String string_1) {
        super(string_1);
    }

    @Shadow
    public void openScreen(Screen gui) {
    }
    // @formatter:on

    @Shadow
    public CrashReport addDetailsToCrashReport(CrashReport report) {
        return null;
    }

    @Shadow
    public void close() {
    }

    @Shadow
    private void initializeSearchableContainers() {
    }

    @Shadow
    public abstract ClientPlayNetworkHandler getNetworkHandler();


    @Shadow
    protected abstract void render(boolean boolean_1);

    @Shadow
    public abstract CompletableFuture<Void> reloadResources();

    @Shadow
    public abstract boolean forcesUnicodeFont();

    @Shadow
    public abstract void stop();

    @Shadow
    public abstract void disconnect(Screen screen);

    @Shadow
    private void startTimerHackThread() {
    }

    @Shadow
    public WorldRenderer worldRenderer;


    @Shadow
    public abstract void onWindowFocusChanged(boolean focused);

    @Shadow
    private void handleGlErrorByDisableVsync(int error, long description) {
    }

    @Shadow
    private void checkGameData() {
    }


    private boolean crashedDuringStartup = false;

    /**
     * @author runemoro
     * @reason Allows the player to choose to return to the title screen after a crash, or get
     * a pasteable link to the crash report on paste.dimdev.org.
     */
    @Overwrite
    public void run() {
        while (running) {
            if (crashReport == null) {
                try {
                    render(true);
                } catch (CrashException e) {
                    clientCrashCount++;
                    addDetailsToCrashReport(e.getReport());
                    addInfoToCrash(e.getReport());
                    resetGameState();
                    LOGGER.fatal("Reported exception thrown!", e);
                    displayCrashScreen(e.getReport());
                } catch (Throwable e) {
                    clientCrashCount++;
                    CrashReport report = new CrashReport("Unexpected error", e);

                    addDetailsToCrashReport(report);
                    addInfoToCrash(report);
                    resetGameState();
                    LOGGER.fatal("Unreported exception thrown!", e);
                    displayCrashScreen(report);
                }
            } else {
                serverCrashCount++;
                addInfoToCrash(crashReport);
                displayCrashScreen(crashReport);

                crashReport = null;
            }
        }
    }

    public void addInfoToCrash(CrashReport report) {
        report.getSystemDetailsSection().add("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getSystemDetailsSection().add("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    @Override
    public void displayInitErrorScreen(CrashReport report) {
        crashedDuringStartup = true;


        CrashUtils.outputReport(report);

        try {

//            GlUtil.resetStateWithoutContext();

            //TODO: skip a lot of the initialization to not give the impression the game loaded correctly
            completeInitializationForGUIPreScreen();
            running = true;
            openScreen(new InitErrorScreen(report));
            completeInitializationForGUIPostScreen();

//            runGUILoop(new InitErrorScreen(report));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private static final int DefaultWidth = 854;
    private static final int DefaultHeight = 480;

    private WindowSettings getDefaultWindowsSettings() {
        return new WindowSettings(DefaultWidth, DefaultHeight, OptionalInt.empty(), OptionalInt.empty(), false);
    }

    private MinecraftClient getThis() {
        return MinecraftClient.getInstance();
    }

    private void ignoreAGlfwError() {
        MemoryStack memoryStack = MemoryStack.stackPush();
        PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
        GLFW.glfwGetError(pointerBuffer);
    }

    private void completeInitializationForGUIPreScreen() {
        // At some point earlier something triggers a GL error so we need to ignore it so minecraft doesn't crash
        ignoreAGlfwError();

        this.thread = Thread.currentThread();
        this.options = new GameOptions(getThis(), this.runDirectory);
        this.creativeHotbarStorage = new HotbarStorage(this.runDirectory, getThis().getDataFixer());
        this.startTimerHackThread();
        LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
        WindowSettings windowSettings2 = getDefaultWindowsSettings();

        Util.nanoTimeSupplier = RenderSystem.initBackendSystem();
        this.windowProvider = new WindowProvider(getThis());
        this.window = this.windowProvider.createWindow(windowSettings2, this.options.fullscreenResolution, "Minecraft " + SharedConstants.getGameVersion().getName());
        getThis().onWindowFocusChanged(true);

        try {
            InputStream inputStream = getThis().getResourcePackDownloader().getPack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_16x16.png"));
            InputStream inputStream2 = getThis().getResourcePackDownloader().getPack().open(ResourceType.CLIENT_RESOURCES, new Identifier("icons/icon_32x32.png"));
            this.window.setIcon(inputStream, inputStream2);
        } catch (IOException var9) {
            LOGGER.error("Couldn't set icon", var9);
        }

        this.window.setFramerateLimit(this.options.maxFps);
        this.mouse = new Mouse(getThis());
        this.mouse.setup(this.window.getHandle());
        this.keyboard = new Keyboard(getThis());
        this.keyboard.setup(this.window.getHandle());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.framebuffer = new Framebuffer(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), true, IS_SYSTEM_MAC);
        this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES, this.thread);
        this.options.addResourcePackProfilesToManager(this.resourcePackManager);
        this.resourcePackManager.scanPacks();
        List<ResourcePack> list = (List) fabricInjectModResourcePacks(this.resourcePackManager.getEnabledProfiles().stream().map(ResourcePackProfile::createResourcePack), Collectors.toList());
        Iterator var11 = list.iterator();

        while (var11.hasNext()) {
            ResourcePack resourcePack = (ResourcePack) var11.next();
            this.resourceManager.addPack(resourcePack);
        }

        this.languageManager = new LanguageManager(this.options.language);
        this.resourceManager.registerListener(this.languageManager);
        this.languageManager.reloadResources(list);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerListener(this.textureManager);
//        this.skinProvider = new PlayerSkinProvider(this.textureManager, new File(file, "skins"), getThis().getSessionService());
        this.levelStorage = new LevelStorage(this.runDirectory.toPath().resolve("saves"), this.runDirectory.toPath().resolve("backups"),
                        getThis().getDataFixer());
        this.soundManager = new SoundManager(this.resourceManager, this.options);
        this.resourceManager.registerListener(this.soundManager);
        this.splashTextLoader = new SplashTextResourceSupplier(getThis().getSession());
        this.resourceManager.registerListener(this.splashTextLoader);
        this.musicTracker = new MusicTracker(getThis());
        this.fontManager = new FontManager(this.textureManager, this.forcesUnicodeFont());
        this.resourceManager.registerListener(this.fontManager.getResourceReloadListener());
        TextRenderer textRenderer = this.fontManager.getTextRenderer(DEFAULT_TEXT_RENDERER_ID);
        if (textRenderer == null) {
            throw new IllegalStateException("Default font is null");
        } else {
            this.textRenderer = textRenderer;
            this.textRenderer.setRightToLeft(this.languageManager.isRightToLeft());
            this.resourceManager.registerListener(new GrassColormapResourceSupplier());
            this.resourceManager.registerListener(new FoliageColormapResourceSupplier());
            this.window.setPhase("Startup");
            RenderSystem.setupDefaultState(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
            this.window.setPhase("Post startup");
            this.blockColorMap = BlockColors.create();
            this.itemColorMap = ItemColors.create(this.blockColorMap);
            this.bakedModelManager = new BakedModelManager(this.textureManager, this.blockColorMap, this.options.mipmapLevels);
            this.resourceManager.registerListener(this.bakedModelManager);
            this.itemRenderer = new ItemRenderer(this.textureManager, this.bakedModelManager, this.itemColorMap);
            this.entityRenderManager = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.resourceManager, this.textRenderer, this.options);
            this.firstPersonRenderer = new FirstPersonRenderer(getThis());
            this.resourceManager.registerListener(this.itemRenderer);
            this.bufferBuilders = new BufferBuilderStorage();
            this.gameRenderer = new GameRenderer(getThis(), this.resourceManager, this.bufferBuilders);
            this.resourceManager.registerListener(this.gameRenderer);
            this.blockRenderManager = new BlockRenderManager(this.bakedModelManager.getBlockModels(), this.blockColorMap);
            this.resourceManager.registerListener(this.blockRenderManager);
            this.worldRenderer = new WorldRenderer(getThis(), this.bufferBuilders);
            this.resourceManager.registerListener(this.worldRenderer);
            this.initializeSearchableContainers();
            this.resourceManager.registerListener(this.searchManager);
            this.particleManager = new ParticleManager(getThis().world, this.textureManager);
            this.resourceManager.registerListener(this.particleManager);
            this.paintingManager = new PaintingManager(this.textureManager);
            this.resourceManager.registerListener(this.paintingManager);
            this.statusEffectSpriteManager = new StatusEffectSpriteManager(this.textureManager);
            this.resourceManager.registerListener(this.statusEffectSpriteManager);
            this.inGameHud = new InGameHud(getThis());
            this.debugRenderer = new DebugRenderer(getThis());
            RenderSystem.setErrorCallback(this::handleGlErrorByDisableVsync);
            if (this.options.fullscreen && !this.window.isFullscreen()) {
                this.window.toggleFullscreen();
                this.options.fullscreen = this.window.isFullscreen();
            }

            this.window.setVsync(this.options.enableVsync);
            this.window.setRawMouseMotion(this.options.rawMouseInput);
            this.window.logOnGlError();
            getThis().onResolutionChanged();
        }
    }

    private void completeInitializationForGUIPostScreen() {
        SplashScreen.init(getThis());
        getThis().setOverlay(new SplashScreen(getThis(), this.resourceManager.beginInitialMonitoredReload(Util.getServerWorkerExecutor(), this, COMPLETED_UNIT_FUTURE), (optional) -> {
            Util.ifPresentOrElse(optional, (throwable) -> {
                if (this.resourcePackManager.getEnabledProfiles().size() > 1) {
                    LOGGER.info("Caught error loading resourcepacks, removing all assigned resourcepacks", throwable);
                    this.resourcePackManager.setEnabledProfiles(Collections.emptyList());
                    this.options.resourcePacks.clear();
                    this.options.incompatibleResourcePacks.clear();
                    this.options.write();
                    this.reloadResources();
                } else {
                    Util.method_24155(throwable);
                }

            }, () -> {
                if (SharedConstants.isDevelopment) {
                    this.checkGameData();
                }

            });
        }, false));


        RenderSystem.finishInitialization();

        RenderSystem.initGameThread(false);
        run();
    }

    private Object fabricInjectModResourcePacks(Stream<ResourcePack> stream, Collector collector) {
        List<ResourcePack> fabricResourcePacks = stream.collect(Collectors.toList());
        this.fabricModifyResourcePackList(fabricResourcePacks);
        return fabricResourcePacks.stream().collect(collector);
    }

    private void fabricModifyResourcePackList(List<ResourcePack> list) {
        List<ResourcePack> oldList = Lists.newArrayList(list);
        list.clear();
        boolean appended = false;

        for (int i = 0; i < oldList.size(); ++i) {
            ResourcePack pack = (ResourcePack) oldList.get(i);
            list.add(pack);
            boolean isDefaultResources = pack instanceof DefaultResourcePack;
            if (!isDefaultResources && pack instanceof Format4ResourcePack) {
                MixinFormat4ResourcePack fixer = (MixinFormat4ResourcePack) pack;
                isDefaultResources = fixer.getParent() instanceof DefaultResourcePack;
            }

            if (isDefaultResources) {
                ModResourcePackUtil.appendModResourcePacks(list, ResourceType.CLIENT_RESOURCES);
                appended = true;
            }
        }

        if (!appended) {
            StringBuilder builder = new StringBuilder("Fabric could not find resource pack injection location!");
            Iterator var9 = oldList.iterator();

            while (var9.hasNext()) {
                ResourcePack rp = (ResourcePack) var9.next();
                builder.append("\n - ").append(rp.getName()).append(" (").append(rp.getClass().getName()).append(")");
            }

            throw new RuntimeException(builder.toString());
        }
    }

//    private void runGUILoop(Screen screen) {
//        openScreen(screen);
//
//        while (running && currentScreen != null && !(currentScreen instanceof TitleScreen)) {
//            window.setPhase("Not Enough Crashes GUI Loop");
//            if (GLX._shouldClose(window)) {
//                stop();
//            }
//
//            attackCooldown = 10000;
//            currentScreen.tick();
//
//            mouse.updateMouse();
//
//            RenderSystem.pushMatrix();
//            RenderSystem.clear(16640, IS_SYSTEM_MAC);
//            framebuffer.beginWrite(true);
//            RenderSystem.enableTexture();
//
//            RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
//            RenderSystem.matrixMode(5889);
//            RenderSystem.loadIdentity();
//            RenderSystem.matrixMode(5888);
//            RenderSystem.loadIdentity();
//
//            RenderSystem.clear(256, IS_SYSTEM_MAC);
//
//            unknownMethodOfImportantGl();
//
//            currentScreen.render(
//                            (int) (mouse.getX() * window.getScaledWidth() / window.getWidth()),
//                            (int) (mouse.getY() * window.getScaledHeight() / window.getHeight()),
//                            0
//            );
//
//            framebuffer.endWrite();
//            RenderSystem.popMatrix();
//            RenderSystem.pushMatrix();
//            framebuffer.draw(window.getWidth(), window.getHeight());
//            RenderSystem.popMatrix();
//            RenderSystem.pushMatrix();
//            unknownMethodOfImportantGl();
//            RenderSystem.popMatrix();
//            window.setFullscreen();
//            Thread.yield();
//        }
//    }

    private void unknownMethodOfImportantGl() {
        RenderSystem.clear(256, IS_SYSTEM_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
    }

    public void displayCrashScreen(CrashReport report) {
        try {
            if (crashedDuringStartup) throw new IllegalStateException("Could not initialize startup crash screen");

            CrashUtils.outputReport(report);

            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            options.debugEnabled = false;
            inGameHud.getChatHud().clear(true);

            // Display the crash screen
//            runGUILoop(new CrashScreen(report));
            openScreen(new CrashScreen(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            printCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    /**
     * @author Runemoro
     * @reason Substitute
     */
    @Overwrite
    public static void printCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }

    public void resetGameState() {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalReservedMemorySize = -1;
            try { // In case another mod actually deletes the memoryReserve field
                if (memoryReservedForCrash != null) {
                    originalReservedMemorySize = memoryReservedForCrash.length;
                    memoryReservedForCrash = new byte[0];
                }
            } catch (Throwable ignored) {
            }

            // Reset registered resettables
            StateManager.resetStates();

            // Close the world
            if (getNetworkHandler() != null) {
                // Fix: Close the connection to avoid receiving packets from old server
                // when playing in another world (MC-128953)
                getNetworkHandler().getConnection().disconnect(new LiteralText(String.format("[%s] Client crashed", NotEnoughCrashes.NAME)));
            }

            disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            renderTaskQueue.clear(); // Fix: method_1550(null, ...) only clears when integrated server is running

            // Reset graphics
            GlUtil.resetState();

            // Re-create memory reserve so that future crashes work well too
            if (originalReservedMemorySize != -1) {
                try {
                    memoryReservedForCrash = new byte[originalReservedMemorySize];
                } catch (Throwable ignored) {
                }
            }

            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
            try {
                StateManager.resetStates();
                GlUtil.resetState();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * @author runemoro
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * Bugs Fixed:
     * - https://bugs.mojang.com/browse/MC-128953
     * - Memory reserve not recreated after out-of memory
     */
    @Overwrite
    public void cleanUpAfterCrash() {
        resetGameState();
    }
}
