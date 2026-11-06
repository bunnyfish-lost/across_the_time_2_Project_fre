package fr.poubone.att2.client.screen;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import fr.poubone.att2.client.hud.HUDConfig;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class SettingsSaveState {
    private final Gson gson = new Gson();
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("att2_hud.json");
    private String disk = this.read();
    private JsonElement saved = this.initial();

    private JsonElement initial() {
        JsonElement var1 = this.parse(this.disk);
        return var1 != null ? var1 : this.gson.toJsonTree(new HUDConfig());
    }

    private String read() {
        try {
            return Files.exists(this.file) ? Files.readString(this.file) : null;
        } catch (Exception var2) {
            return null;
        }
    }

    private JsonElement parse(String var1) {
        try {
            return var1 == null
                    ? null
                    : this.gson.toJsonTree(this.gson.fromJson(var1, HUDConfig.class));
        } catch (Exception var3) {
            return null;
        }
    }

    public boolean dirty() {
        String var1 = this.read();
        if (!Objects.equals(var1, this.disk)) {
            JsonElement var2 = this.parse(var1);
            if (var2 != null) {
                this.saved = var2;
                this.disk = var1;
            }
        }

        return !this.saved.equals(this.gson.toJsonTree(HUDConfig.get()));
    }

    public boolean save() {
        JsonElement var1 = this.gson.toJsonTree(HUDConfig.get());
        HUDConfig.save();
        String var2 = this.read();
        JsonElement var3 = this.parse(var2);
        if (var3 != null && var1.equals(var3)) {
            this.saved = var1;
            this.disk = var2;
            return true;
        } else {
            return false;
        }
    }
}
