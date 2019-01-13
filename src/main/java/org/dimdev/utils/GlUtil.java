package org.dimdev.utils;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.GuiLighting;
import org.lwjgl.opengl.*;

public class GlUtil {
    public static void resetState() {
        // Clear matrix stack
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_COLOR);
        GlStateManager.loadIdentity();

        // Clear attribute stacks TODO: Broken, a stack underflow breaks LWJGL
        // try {
        //     do GL11.glPopAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}
        //
        // try {
        //     do GL11.glPopClientAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}

        // Reset texture
        GlStateManager.bindTexture(0);
        GlStateManager.disableTexture();

        // Reset GL lighting
        GlStateManager.disableLighting();
        GlStateManager.lightModel(GL11.GL_LIGHT_MODEL_AMBIENT, GuiLighting.singletonBuffer(0.2F, 0.2F, 0.2F, 1.0F));
        for (int i = 0; i < 8; ++i) {
            GlStateManager.disableLight(i);
            GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, GuiLighting.singletonBuffer(0.0F, 0.0F, 1.0F, 0.0F));

            if (i == 0) {
                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, GuiLighting.singletonBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, GuiLighting.singletonBuffer(1.0F, 1.0F, 1.0F, 1.0F));
            } else {
                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                GlStateManager.light(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            }
        }
        GlStateManager.disableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);

        // Reset depth
        GlStateManager.disableDepthTest();
        GlStateManager.depthFunc(513);
        GlStateManager.depthMask(true);

        // Reset blend mode
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO);
        GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO, GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO);
        GlStateManager.blendEquation(GL14.GL_FUNC_ADD);

        // Reset fog
        GlStateManager.disableFog();
        GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
        GlStateManager.fogDensity(1.0F);
        GlStateManager.fogStart(0.0F);
        GlStateManager.fogEnd(1.0F);
        GlStateManager.fog(GL11.GL_FOG_COLOR, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        if (GL.getCapabilities().GL_NV_fog_distance) {
            GlStateManager.fog(GL11.GL_FOG_MODE, 34140);
        }

        // Reset polygon offset
        GlStateManager.polygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();

        // Reset color logic
        GlStateManager.disableColorLogicOp();
        GlStateManager.logicOp(5379);

        // Reset texgen
        GlStateManager.disableTexGen(GlStateManager.TexCoord.S);
        GlStateManager.disableTexGen(GlStateManager.TexCoord.T);
        GlStateManager.disableTexGen(GlStateManager.TexCoord.R);
        GlStateManager.disableTexGen(GlStateManager.TexCoord.Q);
        GlStateManager.texGenMode(GlStateManager.TexCoord.S, 9216);
        GlStateManager.texGenMode(GlStateManager.TexCoord.T, 9216);
        GlStateManager.texGenMode(GlStateManager.TexCoord.R, 9216);
        GlStateManager.texGenMode(GlStateManager.TexCoord.Q, 9216);
        GlStateManager.texGenParam(GlStateManager.TexCoord.S, 9474, GuiLighting.singletonBuffer(1.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.T, 9474, GuiLighting.singletonBuffer(0.0F, 1.0F, 0.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.R, 9474, GuiLighting.singletonBuffer(0.0F, 0.0F, 1.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.Q, 9474, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.S, 9217, GuiLighting.singletonBuffer(1.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.T, 9217, GuiLighting.singletonBuffer(0.0F, 1.0F, 0.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.R, 9217, GuiLighting.singletonBuffer(0.0F, 0.0F, 1.0F, 0.0F));
        GlStateManager.texGenParam(GlStateManager.TexCoord.Q, 9217, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        // Disable lightmap
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();

        GlStateManager.activeTexture(GLX.GL_TEXTURE0);

        // Reset texture parameters
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
        GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, GuiLighting.singletonBuffer(0.0F, 0.0F, 0.0F, 0.0F));
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
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1.0F);
        GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1.0F);

        GlStateManager.disableNormalize();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableRescaleNormal();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.lineWidth(1.0F);
        GlStateManager.normal3f(0.0F, 0.0F, 1.0F);
        GlStateManager.polygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        GlStateManager.polygonMode(GL11.GL_BACK, GL11.GL_FILL);

        GlStateManager.enableTexture();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(GlStateManager.FaceSides.BACK);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
    }
}
