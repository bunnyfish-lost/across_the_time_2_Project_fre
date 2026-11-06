package fr.poubone.att2.client.gambling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ScratchClickGuard {
    private final Map<Integer, Long> pending = new HashMap<>();

    public void clear() {
        this.pending.clear();
    }

    public boolean accept(
            GamblingModel.GridCell var1, List<List<GamblingModel.GridCell>> var2, long var3) {
        HashSet var5 = new HashSet();

        for (List<GamblingModel.GridCell> var7 : var2) {
            for (GamblingModel.GridCell var9 : var7) {
                if (var9.isLocked() && var9.trigger() >= 0) {
                    var5.add(var9.trigger());
                }
            }
        }

        this.pending
                .entrySet()
                .removeIf(
                        var3x ->
                                !var5.contains(var3x.getKey())
                                        || var3 - var3x.getValue() >= 10000L);
        if (var1 != null
                && var1.isLocked()
                && var1.trigger() >= 0
                && !this.pending.containsKey(var1.trigger())) {
            this.pending.put(var1.trigger(), var3);
            return true;
        } else {
            return false;
        }
    }
}
