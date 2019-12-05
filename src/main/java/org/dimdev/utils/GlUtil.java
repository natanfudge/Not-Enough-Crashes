package org.dimdev.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.DiffuseLighting;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;

public class GlUtil {

    // On init there is no GL state so we need to avoid making calls that rely on it
    public static void resetStateWithoutContext(){
        RenderSystem.bindTexture(0);
        RenderSystem.disableTexture();

        // Reset GL lighting
        RenderSystem.disableLighting();
        RenderSystem.disableColorMaterial();
        RenderSystem.colorMaterial(1032, 5634);

        // Reset depth
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(513);
        RenderSystem.depthMask(true);

        // Reset blend mode
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        // Reset fog
        RenderSystem.disableFog();
        RenderSystem.fogDensity(1.0F);
        RenderSystem.fogStart(0.0F);
        RenderSystem.fogEnd(1.0F);

        // Reset polygon offset
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();

        // Reset color logic
        RenderSystem.disableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.COPY);

        // Reset texgen
        RenderSystem.clearTexGen();


        RenderSystem.shadeModel(7425);
        RenderSystem.disableRescaleNormal();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.clearDepth(1.0D);
//
        RenderSystem.shadeModel(7425);
        RenderSystem.clearDepth(1.0D);

        //TODO: complete guess
        RenderSystem.enableCull();
//        GlStateManager.cullFace(GlStateManager.FaceSides.BACK);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
    }

    public static void resetState() {
        //TODO: start adding stuff till it stops working

        // Clear matrix stack
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(GL11.GL_COLOR);
        RenderSystem.loadIdentity();
//
//        // Clear attribute stacks TODO: Broken, a stack underflow breaks LWJGL
//        // try {
//        //     do GL11.glPopAttrib(); while (RenderSystem.glGetError() == 0);
//        // } catch (Throwable ignored) {}
//        //
//        // try {
//        //     do GL11.glPopClientAttrib(); while (RenderSystem.glGetError() == 0);
//        // } catch (Throwable ignored) {}
//
//        // Reset texture
        RenderSystem.bindTexture(0);
        RenderSystem.disableTexture();

        // Reset GL lighting
        RenderSystem.disableLighting();
//        //TODO: restore this
////        GlStateManager.lightModel(GL11.GL_LIGHT_MODEL_AMBIENT,  GlStateManager.singletonBuffer(0.2F, 0.2F, 0.2F, 1.0F));
////        for (int i = 0; i < 8; ++i) {
////            GlStateManager.disableLight(i);
////            GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, DiffuseLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
////            GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, DiffuseLighting.singletonBuffer(0.0F, 0.0F, 1.0F, 0.0F));
////
////            if (i == 0) {
////                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, DiffuseLighting.singletonBuffer(1.0F, 1.0F, 1.0F, 1.0F));
////                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, DiffuseLighting.singletonBuffer(1.0F, 1.0F, 1.0F, 1.0F));
////            } else {
////                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, DiffuseLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
////                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, DiffuseLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
////            }
////        }
        RenderSystem.disableColorMaterial();
        RenderSystem.colorMaterial(1032, 5634);

        // Reset depth
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(513);
        RenderSystem.depthMask(true);

        // Reset blend mode
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);

        // Reset fog
        RenderSystem.disableFog();
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
        RenderSystem.fogDensity(1.0F);
        RenderSystem.fogStart(0.0F);
        RenderSystem.fogEnd(1.0F);
        RenderSystem.fog(GL11.GL_FOG_COLOR, 0.0F, 0.0F, 0.0F, 0.0F);
        if (GL.getCapabilities().GL_NV_fog_distance) {
            RenderSystem.fogMode(34140);
        }

        // Reset polygon offset
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();

        // Reset color logic
        RenderSystem.disableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.COPY);

        // Reset texgen
        RenderSystem.clearTexGen();
////
        // Disable lightmap
        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
        RenderSystem.disableTexture();

        RenderSystem.activeTexture(GL13.GL_TEXTURE0);

        // Reset texture parameters
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);

        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        //TODO
//        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, DiffuseLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_RGB, GL11.GL_TEXTURE);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_RGB, GL13.GL_PREVIOUS);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_RGB, GL13.GL_CONSTANT);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_ALPHA, GL13.GL_PREVIOUS);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_ALPHA, GL13.GL_CONSTANT);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1);

        //TODO
//        RenderSystem.disableNormalize();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableRescaleNormal();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.clearDepth(1.0D);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.normal3f(0.0F, 0.0F, 1.0F);
        RenderSystem.polygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        RenderSystem.polygonMode(GL11.GL_BACK, GL11.GL_FILL);
//
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7425);
        RenderSystem.clearDepth(1.0D);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.1F);

        //TODO: complete guess
        RenderSystem.enableCull();
//        GlStateManager.cullFace(GlStateManager.FaceSides.BACK);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
    }
}
