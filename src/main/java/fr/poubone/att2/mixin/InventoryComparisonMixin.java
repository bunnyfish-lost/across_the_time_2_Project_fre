package fr.poubone.att2.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import fr.poubone.att2.client.shop.ShopComparison;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryComparisonMixin extends Screen {
    @Shadow protected Slot hoveredSlot;
    @Shadow @Final protected AbstractContainerMenu menu;

    @Shadow
    protected abstract List<Component> getTooltipFromContainerItem(ItemStack var1);

    protected InventoryComparisonMixin(Component var1) {
        super(var1);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void att2$compareHoveredItem(GuiGraphics var1, int var2, int var3, CallbackInfo var4) {
        ItemStack var5 = this.comparableHoveredStack();
        if (var5 != null && compareHeld()) {
            ShopComparison.render(
                    var1,
                    this.getTooltipFromContainerItem(var5),
                    Minecraft.getInstance().player,
                    ShopComparison.kind(var5),
                    var5,
                    var2,
                    var3,
                    this.width,
                    this.height);
            var4.cancel();
        }
    }

    @ModifyReturnValue(method = "getTooltipFromContainerItem", at = @At("RETURN"))
    private List<Component> att2$addCompareHint(List<Component> var1, ItemStack var2) {
        if (var2 == this.comparableHoveredStack() && !compareHeld()) {
            ArrayList var3 = new ArrayList(var1);
            var3.add(ShopComparison.compareHint().withStyle(ChatFormatting.GRAY));
            return var3;
        } else {
            return var1;
        }
    }

    private ItemStack comparableHoveredStack() {
        LocalPlayer var1 = Minecraft.getInstance().player;
        if (((Object) this instanceof InventoryScreen
                        || (Object) this instanceof CreativeModeInventoryScreen)
                && var1 != null) {
            if (this.hoveredSlot != null
                    && this.hoveredSlot.hasItem()
                    && this.menu.getCarried().isEmpty()) {
                ItemStack var2 = this.hoveredSlot.getItem();
                return ShopComparison.hasComparison(var1, var2) ? var2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void att2$cycleComparison(
            double var1,
            double var3,
            double var5,
            double var7,
            CallbackInfoReturnable<Boolean> var9) {
        ItemStack var10 = this.comparableHoveredStack();
        if (var10 != null
                && ShopComparison.scroll(
                        Minecraft.getInstance().player, ShopComparison.kind(var10), var10, var7)) {
            var9.setReturnValue(true);
        }
    }

    private static boolean compareHeld() {
        return ShopComparison.isHeld(0L, 0);
    }
}
