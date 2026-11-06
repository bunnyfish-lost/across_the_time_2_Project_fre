package fr.poubone.att2.client.screen;

final class HUDConfigLayout {
    private HUDConfigLayout() {}

    static int tabHeight(int var0, int var1) {
        return Math.max(14, Math.min(20, (var0 - 60) / Math.max(1, var1)));
    }

    static int tabStep(int var0, int var1) {
        int var2 = tabHeight(var0, var1);
        return Math.max(var2, Math.min(24, (var0 - 60 - var2) / Math.max(1, var1 - 1)));
    }

    static int tabTop(int var0, int var1) {
        return Math.max(
                28,
                Math.min(
                        36,
                        var0 - 32 - ((var1 - 1) * tabStep(var0, var1) + tabHeight(var0, var1))));
    }
}
