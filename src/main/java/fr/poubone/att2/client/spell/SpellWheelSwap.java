package fr.poubone.att2.client.spell;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.SpellLauncherTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SpellWheelSwap {
    private SpellWheelSwap() {}

    public static int sourceSlot(Minecraft var0) {
        if (var0 == null || var0.player == null) {
            return -1;
        } else if (SpellLauncherTracker.parse(var0.player.getMainHandItem()) != null) {
            return var0.player.getInventory().getSelectedSlot();
        } else {
            return SpellLauncherTracker.parse(var0.player.getOffhandItem()) != null ? 40 : -1;
        }
    }

    public static SpellLauncherTracker.LauncherState sourceSpell(Minecraft var0) {
        int var1 = sourceSlot(var0);
        return var1 < 0
                ? null
                : SpellLauncherTracker.parse(var0.player.getInventory().getItem(var1));
    }

    public static boolean canOpen(Minecraft var0) {
        return var0 != null
                && var0.player != null
                && var0.gameMode != null
                && !FlashbackCompat.isInReplay()
                && var0.isWindowActive()
                && var0.player.inventoryMenu != null
                && var0.player.isAlive()
                && !var0.player.isSpectator()
                && var0.player.containerMenu == var0.player.inventoryMenu
                && var0.player.containerMenu.getCarried().isEmpty()
                && sourceSlot(var0) >= 0;
    }

    public static Slot findSlot(Inventory var0, AbstractContainerMenu var1, int var2) {
        for (Slot var4 : var1.slots) {
            if (var4.container == var0 && var4.getContainerSlot() == var2) {
                return var4;
            }
        }

        return null;
    }

    public static SpellWheelSwap.Result choose(Minecraft var0, int var1, int var2, int var3) {
        return choose(var0, var1, var2, var3, var2);
    }

    public static SpellWheelSwap.Result choose(
            Minecraft var0, int var1, int var2, int var3, int var4) {
        if (canOpen(var0) && var0.screen == null) {
            LocalPlayer var5 = var0.player;
            Inventory var6 = var5.getInventory();
            SpellLauncherTracker.LauncherState var7 = sourceSpell(var0);
            if (var6.getSelectedSlot() == var2
                    && sourceSlot(var0) == var4
                    && var7 != null
                    && var7.spellId == var3
                    && var2 >= 0
                    && var2 < 9) {
                SpellWheelCatalog.Owned var8 =
                        SpellWheelCatalog.scan(var6, var5.getOffhandItem(), var4).get(var1);
                if (var8 == null) {
                    return SpellWheelSwap.Result.UNAVAILABLE;
                }

                if (var8.inventorySlot() == var4) {
                    return SpellWheelSwap.Result.SAME_SLOT;
                }

                AbstractContainerMenu var9 = var5.containerMenu;
                Slot var10 = findSlot(var6, var9, var8.inventorySlot());
                ItemStack var11 = var6.getItem(var4);
                if (var10 != null
                        && var10.mayPickup(var5)
                        && var10.mayPlace(var11)
                        && var11.getCount() <= var10.getMaxStackSize(var11)) {
                    SpellLauncherTracker.LauncherState var12 =
                            SpellLauncherTracker.parse(var10.getItem());
                    if (var12 != null && var12.spellId == var1) {
                        var0.gameMode.handleInventoryMouseClick(
                                var9.containerId, var10.index, var4, ClickType.SWAP, var5);
                        return SpellWheelSwap.Result.SWAPPED;
                    } else {
                        return SpellWheelSwap.Result.UNAVAILABLE;
                    }
                } else {
                    return SpellWheelSwap.Result.UNAVAILABLE;
                }
            } else {
                return SpellWheelSwap.Result.CONTEXT_CHANGED;
            }
        } else {
            return SpellWheelSwap.Result.CONTEXT_CHANGED;
        }
    }

    public enum Result {
        SWAPPED,
        SAME_SLOT,
        UNAVAILABLE,
        CONTEXT_CHANGED;
    }
}
