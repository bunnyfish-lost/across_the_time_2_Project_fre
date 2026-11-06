package fr.poubone.att2.client.data;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Set;

public final class AttributeWeapon111 {
    private AttributeWeapon111() {}

    public static AttributeModifier modifier(Player var0, Holder<Attribute> var1, Identifier var2) {
        if (var0 == null) {
            return null;
        }

        AttributeModifier[] var3 = new AttributeModifier[] {null};
        ItemStack var4 = var0.getMainHandItem();
        if (var4 != null && !var4.isEmpty()) {
            var4.forEachModifier(
                    EquipmentSlot.MAINHAND,
                    (var3x, var4x) -> {
                        if (var3x.equals(var1) && var4x.id().equals(var2)) {
                            var3[0] = var4x;
                        }
                    });
        }

        if (var3[0] != null) {
            return var3[0];
        }

        AttributeInstance var5 = var0.getAttribute(var1);
        return var5 == null ? null : var5.getModifier(var2);
    }

    public static StatEffectText.WeaponStats of(Player var0) {
        AttributeModifier var1 =
                modifier(
                        var0,
                        Attributes.ATTACK_DAMAGE,
                        Identifier.withDefaultNamespace("attack_damage"));
        AttributeModifier var2 =
                modifier(
                        var0,
                        Attributes.ATTACK_SPEED,
                        Identifier.withDefaultNamespace("attack_speed"));
        return var1 != null && var2 != null
                ? new StatEffectText.WeaponStats(
                        (int) (var1.amount() * 100.0), (int) (var2.amount() * 1000.0))
                : null;
    }

    public static double strengthValue(Player var0, int var1, StatEffectText.WeaponStats var2) {
        AttributeInstance var3 = new AttributeInstance(Attributes.ATTACK_DAMAGE, var0x -> {});
        var3.setBaseValue(
                StatEffectText.strengthDamage(var1, var2.damageX100(), var2.attackSpeedX1000()));
        LinkedHashMap<Identifier, AttributeModifier> var4 = new LinkedHashMap();
        AttributeInstance var5 = var0.getAttribute(Attributes.ATTACK_DAMAGE);
        if (var5 != null) {
            for (AttributeModifier var7 : var5.getModifiers()) {
                var4.put(var7.id(), var7);
            }
        }

        for (EquipmentSlot var9 : EquipmentSlot.values()) {
            String var10 = var9.getName();
            if (Set.of("mainhand", "offhand", "head", "chest", "legs", "feet").contains(var10)) {
                ItemStack var11 =
                        var9 == EquipmentSlot.MAINHAND
                                ? var0.getMainHandItem()
                                : var0.getItemBySlot(var9);
                if (var11 != null && !var11.isEmpty()) {
                    var11.forEachModifier(
                            var9,
                            (var1x, var2x) -> {
                                if (var1x.equals(Attributes.ATTACK_DAMAGE)) {
                                    var4.put(var2x.id(), var2x);
                                }
                            });
                }
            }
        }

        for (AttributeModifier var15 : var4.values()) {
            var3.addTransientModifier(var15);
        }

        return Math.floor(var3.getValue());
    }
}
