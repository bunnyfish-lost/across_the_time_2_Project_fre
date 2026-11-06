package fr.poubone.att2.mixin.screen;

import fr.poubone.att2.client.screen.GuiBackdrop1132;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenBackdrop1132Mixin {
    @Inject(
            method = {
                "renderTransparentBackground(Lnet/minecraft/client/gui/GuiGraphics;)V",
                "renderBlurredBackground(Lnet/minecraft/client/gui/GuiGraphics;)V",
                "renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"
            },
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = true)
    private void att2$skipBackdrop1132(GuiGraphics var1, CallbackInfo var2) {
        if (GuiBackdrop1132.hide((Screen) (Object) this)) {
            var2.cancel();
        }
    }
}
