package fr.poubone.att2.client.spell;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;

import fr.poubone.att2.client.input.KeybindManager;
import fr.poubone.att2.client.screen.SpellWheelScreen;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class SpellWheelInput {
    private static boolean down;
    private static int cachedScan = -1;
    private static int cachedKey = -1;

    private SpellWheelInput() {}

    public static boolean held(Minecraft var0) {
        return held(var0, KeybindManager.getSpellWheelKey());
    }

    public static boolean held(Minecraft var0, KeyMapping var1) {
        if (var1 == null) {
            return false;
        }

        Key var2 = KeyBindingHelper.getBoundKeyOf(var1);
        int var3 = var2.getValue();
        if (var3 < 0) {
            return false;
        }

        long var4 = var0.getWindow().handle();
        if (var2.getType() == Type.MOUSE) {
            return GLFW.glfwGetMouseButton(var4, var3) == 1;
        }

        if (var2.getType() != Type.KEYSYM) {
            if (cachedScan != var3) {
                cachedScan = var3;
                cachedKey = -1;

                for (int var6 = 32; var6 <= 348; var6++) {
                    if (GLFW.glfwGetKeyScancode(var6) == var3) {
                        cachedKey = var6;
                        break;
                    }
                }
            }

            var3 = cachedKey;
        }

        return var3 >= 0 && GLFW.glfwGetKey(var4, var3) == 1;
    }

    public static void tick(Minecraft var0) {
        boolean var1 = held(var0);
        if (!var1) {
            down = false;
        } else if (!down) {
            down = true;
            if (var0.screen == null && SpellWheelSwap.canOpen(var0)) {
                var0.setScreen(new SpellWheelScreen(var0));
            }
        }
    }
}
