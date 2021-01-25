package fudge.notenoughcrashes.test;//package fudge.notenoughcrashes.test;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.lwjgl.glfw.GLFW;
//
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.util.InputUtil;
//import net.minecraft.util.Identifier;
//
//import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
//import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
//import net.fabricmc.fabric.api.event.client.ClientTickCallback;
//
//public class TestKeyBinding {
//	private static FabricKeyBinding keyBinding = FabricKeyBinding.Builder.create(
//					new Identifier("notenoughcrashes", "crash"),
//					InputUtil.Type.KEYSYM,
//					GLFW.GLFW_KEY_K,
//					"NEC"
//	).build();
//
//	public static void init() {
//		KeyBindingRegistry.INSTANCE.register(keyBinding);
//		AtomicBoolean pressed = new AtomicBoolean(false);
//		ClientTickCallback.EVENT.register(e ->
//		{
//			if (keyBinding.isPressed()) {
//				if (!pressed.get()) {
//					MinecraftClient.getInstance().openScreen(new TestGUI());
//					pressed.set(true);
//				}
//			}
//		});
//	}
//}
