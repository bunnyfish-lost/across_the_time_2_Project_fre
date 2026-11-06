package fr.poubone.att2.client.data;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.quest.QuestModel;
import fr.poubone.att2.client.rune.RunePouchSync1115;
import fr.poubone.att2.client.rune.RuneWordCraft119;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayDeque;
import java.util.Map;

public final class Att2Triggers {
    public static final String OBJECTIVE = "ScoreTrigger";
    public static final int REPAIR_HELMET = 416;
    public static final int REPAIR_CHESTPLATE = 417;
    public static final int REPAIR_LEGGINGS = 418;
    public static final int REPAIR_BOOTS = 419;
    public static final int REPAIR_OFFHAND = 420;
    public static final int REPAIR_MAINHAND = 421;
    public static final int REPAIR_ALL = 422;
    public static final int TOOLS_NUMBER_INFO = 3607;
    public static final int RUNE_BUNDLE_INFO = 3608;
    public static final int MAIN_QUEST_GO = 1068;
    public static final int CONSCIOUSNESS_SIDEQUEST_LIST = 2315;
    public static final int CONSCIOUSNESS_DAILYQUEST_LIST = 3280;
    public static final int CONSCIOUSNESS_CLEAR = 2310;
    public static final int HORSE_WHISTLE = 1070;
    public static final int COLLECT_ITEMS = 1072;
    public static final int CONSCIOUSNESS_ATTRIBUTE = 2313;
    public static final int CONSCIOUSNESS_CURRENCY = 2312;
    public static final int HUD_SCORE_SYNC = 3610;
    public static final int PLAYER_GLOW = 1085;
    public static final int STAT_DISPLAY_ALL_RUN = 556;
    public static final int STAT_DISPLAY_ALL_STOP = 557;
    public static final int STAT_DISPLAY_COUNT = 558;
    public static final Map<String, Integer> STAT_UPGRADE =
            Map.of(
                    "STR", 1077, "RES", 1078, "HAS", 1079, "SPD", 1080, "HER", 1081, "DAR", 1082,
                    "LUC", 1083, "HUN", 1084, "CRT", 3529);
    private static final ArrayDeque<Integer> QUEUE = new ArrayDeque<>();
    private static final int SEND_INTERVAL_TICKS = 2;
    private static int cooldown = 0;
    private static final int HUD_SYNC_READY_DELAY_TICKS = 40;
    private static int hudSyncReadyTicks;
    private static boolean hudSyncRequested;
    public static final int STAT_DISPLAY_VALUE = 559;

    private Att2Triggers() {}

    public static String command(int var0) {
        return "trigger ScoreTrigger set " + var0;
    }

    public static void send(int var0) {
        if (!FlashbackCompat.isInReplay() && !QUEUE.contains(var0)) {
            QUEUE.addLast(var0);
        }
    }

    public static void cancel(int var0) {
        QUEUE.removeFirstOccurrence(var0);
    }

    public static void sendAlways(int var0) {
        if (!FlashbackCompat.isInReplay()) {
            QUEUE.addLast(var0);
        }
    }

    public static boolean isGameplayPaused(Minecraft var0) {
        return var0.isPaused() || var0.screen != null && var0.screen.isPauseScreen();
    }

    public static void tick(Minecraft var0) {
        if (FlashbackCompat.isInReplay()) {
            reset();
        } else if (!isGameplayPaused(var0)) {
            LocalPlayer var1 = var0.player;
            if (var1 != null && var1.connection != null && var0.level != null) {
                if (!hudSyncRequested) {
                    if (!MapReady.isReady() && !hasMapDisplay(var0)) {
                        hudSyncReadyTicks = 0;
                    } else if (++hudSyncReadyTicks >= 40) {
                        send(3610);
                        hudSyncRequested = true;
                    }
                }

                if (cooldown > 0) {
                    cooldown--;
                } else {
                    Integer var2 = QUEUE.pollFirst();
                    if (var2 != null) {
                        cooldown = 2;
                        RepairDialogSuppressor.onTriggerSent(var2);
                        QuestModel.get().onTriggerSent(var2);
                        RuneWordCraft119.onTriggerSent(var2);
                        RunePouchSync1115.onTriggerSent(var2);
                        var1.connection.sendCommand(command(var2));
                    }
                }
            } else {
                QUEUE.clear();
                QuestModel.get().cancelDetailRequests();
            }
        }
    }

    private static boolean hasMapDisplay(Minecraft var0) {
        if (var0.level == null) {
            return false;
        }

        Scoreboard var1 = var0.level.getScoreboard();
        Objective var2 = var1.getDisplayObjective(DisplaySlot.LIST);
        if (var2 != null && "TEMPERATURE".equals(var2.getName())) {
            return true;
        }

        Objective var3 = var1.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (var3 == null) {
            return false;
        }

        return switch (var3.getName()) {
            case "Sidebar_Display", "Sidebar_Info", "DAHAL" -> true;
            default -> false;
        };
    }

    public static void reset() {
        RunePouchSync1115.onQueueReset();
        QUEUE.clear();
        QuestModel.get().cancelDetailRequests();
        RepairDialogSuppressor.reset();
        cooldown = 0;
        hudSyncReadyTicks = 0;
        hudSyncRequested = false;
    }

    public static int parseTriggerCommand(String var0) {
        if (var0 == null) {
            return -1;
        }

        String var1 = var0.strip();
        if (var1.startsWith("/")) {
            var1 = var1.substring(1).stripLeading();
        }

        String[] var2 = var1.split("\\s+");
        if (var2.length != 4) {
            return -1;
        }

        if (!var2[0].equalsIgnoreCase("trigger")
                && !var2[0].equalsIgnoreCase("minecraft:trigger")) {
            return -1;
        }

        if (var2[1].equals("ScoreTrigger") && var2[2].equalsIgnoreCase("set")) {
            try {
                return Integer.parseInt(var2[3]);
            } catch (NumberFormatException var4) {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
