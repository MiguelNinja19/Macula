package net.mine_diver.macula.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.macula.rendering.FramebufferManager;
import net.mine_diver.smoothbeta.client.render.Shader;
import net.mine_diver.smoothbeta.client.render.Shaders;
import net.mine_diver.smoothbeta.client.render.gl.GlProgramManager;
import net.mine_diver.smoothbeta.client.render.gl.GlStateManager;
import net.mine_diver.smoothbeta.client.render.gl.VertexBuffer;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class SmoothBetaCompat {
    public static final boolean LOADED = FabricLoader.getInstance().isModLoaded("smoothbeta");

    /**
     * Called before SmoothBeta renders terrain chunks.
     * Sets up the FBO and draw buffer for SmoothBeta to render into Macula's color attachment 0.
     */
    public static void beforeTerrainRender() {
        if (!LOADED) return;

        ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_FRAMEBUFFER, FramebufferManager.defaultFramebufferId);
        GL20.glDrawBuffers(ARBFramebufferObject.GL_COLOR_ATTACHMENT0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Called after SmoothBeta renders terrain chunks.
     * Restores Macula's draw buffers and cleans up VAO/VBO state.
     */
    public static void afterTerrainRender() {
        if (!LOADED) return;

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL20.glDrawBuffers(FramebufferManager.defaultDrawBuffers);
        GlProgramManager.useProgram(0);
        GlStateManager._disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    /**
     * Get SmoothBeta's terrain shader for uniform synchronization.
     */
    public static Shader getTerrainShader() {
        if (!LOADED) return null;
        return Shaders.getTerrainShader();
    }
}
