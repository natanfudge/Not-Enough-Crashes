package fudge.notenoughcrashes.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BackgroundRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;


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
        RenderSystem.bindTexture(0);

        // Reset depth
        RenderSystem.disableDepthTest();
        RenderSystem.disableScissor();
        RenderSystem.depthFunc(513);
        RenderSystem.depthMask(true);

        // Reset blend mode
        RenderSystem.disableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);

        RenderSystem.enableCull();

        RenderSystem.polygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        RenderSystem.polygonMode(GL11.GL_BACK, GL11.GL_FILL);

        // Reset polygon offset
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();

        // Reset color logic
        RenderSystem.disableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.COPY);


        // Disable lightmap
        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
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

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.clearDepth(1.0D);

        BackgroundRenderer.clearFog();


        RenderSystem.lineWidth(1.0F);
        RenderSystem.clearDepth(1.0D);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.enableCull();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

    }
}
