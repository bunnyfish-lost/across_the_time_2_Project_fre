package fr.poubone.att2.client.miner;

import fr.poubone.att2.client.rune.RuneCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MinerCatalog {
    public static final int MENU_POWDER = 3488;
    public static final int MENU_BUY = 3489;
    public static final int ACTION_RESIN = 3490;
    public static final int ACTION_ORE = 3536;
    public static final int MENU_EXCHANGE = 3535;
    public static final int BUY_FIRST = 3495;
    public static final int BUY_LAST = 3521;
    public static final int POWDER_FIRST = 3491;
    public static final int POWDER_LAST = 3494;
    private static final List<MinerCatalog.BuyOffer> BUYS;
    private static final List<MinerCatalog.PowderOffer> POWDERS =
            List.of(
                    new MinerCatalog.PowderOffer(50, 1, 3491),
                    new MinerCatalog.PowderOffer(250, 5, 3492),
                    new MinerCatalog.PowderOffer(500, 10, 3493),
                    new MinerCatalog.PowderOffer(1000, 20, 3494));

    private MinerCatalog() {}

    public static List<MinerCatalog.BuyOffer> buys() {
        return BUYS;
    }

    public static List<MinerCatalog.PowderOffer> powders() {
        return POWDERS;
    }

    public static Optional<RuneCatalog.RuneDef> runeByName(String var0) {
        if (var0 == null) {
            return Optional.empty();
        }

        String var1 = var0.strip().toLowerCase();
        return RuneCatalog.byId(var1);
    }

    public static boolean isMenuTrigger(int var0) {
        return var0 == 3488 || var0 == 3489 || var0 == 3535;
    }

    public static boolean isBuyTrigger(int var0) {
        return var0 >= 3495 && var0 <= 3521;
    }

    public static boolean isPowderTrigger(int var0) {
        return var0 >= 3491 && var0 <= 3494;
    }

    public static boolean isExchangeTrigger(int var0) {
        return var0 == 3532 || var0 == 3533 || var0 == 3534 || var0 >= 3576 && var0 <= 3582;
    }

    public static boolean isActionTrigger(int var0) {
        return var0 == 3490 || var0 == 3536;
    }

    public static boolean isPrisonerGameplayTrigger(int var0) {
        return isMenuTrigger(var0)
                || isBuyTrigger(var0)
                || isPowderTrigger(var0)
                || isExchangeTrigger(var0)
                || isActionTrigger(var0);
    }

    public static MinerCatalog.Tab tabForTrigger(int var0) {
        if (isPowderTrigger(var0) || var0 == 3488) {
            return MinerCatalog.Tab.POWDER;
        } else if (isExchangeTrigger(var0) || var0 == 3535) {
            return MinerCatalog.Tab.EXCHANGE;
        } else if (var0 == 3490) {
            return MinerCatalog.Tab.RESIN;
        } else {
            return var0 == 3536 ? MinerCatalog.Tab.ORE : MinerCatalog.Tab.BUY;
        }
    }

    static {
        ArrayList var0 = new ArrayList();

        for (RuneCatalog.RuneDef var2 : RuneCatalog.allRunes()) {
            int var3 = var2.level() + 1;
            var0.add(
                    new MinerCatalog.BuyOffer(
                            var2,
                            3495 + var2.level(),
                            var3,
                            var3 + "_" + var2.id() + "_chronoton"));
        }

        BUYS = List.copyOf(var0);
    }

    public record BuyOffer(
            RuneCatalog.RuneDef rune, int trigger, int resinRequired, String priceHolder) {}

    public record ExchangeOffer(int trigger, List<String> ingredients, String result) {}

    public record PowderOffer(int powder, int esc, int trigger) {}

    public enum Tab {
        BUY,
        POWDER,
        EXCHANGE,
        ACTIONS,
        RESIN,
        ORE;
    }
}
