package fudge.notenoughcrashes.mixins.client;


import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static fudge.notenoughcrashes.mixinhandlers.EntryPointCatcher.replacedImage;

///**
// * We replace all usages of the LOGO field with our own image.
// */
//@Mixin(SplashScreen.class)
//public abstract class SplashScreenMixin {
//
//    @Redirect(method = "init(Lnet/minecraft/client/MinecraftClient;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashScreen;LOGO:Lnet/minecraft/util/Identifier;"))
//    private static Identifier replaceSplashImage1(MinecraftClient client) {
//        return replacedImage();
//    }
//
//    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashScreen;LOGO:Lnet/minecraft/util/Identifier;"))
//    private Identifier replaceSplashImage2(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        return replacedImage();
//    }
//
//    @Mixin(SplashScreen.LogoTexture.class)
//    public static abstract class LogoTextureMixin {
//        @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashScreen;LOGO:Lnet/minecraft/util/Identifier;"))
//        private static Identifier replaceSplashImage3() {
//            return replacedImage();
//        }
//
//        @Redirect(method = "loadTextureData", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashScreen;LOGO:Lnet/minecraft/util/Identifier;"))
//        private  Identifier replaceSplashImage4() {
//            return replacedImage();
//        }
//    }
//
//}
