package com.ultramenu.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.ultramenu.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HandMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
    private void um$hand(CallbackInfo ci,
                         @Local(argsOnly = true) MatrixStack matrices,
                         @Local(argsOnly = true) Hand hand) {
        Config c = Config.I;
        if (!c.handsEnabled) return;

        Arm main = MinecraftClient.getInstance().options.getMainArm().getValue();
        float side = ((main == Arm.RIGHT) == (hand == Hand.MAIN_HAND)) ? 1f : -1f;

        matrices.translate(side * c.handX, c.handY, c.handZ);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.handRotX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * c.handRotY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * c.handRotZ));
        matrices.scale(c.handScale, c.handScale, c.handScale);
    }
}
