package fr.poubone.att2.mixin.rune;

import fr.poubone.att2.client.rune.RecipeTracking119;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class RecipeSlotHighlightMixin119 {
    @Inject(
            method =
                    "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;II)V",
            at = @At("TAIL"))
    private void fre$highlightMaterial(
            GuiGraphics var1, Slot var2, int var3, int var4, CallbackInfo var5) {
        RecipeTracking119.renderSlot(var1, var2);
    }

    @Inject(
            method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void fre$cancelRecipe(KeyEvent var1, CallbackInfoReturnable<Boolean> var2) {
        if (RecipeTracking119.onKey(var1)) {
            var2.setReturnValue(true);
        }
    }
}
