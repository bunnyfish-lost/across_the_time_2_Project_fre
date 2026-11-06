package fr.poubone.att2.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import fr.poubone.att2.client.renderer.ItemParticleBudget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
public abstract class DroppedParticlePackets206 {
    @WrapMethod(method = "handleParticleEvent")
    private void att2$thinDroppedDust206(
            ClientboundLevelParticlesPacket var1, Operation<Void> var2) {
        if (!Minecraft.getInstance().isSameThread()) {
            var2.call(new Object[] {var1});
        } else {
            ItemParticleBudget.Verdict var3 = ItemParticleBudget.classify(var1);
            if (var3 != ItemParticleBudget.Verdict.HIDE) {
                boolean var4 = ItemParticleBudget.isSpawningItemDust();
                ItemParticleBudget.setSpawningItemDust(var3 == ItemParticleBudget.Verdict.SHOW);

                try {
                    var2.call(new Object[] {var1});
                } finally {
                    ItemParticleBudget.setSpawningItemDust(var4);
                }
            }
        }
    }
}
