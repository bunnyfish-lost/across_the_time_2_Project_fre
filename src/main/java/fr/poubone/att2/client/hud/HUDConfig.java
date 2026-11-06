package fr.poubone.att2.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lootbeams.helpers.ItemHelper;

import fr.poubone.att2.client.util.ModLanguageManager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HUDConfig {
    public boolean currencyPickupAnimation = true;
    public boolean takeoverCityTitles = true;
    public boolean cityTransparentBackground;
    public boolean cityUseOriginalTextColors = true;
    public boolean showChronoton = true;
    public boolean showXP = true;
    public boolean showMana = true;
    public boolean showStats = true;
    public boolean showArrows = true;
    public boolean showArmorDurability = true;
    public boolean showSpellBar = true;
    public boolean showTemperature = true;
    public boolean orbGpuRendering = true;
    public int orbFps = -1;
    public Map<String, HudSlot> slots = new LinkedHashMap<>();
    public boolean dropLockEnabled = true;
    public List<String> dropLockRarities = new ArrayList<>(List.of("myt", "que"));
    public boolean discordRichPresence = true;
    public boolean discordShowQuest = true;
    public boolean discordRegionImage = true;
    public String discordApplicationId = "1545088290577981521";
    public String discordMapUrl = "https://adventquest.com/across-the-time-ii-time-for-regrets";
    public String discordModUrl = "https://github.com/Poubone/ATT2_Project";
    public String discordLargeImage = "logo-brillant";
    public List<String> discordUploadedAssets = new ArrayList<>(List.of("logo-brillant"));
    private static final String DEFAULT_DISCORD_APP_ID = "1545088290577981521";
    private static final String DEFAULT_DISCORD_MAP_URL =
            "https://adventquest.com/across-the-time-ii-time-for-regrets";
    private static final String DEFAULT_DISCORD_MOD_URL = "https://github.com/Poubone/ATT2_Project";
    private static final String DEFAULT_DISCORD_LARGE_IMAGE = "logo-brillant";
    public String modLanguage = "fr";
    public volatile boolean partySyncEnabled = true;
    public String partySyncUrl = "https://sync.guide-att2.com";
    public String partySyncToken = "";
    public List<String> renderRarities =
            new ArrayList<>(
                    List.of(
                            "com",
                            "cur",
                            "epi",
                            "epi_set",
                            "leg",
                            "leg_armset",
                            "misc",
                            "myt",
                            "que",
                            "rar",
                            "spe",
                            "ult",
                            "unc",
                            "unk",
                            "epi_esc",
                            "esc"));
    public boolean allItems = true;
    public boolean onlyEquipment = false;
    public boolean onlyRare = false;
    public List<String> whitelist = new ArrayList<>();
    public List<String> blacklist = new ArrayList<>();
    public List<String> colorOverrides = new ArrayList<>();
    public boolean renderNameColor = true;
    public boolean renderRarityColor = true;
    public float beamRadius = 1.0F;
    public float beamHeight = 1.0F;
    public float beamYOffset = 0.0F;
    public float beamAlpha = 0.85F;
    public float renderDistance = 24.0F;
    public float fadeDistance = 2.0F;
    public boolean borders = true;
    public boolean renderNametags = true;
    public boolean renderNametagsOnlook = true;
    public boolean renderStackcount = true;
    public float nametagLookSensitivity = 0.018F;
    public float nametagTextAlpha = 1.0F;
    public float nametagBackgroundAlpha = 0.5F;
    public float nametagScale = 1.0F;
    public float nametagYOffset = 0.75F;
    public List<String> alwaysDrawRaritiesOn = List.of("#minecraft:music_discs");
    public boolean whiteRarities = false;
    public boolean questBookShowMain = true;
    public boolean questBookShowSide = true;
    public boolean questBookShowCompleted = true;
    public boolean questBookShowDaily = true;
    public boolean charlesAutoOpen = true;
    public boolean runeMenuEnabled = true;
    public boolean minerMenuEnabled = true;
    public boolean questMenuEnabled = true;
    public boolean shopAutoOpen = true;
    public Map<String, Boolean> shopMenus = null;
    public static final List<String> SHOP_MENU_IDS =
            List.of(
                    "blacksmith",
                    "food",
                    "alchemist",
                    "fletcher",
                    "dahal",
                    "tailor",
                    "fish",
                    "stable",
                    "general");
    public String questBookTheme = "minecraft";
    public String questBookTitleColor = "#FFFF55";
    public String questBookTextColor = "#000000";
    public String questBookSecondaryTextColor = "#404040";
    public String questBookEntryColor = "#B5AE97";
    public String questBookEntryHoverColor = "#797465";
    public String questBookSelectedColor = "#FFCE7F";
    public String questBookSelectedHoverColor = "#FFC432";
    public String questBookFilterColor = "#B5AE97";
    public String questBookFilterHoverColor = "#797465";
    public String questBookFilterEnabledColor = "#A4D48E";
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(), "att2_hud.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HUDConfig INSTANCE;
    public static final int[] ORB_FPS_STEPS = new int[] {15, 30, 60, 120, 0};
    public static final int ORB_FPS_AUTO = -1;
    public boolean takeoverLevelTitles = true;
    public boolean takeoverOtherTitles = true;
    public boolean batchLootBeams = true;
    public boolean cullLootBeams = true;
    public boolean cacheLootBeamConfig = true;
    public boolean pruneBeamTimersPerFrame = true;
    public boolean dustWithoutCollisions = true;
    public boolean cullHiddenItemFaces = true;
    public boolean cacheItemBounds = true;
    public boolean directItemOutlines = true;
    public boolean cacheNameTags = true;
    public boolean lightItemChecks = true;
    public boolean lootPiles = true;
    public boolean sneakSkipsMenus = true;
    public boolean shopHideOwnedSpells = true;
    public boolean solidItemEntities;
    public int lootBeamLimit;
    public int mapItemParticleLimit;
    public List<String> visibleStatKeys;
    public int statsDisplayMode;
    public int statsLayoutMode;
    public boolean shopFollowGuiScale;
    public boolean disableGrayBackground;

    public HUDConfig() {
        StatsSettings207.defaults(this);
    }

    public static int color(String var0, int var1) {
        if (var0 == null) {
            return var1;
        }

        String var2 = var0.trim();
        if (var2.startsWith("#")) {
            var2 = var2.substring(1);
        }

        try {
            long var3 = Long.parseLong(var2, 16);
            if (var2.length() <= 6) {
                var3 |= 4278190080L;
            }

            return (int) var3;
        } catch (NumberFormatException var5) {
            return var1;
        }
    }

    public static HUDConfig get() {
        if (INSTANCE == null) {
            load();
        }

        return INSTANCE;
    }

    public boolean orbsOnGpu() {
        return this.orbGpuRendering && GlobeShader.isAvailable();
    }

    public int effectiveOrbFps() {
        if (this.orbFps != -1) {
            return this.orbFps;
        } else {
            return this.orbsOnGpu() ? 0 : 30;
        }
    }

    public static boolean isValidOrbFps(int var0) {
        if (var0 == -1) {
            return true;
        }

        for (int var4 : ORB_FPS_STEPS) {
            if (var4 == var0) {
                return true;
            }
        }

        return false;
    }

    public static void load() {
        try {
            if (FILE.exists()) {
                try (FileReader var0 = new FileReader(FILE, StandardCharsets.UTF_8)) {
                    INSTANCE = (HUDConfig) GSON.fromJson(var0, HUDConfig.class);
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        if (INSTANCE == null) {
            INSTANCE = new HUDConfig();
        }

        if (INSTANCE.renderRarities == null) {
            INSTANCE.renderRarities =
                    new ArrayList<>(
                            List.of(
                                    "com",
                                    "cur",
                                    "epi",
                                    "epi_set",
                                    "leg",
                                    "leg_armset",
                                    "misc",
                                    "myt",
                                    "que",
                                    "rar",
                                    "spe",
                                    "ult",
                                    "unc",
                                    "unk",
                                    "epi_esc",
                                    "esc"));
        }

        if (INSTANCE.whitelist == null) {
            INSTANCE.whitelist = new ArrayList<>();
        }

        if (INSTANCE.blacklist == null) {
            INSTANCE.blacklist = new ArrayList<>();
        }

        if (INSTANCE.slots == null) {
            INSTANCE.slots = new LinkedHashMap<>();
        }

        if (INSTANCE.slots != null) {
            for (HudSlot var1 : INSTANCE.slots.values()) {
                if (var1 != null) {
                    if (var1.scale <= 0.0F) {
                        var1.scale = 1.0F;
                    }

                    if (var1.gap <= 0.0F) {
                        var1.gap = 1.0F;
                    }
                }
            }
        }

        if (INSTANCE.discordUploadedAssets == null) {
            INSTANCE.discordUploadedAssets = new ArrayList<>(List.of("logo-brillant"));
        }

        INSTANCE.modLanguage = ModLanguageManager.normalize(INSTANCE.modLanguage);
        if (INSTANCE.discordApplicationId == null || INSTANCE.discordApplicationId.isBlank()) {
            INSTANCE.discordApplicationId = "1545088290577981521";
        }

        if (INSTANCE.discordMapUrl == null || INSTANCE.discordMapUrl.isBlank()) {
            INSTANCE.discordMapUrl = "https://adventquest.com/across-the-time-ii-time-for-regrets";
        }

        if (INSTANCE.discordModUrl == null || INSTANCE.discordModUrl.isBlank()) {
            INSTANCE.discordModUrl = "https://github.com/Poubone/ATT2_Project";
        }

        if (INSTANCE.discordLargeImage == null || INSTANCE.discordLargeImage.isBlank()) {
            INSTANCE.discordLargeImage = "logo-brillant";
        }

        if (!isValidOrbFps(INSTANCE.orbFps)) {
            INSTANCE.orbFps = -1;
        }

        if (INSTANCE.partySyncUrl == null) {
            INSTANCE.partySyncUrl = "";
        }

        if (INSTANCE.partySyncToken == null) {
            INSTANCE.partySyncToken = "";
        }

        if (INSTANCE.shopMenus == null) {
            INSTANCE.shopMenus = new LinkedHashMap<>();

            for (String var9 : SHOP_MENU_IDS) {
                INSTANCE.shopMenus.put(var9, INSTANCE.shopAutoOpen);
            }
        } else {
            for (String var10 : SHOP_MENU_IDS) {
                INSTANCE.shopMenus.putIfAbsent(var10, true);
            }
        }

        StatsSettings207.normalize(get());
    }

    public boolean isShopMenuEnabled(String var1) {
        return this.shopMenus != null && var1 != null
                ? this.shopMenus.getOrDefault(var1, true)
                : this.shopAutoOpen;
    }

    public void setShopMenuEnabled(String var1, boolean var2) {
        if (this.shopMenus == null) {
            this.shopMenus = new LinkedHashMap<>();
        }

        this.shopMenus.put(var1, var2);
    }

    public static void save() {
        try (FileWriter var0 = new FileWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(INSTANCE, var0);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

    public static void setModLanguage(String var0) {
        get().modLanguage = ModLanguageManager.normalize(var0);
        save();
    }

    public static String getModLanguage() {
        return get().modLanguage;
    }

    public static TextColor getColorFromItemOverrides(Item var0) {
        List<String> var1 = get().colorOverrides;
        if (var1.isEmpty()) {
            return null;
        }

        for (String var3 : var1.stream().filter(var0x -> !var0x.isEmpty()).toList()) {
            String[] var4 = var3.split("=");
            if (var4.length == 2) {
                String var5 = var4[0];
                Optional var6 = TextColor.parseColor(var4[1]).result();
                if (var6.isEmpty()) {
                    return null;
                }

                if (matchesItem(var5, var0)) {
                    return (TextColor) var6.get();
                }
            }
        }

        return null;
    }

    public static boolean shouldRenderLootBeam(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            HUDConfig var1 = get();
            Item var2 = var0.getItem();
            boolean var3 = matchesAnyList(var1.whitelist, var2);
            boolean var4 = matchesAnyList(var1.blacklist, var2);
            boolean var5 =
                    var1.allItems
                            || var1.onlyEquipment && ItemHelper.isEquipmentItem(var0)
                            || var1.onlyRare && var0.getRarity() != Rarity.COMMON
                            || var3;
            if (var5 && !var4) {
                String var6 = lootRarityId(var0);
                return var6 != null
                        && var1.renderRarities != null
                        && var1.renderRarities.contains(var6);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static String lootRarityId(ItemStack var0) {
        return LootItemIdentity.rarityId(var0);
    }

    private static boolean matchesAnyList(List<String> var0, Item var1) {
        if (var0 != null && !var0.isEmpty()) {
            for (String var3 : var0) {
                if (matchesItem(var3, var1)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean matchesItem(String var0, Item var1) {
        if (var0 != null && !var0.isEmpty()) {
            Identifier var2 = BuiltInRegistries.ITEM.getKey(var1);
            if (!var0.contains(":")) {
                return var2.getNamespace().equals(var0);
            }

            Identifier var3 = Identifier.tryParse(var0.replace("#", ""));
            if (var3 == null) {
                return false;
            }

            if (!var0.startsWith("#")) {
                return var2.equals(var3);
            }

            Optional var4 = BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, var3));
            return var4.isPresent() && ((Named) var4.get()).contains(var1.builtInRegistryHolder());
        } else {
            return false;
        }
    }

    public boolean isStatVisible(String var1) {
        return StatsSettings207.visible(this, var1);
    }

    public void setStatVisible(String var1, boolean var2) {
        StatsSettings207.setVisible(this, var1, var2);
    }
}
