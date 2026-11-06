package com.lootbeams.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lootbeams.LootBeams;
import com.lootbeams.compat.prism.PrismCompat;
import com.lootbeams.helpers.TextColorHelper;
import com.lootbeams.utils.Selectors;

import fr.poubone.att2.client.data.CustomDataView;
import fr.poubone.att2.client.hud.LootItemIdentity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRarity {
    private static final String CUSTOM_RARITIES_FILE = "custom_rarities.json";
    private static final String CUSTOM_RARITIES_JSON_KEY = "custom_rarities";
    private static final Map<String, CustomRarity> RARITY_CACHE = new HashMap<>();
    private static final String CUSTOM_RARITY_KEY = "custom_rarity";
    private static final String CUSTOM_RARITY_ID_KEY = "id";
    private static final String CUSTOM_RARITY_NAME_KEY = "name";
    private static final String CUSTOM_RARITY_COLOR_KEY = "color";
    private static final String CUSTOM_RARITY_ANIMATED_COLOR_KEY = "animated_color";
    private static final String CUSTOM_RARITY_CONFIG_KEY = "config";
    private static final String CUSTOM_RARITY_SELECTORS_KEY = "selectors";
    public static final String ITEM_CUSTOM_RARITY_ID_KEY = "custom_rarity_id";
    private String name = "";
    private int color = 16777215;
    private TextColor animatedColor = null;
    private JsonObject rarityConfig = null;
    private List<String> selectors = new ArrayList<>();

    public CustomRarity(String var1) {
        this.name = var1;
    }

    public CustomRarity setColor(int var1) {
        this.color = var1;
        return this;
    }

    public CustomRarity setAnimatedColor(TextColor var1) {
        this.animatedColor = var1;
        return this;
    }

    public CustomRarity setConfig(JsonObject var1) {
        this.rarityConfig = var1;
        return this;
    }

    public CustomRarity setSelectors(List<String> var1) {
        this.selectors = var1;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public int getColor() {
        return this.animatedColor != null ? this.animatedColor.getValue() : this.color;
    }

    public boolean hasRarityConfig() {
        return this.rarityConfig != null;
    }

    public List<String> getSelectors() {
        return this.selectors;
    }

    public JsonObject getRarityConfig() {
        return this.rarityConfig;
    }

    public static CustomRarity fromSelectors(ItemStack var0) {
        for (CustomRarity var2 : RARITY_CACHE.values()) {
            if (!var2.getSelectors().isEmpty() && Minecraft.getInstance().level != null) {
                for (String var4 : var2.getSelectors()) {
                    if (Selectors.validateSelector(var4)
                            && Selectors.itemMatches(
                                    var0, var4, Minecraft.getInstance().level.registryAccess())) {
                        return var2;
                    }
                }
            }
        }

        return null;
    }

    public static CustomRarity fromItemStack(ItemStack var0) {
        if (var0.getComponents().has(DataComponents.CUSTOM_DATA)) {
            CustomData var1 = var0.get(DataComponents.CUSTOM_DATA);
            if (var1 != null) {
                CompoundTag var2 = CustomDataView.read(var1);
                if (!var2.isEmpty()) {
                    CompoundTag var3 = var2.getCompoundOrEmpty("custom_rarity");
                    if (!var3.isEmpty()) {
                        String var4 = var3.getStringOr("name", "");
                        String var5 = var3.getStringOr("color", "");
                        String var6 = var3.getStringOr("animated_color", "");
                        if (!var4.isEmpty()) {
                            CustomRarity var14 =
                                    new CustomRarity(Component.translatable(var4).getString());
                            if (!var5.isEmpty()) {
                                var14.setColor(TextColorHelper.getColorFromHEX(var5));
                            }

                            if (!var6.isEmpty() && PrismCompat.isPrismLoaded()) {
                                TextColor var15 = PrismCompat.parseColor(var6);
                                var14.setAnimatedColor(var15);
                                var14.setColor(var15.getValue());
                            }

                            return var14;
                        }
                    }

                    String var11 = var2.getStringOr("EquipmentID", "");
                    String var12 = var2.getStringOr("custom_rarity_id", "");
                    String var13 = var2.getStringOr("Coin", "");
                    String var7 = var2.getStringOr("Rarity", "");
                    String var8 = LootItemIdentity.coinRarityId(var13);
                    if (var8 == null && ("cur".equals(var7) || "esc".equals(var7))) {
                        var8 = var7;
                    }

                    String var9 =
                            LootBeamIdResolver.firstCached(
                                    RARITY_CACHE, var8, var11, var12, var13, var7);
                    if (!var9.isEmpty()) {
                        return RARITY_CACHE.get(var9);
                    }
                }
            }
        }

        CustomRarity var10 = fromSelectors(var0);
        return var10 != null ? var10 : null;
    }

    public static Component toText(CustomRarity var0) {
        Style var1 = Style.EMPTY.withItalic(false).withColor(var0.getColor());
        return Component.literal(var0.getName()).setStyle(var1);
    }

    public static void onResourcesReload(ResourceManager var0) {
        RARITY_CACHE.clear();

        for (Resource var2 : var0.getResourceStack(LootBeams.id("custom_rarities.json"))) {
            try {
                JsonObject var3 = GsonHelper.parse(var2.openAsReader());
                if (var3.has("custom_rarities")) {
                    for (JsonElement var5 : GsonHelper.getAsJsonArray(var3, "custom_rarities")) {
                        if (var5.isJsonObject()) {
                            parseAndPut(var5.getAsJsonObject());
                        }
                    }
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }

    private static void parseAndPut(JsonObject var0) {
        if (var0.has("id")) {
            String var1 = GsonHelper.getAsString(var0, "id");
            String var2 = GsonHelper.getAsString(var0, "name");
            String var3 = GsonHelper.getAsString(var0, "color");
            String var4 = GsonHelper.getAsString(var0, "animated_color", "");
            JsonArray var5 = GsonHelper.getAsJsonArray(var0, "selectors", new JsonArray());
            JsonObject var6 = GsonHelper.getAsJsonObject(var0, "config", new JsonObject());
            if (!var1.isEmpty() && !var2.isEmpty()) {
                CustomRarity var7 = new CustomRarity(Component.translatable(var2).getString());
                if (!var3.isEmpty()) {
                    var7.setColor(TextColorHelper.getColorFromHEX(var3));
                }

                if (!var4.isEmpty() && PrismCompat.isPrismLoaded()) {
                    TextColor var8 = PrismCompat.parseColor(var4);
                    var7.setAnimatedColor(var8);
                    var7.setColor(var8.getValue());
                }

                if (!var5.isEmpty()) {
                    ArrayList var11 = new ArrayList();

                    for (JsonElement var10 : var5) {
                        var11.add(GsonHelper.convertToString(var10, "selector"));
                    }

                    if (!var11.isEmpty()) {
                        var7.setSelectors(var11);
                    }
                }

                if (!var6.isEmpty()) {
                    var7.setConfig(var6);
                }

                RARITY_CACHE.put(var1, var7);
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName()
                + "[name="
                + this.name
                + ", color="
                + this.color
                + ", rarityConfig="
                + this.rarityConfig
                + "]";
    }
}
