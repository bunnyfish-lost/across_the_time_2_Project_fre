package fr.poubone.att2.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.screen.HUDConfigScreen;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;

public final class Att2ModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<HUDConfigScreen> getModConfigScreenFactory() {
        return var0 -> {
            Minecraft var1 = Minecraft.getInstance();
            if (var1 != null && var1.getResourceManager() != null) {
                ModLanguageManager.loadLanguage(var1, HUDConfig.getModLanguage());
            }

            return new HUDConfigScreen(var0);
        };
    }
}
