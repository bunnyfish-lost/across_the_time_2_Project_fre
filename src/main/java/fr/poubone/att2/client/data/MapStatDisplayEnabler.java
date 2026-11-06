package fr.poubone.att2.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;

public final class MapStatDisplayEnabler {
    public static final int ALL_RUN = 556;
    public static final int COUNT = 558;
    public static final int VALUE = 559;
    public static final String ATTRIBUTE_DISPLAY_KEY = "consciousness.attribute_display";
    private static final int WARMUP_TICKS = 40;
    private static final int LISTEN_TICKS = 80;
    private static int warmup = 40;
    private static int requestedTrigger = -1;
    private static boolean listening;
    private static int listenTicks;

    private MapStatDisplayEnabler() {}

    public static boolean shouldRequest(boolean hudOn, boolean showStats, boolean hasPointTotals) {
        return false;
    }

    public static int triggerToSend(
            boolean hudOn, boolean showStats, boolean hasPointTotals, boolean hasDetailedValues) {
        return -1;
    }

    public static int triggerForDisplay(
            boolean hudOn,
            boolean showStats,
            boolean hasPointTotals,
            boolean hasDetailedValues,
            boolean detailedMode) {
        return -1;
    }

    public static int triggerToSend(
            boolean hudOn,
            boolean showStats,
            boolean hasPointTotals,
            boolean hasDetailedValues,
            boolean mapReady) {
        return -1;
    }

    public static void tick(Minecraft client) {}

    public static void onSentDisplayTrigger() {}

    public static boolean applyIfListening(Component title, Component body) {
        return false;
    }

    public static boolean onDialog(Holder<Dialog> holder) {
        return false;
    }

    static boolean isAttributeDisplay(Component title, Component body) {
        return DialogComponents.hasKeyOrChild(title, "consciousness.attribute_display")
                || DialogComponents.hasKeyOrChild(body, "consciousness.attribute_display");
    }

    public static void reset() {
        warmup = 40;
        requestedTrigger = -1;
        listening = false;
        listenTicks = 0;
    }

    private static void decayListenWindow() {
        if (listenTicks > 0) {
            listenTicks--;
            if (listenTicks == 0) {
                listening = false;
            }
        }
    }

    private static CommonDialogData commonOf(Dialog dialog) {
        if (dialog instanceof MultiActionDialog multi) {
            return multi.common();
        } else {
            return dialog instanceof NoticeDialog notice ? notice.common() : null;
        }
    }

    private static Component bodyOf(CommonDialogData common) {
        Component body = Component.empty();

        for (DialogBody part : common.body()) {
            if (part instanceof PlainMessage message) {
                body = body.copy().append(message.contents());
            }
        }

        return body;
    }
}
