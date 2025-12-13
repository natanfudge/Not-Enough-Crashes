package fudge.notenoughcrashes.utils;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;


public class GlUtil {

    /**
     * This method goes over all the RenderSystem methods Minecraft calls, and resets them.
     * This is to prevent situations when Minecraft calls for example enableX, then crashes, and then it doesn't call disableX itself.
     * In that case, we call disableX ourselves so rendering will keep working properly.
     * Sometimes, Minecraft does disableX and then enableX. In that case we need to do enableX ourselves.
     */
    public static void resetState() {
        // Method calls are in the order they are declared in the Minecraft source.

        // Reset texture
        GlStateManager._bindTexture(0);

        // Reset depth
        GlStateManager._disableDepthTest();
//        RenderSystem.disableScissor();
        GlStateManager._depthFunc(513);
        GlStateManager._depthMask(true);

        // Reset blend mode
        GlStateManager._disableBlend();
//        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
//        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

//        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);

        GlStateManager._enableCull();

        GlStateManager._polygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        GlStateManager._polygonMode(GL11.GL_BACK, GL11.GL_FILL);

        // Reset polygon offset
        GlStateManager._polygonOffset(0.0F, 0.0F);
        GlStateManager._disablePolygonOffset();

        // Reset color logic
        GlStateManager._disableColorLogicOp();
//        RenderSystem.logicOp(GlStateManager.LogicOp.COPY);


        // Disable lightmap
        GlStateManager._activeTexture(GL13.GL_TEXTURE1);
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);

        // Reset texture parameters
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);

        GlStateManager._colorMask(true, true, true, true);
//        RenderSystem.clearDepth(1.0D);

        GL11.glLineWidth(1.0F);
//        RenderSystem.clearDepth(1.0D);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        GlStateManager._enableCull();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

    }
}
