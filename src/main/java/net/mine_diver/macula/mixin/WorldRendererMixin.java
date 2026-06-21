package net.mine_diver.macula.mixin;

import net.mine_diver.macula.compat.SmoothBetaCompat;
import net.mine_diver.macula.rendering.FramebufferManager;
import net.mine_diver.macula.utils.GL;
import net.mine_diver.macula.core.ShaderPack;
import net.mine_diver.macula.shaders.uniform.PositionUniforms;
import net.minecraft.client.render.WorldRenderer;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(
            method = "renderSky(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;method_286(F)F",
                    shift = At.Shift.AFTER
            )
    )
    private void onGetStarBrightness(float par1, CallbackInfo ci) {
        if (!ShaderPack.shaderPackLoaded) return;
        PositionUniforms.updateCelestialPosition();
    }

    @Inject(
            method = "renderLastChunks(ID)V",
            at = @At("HEAD")
    )
    private void beforeSmoothBetaRender(int d, double par2, CallbackInfo ci) {
        if (!SmoothBetaCompat.LOADED || !ShaderPack.shaderPackLoaded) return;
        GL20.glDrawBuffers(ARBFramebufferObject.GL_COLOR_ATTACHMENT0);
    }

    @Inject(
            method = "renderLastChunks(ID)V",
            at = @At("RETURN")
    )
    private void afterSmoothBetaRender(int d, double par2, CallbackInfo ci) {
        if (!SmoothBetaCompat.LOADED || !ShaderPack.shaderPackLoaded) return;
        GL20.glDrawBuffers(FramebufferManager.defaultDrawBuffers);
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"
            )
    )
    private void onGlEnable(int i) {
        if (SmoothBetaCompat.LOADED || !ShaderPack.shaderPackLoaded) {
            GL11.glEnable(i);
            return;
        }
        GL.glEnableWrapper(i);
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V"
            )
    )
    private void onGlDisable(int i) {
        if (SmoothBetaCompat.LOADED || !ShaderPack.shaderPackLoaded) {
            GL11.glDisable(i);
            return;
        }
        GL.glDisableWrapper(i);
    }
}
