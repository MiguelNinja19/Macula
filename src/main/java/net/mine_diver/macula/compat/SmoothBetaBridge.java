package net.mine_diver.macula.compat;

import net.mine_diver.smoothbeta.client.render.Shader;
import net.mine_diver.smoothbeta.client.render.Shaders;
import net.mine_diver.smoothbeta.client.render.gl.GlProgramManager;
import net.mine_diver.smoothbeta.client.render.gl.GlStateManager;
import net.mine_diver.smoothbeta.client.render.gl.GlUniform;
import net.mine_diver.smoothbeta.client.render.gl.VertexBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Bridge between Macula and SmoothBeta rendering systems.
 * Allows Macula to cooperate with SmoothBeta's VBO/VAO terrain rendering
 * while still applying Macula's post-processing pipeline.
 */
public final class SmoothBetaBridge {

    private SmoothBetaBridge() {}

    /**
     * Called before SmoothBeta renders terrain chunks.
     * Sets up the FBO and draw buffer for SmoothBeta to render into.
     */
    public static void beforeSmoothBetaTerrainRender() {
        if (!SmoothBetaCompat.LOADED) return;

        // Bind Macula's default FBO so SmoothBeta renders into our color attachments
        org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer(
                org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER,
                net.mine_diver.macula.rendering.FramebufferManager.defaultFramebufferId
        );

        // SmoothBeta's shader writes to fragColor (single output)
        // Set draw buffer to COLOR_ATTACHMENT0 only
        GL20.glDrawBuffers(org.lwjgl.opengl.ARBFramebufferObject.GL_COLOR_ATTACHMENT0);

        // Ensure VAO is unbound before SmoothBeta binds its own
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Called after SmoothBeta renders terrain chunks.
     * Restores Macula's draw buffers and cleans up VAO/VBO state.
     */
    public static void afterSmoothBetaTerrainRender() {
        if (!SmoothBetaCompat.LOADED) return;

        // Unbind SmoothBeta's VAO and VBOs
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Disable vertex attributes that SmoothBeta enabled
        GL20.glDisableVertexAttribArray(0); // position
        GL20.glDisableVertexAttribArray(1); // texture
        GL20.glDisableVertexAttribArray(2); // color
        GL20.glDisableVertexAttribArray(3); // normal

        // Restore Macula's draw buffers (4 color attachments)
        GL20.glDrawBuffers(net.mine_diver.macula.rendering.FramebufferManager.defaultDrawBuffers);

        // Unbind SmoothBeta's shader program
        GlProgramManager.useProgram(0);

        // Reset GL state that SmoothBeta may have modified
        GlStateManager._disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    /**
     * Called before SmoothBeta renders entities/weather/hand.
     * These use vanilla rendering path, so we need Macula's FBO bound.
     */
    public static void beforeSmoothBetaEntityRender() {
        if (!SmoothBetaCompat.LOADED) return;

        org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer(
                org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER,
                net.mine_diver.macula.rendering.FramebufferManager.defaultFramebufferId
        );
        GL20.glDrawBuffers(org.lwjgl.opengl.ARBFramebufferObject.GL_COLOR_ATTACHMENT0);
    }

    /**
     * Called after SmoothBeta entity/weather/hand render.
     * Restore Macula's multi-draw-buffer setup.
     */
    public static void afterSmoothBetaEntityRender() {
        if (!SmoothBetaCompat.LOADED) return;

        GL20.glDrawBuffers(net.mine_diver.macula.rendering.FramebufferManager.defaultDrawBuffers);
    }

    /**
     * Get SmoothBeta's terrain shader for potential uniform synchronization.
     */
    public static Shader getTerrainShader() {
        if (!SmoothBetaCompat.LOADED) return null;
        return Shaders.getTerrainShader();
    }

    /**
     * Synchronize shared uniforms between Macula and SmoothBeta.
     * Both need ModelViewMat, ProjMat, fog, etc.
     */
    public static void syncUniforms(float partialTick, long time) {
        if (!SmoothBetaCompat.LOADED) return;

        Shader sbShader = Shaders.getTerrainShader();
        if (sbShader == null) return;

        // SmoothBeta updates its own uniforms in WorldRendererMixin.smoothbeta_beforeRenderRegion
        // but we can ensure they're in sync if needed
    }

    /**
     * Ensure SmoothBeta's terrain shader is reloaded when Macula reloads shaders.
     */
    public static void onShaderReload() {
        if (!SmoothBetaCompat.LOADED) return;
        // Shaders class handles reload via StationAPI resource reload listener
        // Just ensure our FBO is resized properly
        net.mine_diver.macula.core.ShaderCore.init();
    }
}