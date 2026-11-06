package fr.poubone.att2.client.rune;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public final class RuneResultIcons119 {
    private static final String[] ITEMS =
            new String[] {
                "angler_pottery_sherd",
                "archer_pottery_sherd",
                "arms_up_pottery_sherd",
                "blade_pottery_sherd",
                "brewer_pottery_sherd",
                "burn_pottery_sherd",
                "danger_pottery_sherd",
                "explorer_pottery_sherd",
                "friend_pottery_sherd",
                "heart_pottery_sherd",
                "heartbreak_pottery_sherd",
                "howl_pottery_sherd",
                "miner_pottery_sherd",
                "mourner_pottery_sherd",
                "plenty_pottery_sherd",
                "prize_pottery_sherd",
                "sheaf_pottery_sherd",
                "shelter_pottery_sherd",
                "skull_pottery_sherd",
                "snort_pottery_sherd"
            };
    private static final String[] RARITIES =
            new String[] {
                "com", "com", "com", "unc", "unc", "unc", "unc", "rar", "rar", "rar", "rar", "epi",
                "epi", "epi", "epi", "leg", "leg", "leg", "leg", "myt"
            };

    public static ItemStack word(RuneCatalog.WordDef var0) {
        int var1 = var0.wordId();
        if (var1 == 20) {
            return RuneItems.named(
                    RuneWorkshopRecipes119.item("blue_bundle").getItem(), var0.nameKey());
        }

        ItemStack var2 = RuneWorkshopRecipes119.item(var1 == 21 ? "written_book" : ITEMS[var1]);
        CompoundTag var3 = new CompoundTag();
        var3.putString("Rarity", var1 == 21 ? "leg" : RARITIES[var1]);
        if (var1 == 21) {
            var3.putInt("Spell", 31);
            var3.putString("Dahal", "book");
            var3.putBoolean("Spell31", true);
            var2.set(
                    DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(34031.0F), List.of(), List.of(), List.of()));
        } else {
            var3.putString("EquipmentType", "runic_word");
            var3.putString("RuneWord", String.valueOf(var1));
        }

        var2.set(DataComponents.CUSTOM_DATA, CustomData.of(var3));
        var2.set(DataComponents.CUSTOM_NAME, Component.translatable(var0.nameKey()));
        var2.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return var2;
    }
}
