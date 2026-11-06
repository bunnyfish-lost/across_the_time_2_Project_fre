package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.data.Att2Triggers;

import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class ShopRecyclerFilter1127 {
    private ShopRecyclerFilter1127() {}

    public static boolean isRecycler(Component var0) {
        if (var0 == null) {
            return false;
        }

        ArrayDeque var1 = new ArrayDeque();
        Set var2 = Collections.newSetFromMap(new IdentityHashMap());
        var1.add(var0);

        while (!var1.isEmpty()) {
            Component var3 = (Component) var1.removeFirst();
            if (var2.add(var3)) {
                if (var3.getStyle().getClickEvent() instanceof RunCommand var4) {
                    int var12 = Att2Triggers.parseTriggerCommand(var4.command());
                    if (var12 == 726 || var12 == 393) {
                        return true;
                    }
                }

                if (var3.getContents() instanceof ScoreContents var10
                        && "RECYCLER".equals(var10.objective())) {
                    return true;
                }

                var1.addAll(var3.getSiblings());
                if (var3.getContents() instanceof TranslatableContents var11) {
                    for (Object var8 : var11.getArgs()) {
                        if (var8 instanceof Component var9) {
                            var1.addLast(var9);
                        }
                    }
                }
            }
        }

        return false;
    }
}
