package fr.poubone.att2.client.input;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;

import fr.poubone.att2.client.shop.ShopComparison;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public final class SneakCompareInput117 {
    private static int cachedScan = -1;
    private static int cachedKey = -1;

    private SneakCompareInput117() {}

    public static boolean held() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0 != null && var0.options != null && var0.options.keyShift != null) {
            Key var1 = KeyBindingHelper.getBoundKeyOf(var0.options.keyShift);
            int var2 = var1.getValue();
            if (var2 < 0) {
                return false;
            }

            long var3 = var0.getWindow().handle();
            if (var1.getType() == Type.MOUSE) {
                return GLFW.glfwGetMouseButton(var3, var2) == 1;
            }

            if (var1.getType() == Type.SCANCODE) {
                if (cachedScan != var2) {
                    cachedScan = var2;
                    cachedKey = -1;

                    for (int var5 = 32; var5 <= 348; var5++) {
                        if (GLFW.glfwGetKeyScancode(var5) == var2) {
                            cachedKey = var5;
                            break;
                        }
                    }
                }

                var2 = cachedKey;
                if (var2 < 0) {
                    return false;
                }
            }

            return GLFW.glfwGetKey(var3, var2) == 1;
        } else {
            return false;
        }
    }

    public static void tick(Minecraft var0) {
        if (!ShopComparison.inShop() || !held()) {
            ShopComparison.release();
        }
    }

    public static Component label() {
        Minecraft var0 = Minecraft.getInstance();
        return var0 != null && var0.options != null && var0.options.keyShift != null
                ? var0.options.keyShift.getTranslatedKeyMessage()
                : Component.literal("下蹲键");
    }
}
