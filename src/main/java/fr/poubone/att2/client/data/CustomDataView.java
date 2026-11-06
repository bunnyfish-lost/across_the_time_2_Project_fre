package fr.poubone.att2.client.data;

import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.mixin.CustomDataAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

public final class CustomDataView {
    private CustomDataView() {}

    public static CompoundTag read(CustomData var0) {
        return HUDConfig.get().lightItemChecks && (Object) var0 instanceof CustomDataAccessor var1
                ? var1.att2$tag()
                : var0.copyTag();
    }
}
