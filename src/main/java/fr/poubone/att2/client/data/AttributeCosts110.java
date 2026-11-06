package fr.poubone.att2.client.data;

import java.util.Map;
import java.util.OptionalInt;

public final class AttributeCosts110 {
    private static final Map<String, int[]> COSTS =
            Map.of(
                    "STR",
                    new int[] {1, 3, 4, 5, 6, 8, 10, 12, 13, 14, 15, 16, 17, 18},
                    "CRT",
                    new int[] {4, 6, 8, 10, 12, 14, 16, 18, 20, 22},
                    "RES",
                    new int[] {4, 8, 10, 12, 16, 18, 20, 20},
                    "SPD",
                    new int[] {2, 3, 6, 9, 11, 13, 15, 17, 19, 20, 21, 22},
                    "HAS",
                    new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
                    "HER",
                    new int[] {1, 3, 5, 8, 11, 14, 17, 20},
                    "DAR",
                    new int[] {1, 2, 3, 4, 6, 8, 10, 12, 14, 16, 18, 20},
                    "LUC",
                    new int[] {3, 5, 7, 10, 12, 15, 16, 18, 19, 20},
                    "HUN",
                    new int[] {2, 3, 4, 5, 6, 6, 7, 7, 7, 8, 8, 9, 9, 10});

    private AttributeCosts110() {}

    public static OptionalInt spent(String var0, int var1) {
        int[] var2 = COSTS.get(var0);
        if (var2 != null && var1 >= 0 && var1 <= var2.length) {
            int var3 = 0;

            for (int var4 = 0; var4 < var1; var4++) {
                var3 += var2[var4];
            }

            return OptionalInt.of(var3);
        } else {
            return OptionalInt.empty();
        }
    }

    public static OptionalInt next(String var0, int var1) {
        int[] var2 = COSTS.get(var0);
        return var2 != null && var1 >= 0 && var1 < var2.length
                ? OptionalInt.of(var2[var1])
                : OptionalInt.empty();
    }

    public static OptionalInt totalSpent() {
        if (!StatUpgradeModel.hasData()) {
            return OptionalInt.empty();
        }

        int var0 = 0;

        for (String var2 : StatManager.STAT_KEYS) {
            OptionalInt var3 = StatUpgradeModel.base(var2);
            if (var3.isEmpty()) {
                return OptionalInt.empty();
            }

            OptionalInt var4 = spent(var2, var3.getAsInt());
            if (var4.isEmpty()) {
                return OptionalInt.empty();
            }

            OptionalInt var5 = StatUpgradeModel.upgradeCost(var2);
            OptionalInt var6 = next(var2, var3.getAsInt());
            if (var5.isEmpty()
                    || var6.isPresent() && var5.getAsInt() != var6.getAsInt()
                    || var6.isEmpty() && var5.getAsInt() != 0) {
                return OptionalInt.empty();
            }

            var0 += var4.getAsInt();
        }

        return OptionalInt.of(var0);
    }
}
