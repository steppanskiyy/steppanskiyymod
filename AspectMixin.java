package com.ultramenu.mixin;

import com.ultramenu.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class AspectMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), require = 0)
    private void um$aspect(CallbackInfoReturnable<Matrix4f> cir) {
        float target = Config.I.aspect;
        if (target <= 0.01f) return;
        Window w = MinecraftClient.getInstance().getWindow();
        float cur = (float) w.getFramebufferWidth() / (float) w.getFramebufferHeight();
        cir.getReturnValue().scale(cur / target, 1f, 1f);
    }
}
