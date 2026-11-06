package fr.poubone.att2.client.compat;

import fr.poubone.att2.client.gambling.GamblingModel;
import fr.poubone.att2.client.input.SneakMenuBypass;
import fr.poubone.att2.client.miner.MinerShopModel;
import fr.poubone.att2.client.quest.QuestModel;
import fr.poubone.att2.client.rune.RuneCodexModel;
import fr.poubone.att2.client.rune.RunePouchSync1115;
import fr.poubone.att2.client.rune.RuneWordCraft119;
import fr.poubone.att2.client.shop.ShopModel;

import net.minecraft.network.chat.Component;

public final class MenuDispatch206 {
    private MenuDispatch206() {}

    public static boolean allow(Component var0, boolean var1) {
        if (!RuneWordCraft119.onMessage(var0, var1)) {
            return false;
        }

        if (FlashbackCompat.isInReplay()) {
            return true;
        }

        boolean var2 = !var1 && SneakMenuBypass.isActive();
        if (var2) {
            if (RunePouchSync1115.onMessage(var0)) {
                return false;
            }
        } else if (!var1) {
            if (GamblingModel.get().onSystemMessage(var0)) {
                return false;
            }

            if (RuneCodexModel.get().onSystemMessage(var0)) {
                return false;
            }

            if (MinerShopModel.get().onSystemMessage(var0)) {
                return false;
            }

            if (ShopModel.get().onSystemMessage(var0)) {
                return false;
            }
        }

        return QuestModel.get().onSystemMessage(var0, var1);
    }
}
