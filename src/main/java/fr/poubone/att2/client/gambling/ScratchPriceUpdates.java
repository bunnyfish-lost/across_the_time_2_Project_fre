package fr.poubone.att2.client.gambling;

public final class ScratchPriceUpdates {
    private static volatile long revision;

    private ScratchPriceUpdates() {}

    public static void received() {
        revision++;
    }

    public static long revision() {
        return revision;
    }
}
