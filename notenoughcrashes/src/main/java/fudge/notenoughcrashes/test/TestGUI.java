//package fudge.notenoughcrashes.test;
//
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.text.LiteralText;
//import net.minecraft.text.Text;
//import net.minecraft.util.Identifier;
//
//import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
//
//public class TestGUI extends Screen {
//	private static final Identifier ID = new Identifier("notenoughcrashes", "crash");
//
//	@Override
//	public void render(int mouseX, int mouseY, float delta) {
//		throw new NullPointerException();
//	}
//
//	protected TestGUI() {
//		super(new LiteralText("crasher"));
//	}
//
//	public static void initGui() {
////		ScreenProviderRegistry.INSTANCE.registerFactory(ID, (container -> new TestGUI()));
//	}
//}
