package com.ultramenu.mixin;

import com.ultramenu.Config;
import com.ultramenu.UltraMenuClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class ZoomMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void um$zoom(CallbackInfoReturnable<Float> cir) {
        if (UltraMenuClient.zooming) {
            cir.setReturnValue(cir.getReturnValue() / Math.max(1f, Config.I.zoomLevel));
        }
    }
}
