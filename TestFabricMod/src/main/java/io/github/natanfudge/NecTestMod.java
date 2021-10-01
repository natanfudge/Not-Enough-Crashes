package io.github.natanfudge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class NecTestMod implements ModInitializer {
    public static String getTestMode() {
        String value = System.getProperty("nec_test");
        if (value == null) {
            return "none";
        } else return value;
    }

    private static KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.nec_test.crash", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_LEFT_BRACKET, // The keycode of the key
            "category.nec_test" // The translation key of the keybinding's category.
    ));
    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed()) {
                throw new NecTestCrash("Test Game Loop Crash");
            }
        });
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

//		TestBlock.init();
//		System.out.println("Hello Fabric world!");
    }
//
//	public static class TestBlock extends Block {
//		public static final Block EXAMPLE_BLOCK = new TestBlock();
//
//		public TestBlock() {
//			super(Settings.of(Material.METAL));
//		}
//
//		@Override
//		public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
//			try {
//				throw new RuntimeException("something NPED!", new NullPointerException("THIS NPED!"));
//			} catch (Throwable e) {
//				e.addSuppressed(new NullPointerException("SUPRESSED!"));
//				throw e;
//			}
//		}
//
//		public static void init() {
//			Registry.register(Registry.BLOCK, new Identifier("crashy_block", "crash_block"), EXAMPLE_BLOCK);
//			Registry.register(Registry.ITEM, new Identifier("crashy_block", "crash_block"),
//					new BlockItem(EXAMPLE_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
//		}
//	}
}

