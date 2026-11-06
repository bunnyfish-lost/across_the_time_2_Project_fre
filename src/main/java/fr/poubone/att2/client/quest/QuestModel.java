package fr.poubone.att2.client.quest;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestModel {
    private static final QuestModel INSTANCE = new QuestModel();
    private static final String SIDEQUEST_TITLE_KEY = "consciousness.sidequest.title";
    private static final Pattern SIDEQUEST_NAME_KEY =
            Pattern.compile("consciousness\\.sidequest\\.(\\d+)");
    private static final String MAINQUEST_PROGRESS_KEY = "att2.mainquest.progress";
    private static final String DAILYQUEST_LIST_TITLE_KEY = "consciousness.dailyquest.title";
    private static final Pattern DAILYQUEST_CITY_KEY =
            Pattern.compile("consciousness\\.dailyquest\\.list\\.([a-z_]+)");
    private static final Pattern DAILYQUEST_CITY_TITLE_KEY =
            Pattern.compile("consciousness\\.dailyquest\\.([a-z_]+)\\.title");
    private static final Pattern DAILYQUEST_NAME_KEY =
            Pattern.compile("att2\\.dailyquest\\.([a-z_]+)\\.(\\d+)\\.name");
    private static final String DAILYQUEST_REMAINING_KEY = "att2.dailyquest.remaining_time";
    private static final int CITY_REQUEST_INTERVAL_TICKS = 8;
    private static final int MAIN_QUEST_MAX_STEP = 300;
    private static final int DIALOG_TIMEOUT_TICKS = 80;
    private static final int CAPTURE_TICKS = 100;
    private static final int RESPONSE_QUIET_TICKS = 10;
    private final List<QuestInfo> sideQuests = new ArrayList<>();
    private final Map<String, List<QuestInfo>> dailyQuests = new LinkedHashMap<>();
    private final Map<String, List<Component>> descriptions = new HashMap<>();
    private int sideCompleted = 0;
    private int sideTotal = 60;
    private int mainStep = -1;
    private long lastSideQuestUpdate = 0L;
    private int dialogWaitTicks = 0;
    private int dailyDialogWaitTicks = 0;
    private final ArrayDeque<Integer> pendingCityTriggers = new ArrayDeque<>();
    private int cityRequestCooldown = 0;
    private boolean hasDailyData = false;
    private int captureTicks = 0;
    private String captureKey = null;
    private final List<Component> captureBuffer = new ArrayList<>();
    private String selectedKey = null;
    private QuestInfo captureQuest;
    private boolean captureDispatched;
    private boolean captureChatVisible;
    private int captureQuietTicks = -1;
    private QuestInfo pendingDetails;
    private boolean pendingChatVisible;

    private QuestModel() {}

    public static QuestModel get() {
        return INSTANCE;
    }

    public void reset() {
        this.sideQuests.clear();
        this.dailyQuests.clear();
        this.descriptions.clear();
        this.sideCompleted = 0;
        this.mainStep = -1;
        this.lastSideQuestUpdate = 0L;
        this.dialogWaitTicks = 0;
        this.dailyDialogWaitTicks = 0;
        this.pendingCityTriggers.clear();
        this.cityRequestCooldown = 0;
        this.hasDailyData = false;
        this.captureTicks = 0;
        this.captureKey = null;
        this.captureBuffer.clear();
        this.selectedKey = null;
        this.cancelDetailRequests();
    }

    public void requestSideQuests() {
        this.descriptions.keySet().removeIf(var0 -> var0.startsWith("SIDE:"));
        this.dialogWaitTicks = 80;
        Att2Triggers.send(2315);
    }

    public void requestDailyQuests() {
        this.descriptions.keySet().removeIf(var0 -> var0.startsWith("DAILY:"));
        this.dailyDialogWaitTicks = 80;
        this.pendingCityTriggers.clear();
        Att2Triggers.send(3280);
    }

    public void requestDetails(QuestInfo var1) {
        this.queueDetails(var1, false, false);
    }

    public void requestTracking(QuestInfo var1) {
        if (!HUDConfig.get().questMenuEnabled) {
            this.cancelDetailRequests();
            if (var1 != null && var1.hasDetails()) {
                this.descriptions.remove(var1.key());
                Att2Triggers.send(var1.progressTrigger());
            }
        } else {
            this.queueDetails(var1, true, true);
        }
    }

    private void queueDetails(QuestInfo var1, boolean var2, boolean var3) {
        if (var1 != null && var1.hasDetails() && !FlashbackCompat.isInReplay()) {
            if (this.captureQuest != null && !this.captureDispatched) {
                if (this.captureQuest.key().equals(var1.key())) {
                    this.captureChatVisible |= var2;
                    this.pendingDetails = null;
                    return;
                }

                Att2Triggers.cancel(this.captureQuest.progressTrigger());
                this.clearCapture();
            }

            if (this.captureQuest != null
                    && this.captureQuest.key().equals(var1.key())
                    && !var3
                    && !var2
                    && !this.captureChatVisible) {
                this.pendingDetails = null;
            } else {
                this.pendingDetails = var1;
                this.pendingChatVisible = var2;
                this.descriptions.remove(var1.key());
                this.startPendingDetails();
            }
        }
    }

    private void startPendingDetails() {
        if (this.captureQuest == null && this.pendingDetails != null) {
            QuestInfo var1 = this.findByKey(this.pendingDetails.key());
            if (var1 == null) {
                var1 = this.pendingDetails;
            }

            this.captureQuest = var1;
            this.captureKey = var1.key();
            this.captureChatVisible = this.pendingChatVisible;
            this.captureDispatched = false;
            this.captureTicks = 0;
            this.captureQuietTicks = -1;
            this.captureBuffer.clear();
            this.descriptions.remove(this.captureKey);
            this.pendingDetails = null;
            Att2Triggers.send(var1.progressTrigger());
        }
    }

    public void onTriggerSent(int var1) {
        if (this.captureQuest != null
                && !this.captureDispatched
                && var1 == this.captureQuest.progressTrigger()) {
            this.captureDispatched = true;
            this.captureTicks = 100;
        }
    }

    public void refreshSelectedDetails() {
        QuestInfo var1 = this.findByKey(this.selectedKey);
        if (var1 == null) {
            QuestInfo var2 = QuestTracking.pinned();
            if (var2 != null && var2.key().equals(this.selectedKey)) {
                var1 = var2;
            }
        }

        this.queueDetails(var1, false, true);
    }

    public void onBookClosed() {
        if (this.pendingDetails != null && !this.pendingChatVisible) {
            this.pendingDetails = null;
        }

        if (this.captureQuest != null && !this.captureChatVisible && !this.captureDispatched) {
            Att2Triggers.cancel(this.captureQuest.progressTrigger());
            this.clearCapture();
        }
    }

    public void cancelDetailRequests() {
        if (this.captureQuest != null && !this.captureDispatched) {
            Att2Triggers.cancel(this.captureQuest.progressTrigger());
        }

        this.pendingDetails = null;
        this.clearCapture();
    }

    private void clearCapture() {
        this.captureQuest = null;
        this.captureKey = null;
        this.captureDispatched = false;
        this.captureChatVisible = false;
        this.captureTicks = 0;
        this.captureQuietTicks = -1;
        this.captureBuffer.clear();
    }

    public void tick(Minecraft var1) {
        if (!Att2Triggers.isGameplayPaused(var1)) {
            if (this.dialogWaitTicks > 0) {
                this.dialogWaitTicks--;
            }

            if (this.dailyDialogWaitTicks > 0) {
                this.dailyDialogWaitTicks--;
            }

            if (this.cityRequestCooldown > 0) {
                this.cityRequestCooldown--;
            }

            if (this.cityRequestCooldown == 0
                    && !this.pendingCityTriggers.isEmpty()
                    && this.dailyDialogWaitTicks == 0) {
                this.dailyDialogWaitTicks = 80;
                this.cityRequestCooldown = 8;
                Att2Triggers.send(this.pendingCityTriggers.pollFirst());
            }

            if (this.captureDispatched && this.captureTicks > 0) {
                this.captureTicks--;
                if (this.captureQuietTicks > 0) {
                    this.captureQuietTicks--;
                }

                if (this.captureTicks == 0 || this.captureQuietTicks == 0) {
                    this.finishCapture();
                }
            }

            this.startPendingDetails();
        }
    }

    private void finishCapture() {
        if (this.captureKey != null) {
            this.descriptions.put(this.captureKey, List.copyOf(this.captureBuffer));
        }

        this.clearCapture();
    }

    public boolean onDialog(Holder<Dialog> var1) {
        if (!HUDConfig.get().questMenuEnabled) {
            return false;
        } else {
            Dialog var2 = (Dialog) var1.value();
            boolean var3 = this.dialogWaitTicks > 0;
            if (var2 instanceof NoticeDialog var4
                    && containsKey(var4.common().title(), "consciousness.sidequest.title")) {
                this.sideQuests.clear();
                this.sideCompleted = 0;
                this.lastSideQuestUpdate = System.currentTimeMillis();
                this.dialogWaitTicks = 0;
                return var3;
            } else if (var2 instanceof MultiActionDialog var5) {
                if (bodyContainsKey(var5.common(), "consciousness.sidequest.title")) {
                    this.parseSideQuestDialog(var5);
                    this.dialogWaitTicks = 0;
                    return var3;
                } else {
                    boolean var6 = this.dailyDialogWaitTicks > 0;
                    if (bodyContainsKey(var5.common(), "consciousness.dailyquest.title")) {
                        this.parseDailyCityList(var5);
                        this.dailyDialogWaitTicks = 0;
                        return var6;
                    } else {
                        String var7 = findDailyCityTitle(var5.common());
                        if (var7 != null) {
                            this.parseDailyCityDialog(var7, var5);
                            this.dailyDialogWaitTicks = 0;
                            return var6;
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        }
    }

    private void parseDailyCityList(MultiActionDialog var1) {
        this.pendingCityTriggers.clear();
        ArrayList<String> var2 = new ArrayList();

        for (ActionButton var4 : var1.actions()) {
            String var5 = findKeyGroup(var4.button().label(), DAILYQUEST_CITY_KEY, 1);
            int var6 = triggerOf(var4);
            if (var5 != null && !var5.equals("error") && var6 >= 0) {
                var2.add(var5);
                this.pendingCityTriggers.addLast(var6);
            }
        }

        this.dailyQuests.keySet().retainAll(var2);

        for (String var8 : var2) {
            this.dailyQuests.putIfAbsent(var8, new ArrayList<>());
        }

        this.hasDailyData = true;
        this.cityRequestCooldown = 0;
    }

    private void parseDailyCityDialog(String var1, MultiActionDialog var2) {
        ArrayList<QuestInfo> var3 = new ArrayList();

        for (ActionButton var5 : var2.actions()) {
            Component var6 = var5.button().label();
            String var7 = findKeyGroup(var6, DAILYQUEST_NAME_KEY, 1);
            String var8 = findKeyGroup(var6, DAILYQUEST_NAME_KEY, 2);
            if (var7 != null && var8 != null) {
                int var9 = Integer.parseInt(var8);
                QuestStatus var10 = QuestStatus.fromColor(findColor(var6));
                int var11 = -1;
                if (var5.button().tooltip().isPresent()) {
                    TranslatableContents var12 =
                            findTranslatable(
                                    var5.button().tooltip().get(),
                                    "att2.dailyquest.remaining_time");
                    if (var12 != null && var12.getArgs().length >= 2) {
                        int var13 = argToInt(var12.getArgs()[0]).orElse(0);
                        int var14 = argToInt(var12.getArgs()[1]).orElse(0);
                        var11 = var13 * 60 + var14;
                    }
                }

                MutableComponent var15 =
                        Component.translatable("att2.dailyquest." + var7 + "." + var9 + ".name");
                var3.add(
                        new QuestInfo(
                                QuestType.DAILY, var9, var15, var10, triggerOf(var5), var7, var11));
            }
        }

        var3.sort((var0, var1x) -> Integer.compare(var0.number(), var1x.number()));
        this.dailyQuests.put(var1, var3);
        this.hasDailyData = true;
    }

    private static int triggerOf(ActionButton var0) {
        if (var0.action().isPresent()
                && var0.action().get() instanceof StaticAction var1
                && var1.value() instanceof RunCommand var2) {
            int var5 = Att2Triggers.parseTriggerCommand(var2.command());
            return var5 == 2310 ? -1 : var5;
        } else {
            return -1;
        }
    }

    private static String findDailyCityTitle(CommonDialogData var0) {
        String var1 = findKeyGroup(var0.title(), DAILYQUEST_CITY_TITLE_KEY, 1);
        if (var1 != null) {
            return var1;
        }

        for (DialogBody var3 : var0.body()) {
            if (var3 instanceof PlainMessage var4) {
                String var5 = findKeyGroup(var4.contents(), DAILYQUEST_CITY_TITLE_KEY, 1);
                if (var5 != null) {
                    return var5;
                }
            }
        }

        return null;
    }

    private static String findKeyGroup(Component var0, Pattern var1, int var2) {
        String[] var3 = new String[] {null};
        visit(
                var0,
                var3x -> {
                    if (var3[0] == null
                            && var3x.getContents() instanceof TranslatableContents var4) {
                        Matcher var6 = var1.matcher(var4.getKey());
                        if (var6.matches()) {
                            var3[0] = var6.group(var2);
                        }
                    }
                });
        return var3[0];
    }

    private void parseSideQuestDialog(MultiActionDialog var1) {
        ArrayList<QuestInfo> var2 = new ArrayList();

        for (ActionButton var4 : var1.actions()) {
            Component var5 = var4.button().label();
            int var6 = findSideQuestNumber(var5);
            if (var6 >= 0) {
                QuestStatus var7 = QuestStatus.fromColor(findColor(var5));
                int var8 = -1;
                if (var4.action().isPresent()
                        && var4.action().get() instanceof StaticAction var9
                        && var9.value() instanceof RunCommand var10) {
                    var8 = Att2Triggers.parseTriggerCommand(var10.command());
                    if (var8 == 2310) {
                        var8 = -1;
                    }
                }

                MutableComponent var12 = Component.translatable("consciousness.sidequest." + var6);
                var2.add(new QuestInfo(QuestType.SIDE, var6, var12, var7, var8));
            }
        }

        var2.sort((var0, var1x) -> Integer.compare(var0.number(), var1x.number()));
        this.sideQuests.clear();
        this.sideQuests.addAll(var2);
        this.sideCompleted =
                (int) var2.stream().filter(var0 -> var0.status() == QuestStatus.COMPLETED).count();
        this.readTitleCounts(var1.common());
        this.lastSideQuestUpdate = System.currentTimeMillis();
    }

    private void readTitleCounts(CommonDialogData var1) {
        for (DialogBody var3 : var1.body()) {
            if (var3 instanceof PlainMessage var4) {
                TranslatableContents var5 =
                        findTranslatable(var4.contents(), "consciousness.sidequest.title");
                if (var5 != null && var5.getArgs().length >= 2) {
                    Optional<Integer> var6 = argToInt(var5.getArgs()[0]);
                    Optional<Integer> var7 = argToInt(var5.getArgs()[1]);
                    var6.ifPresent(var1x -> this.sideCompleted = var1x);
                    var7.ifPresent(var1x -> this.sideTotal = var1x);
                }
            }
        }
    }

    public boolean onSystemMessage(Component var1, boolean var2) {
        if (HUDConfig.get().questMenuEnabled && !var2) {
            TranslatableContents var3 = findTranslatable(var1, "att2.mainquest.progress");
            if (var3 != null && var3.getArgs().length > 0) {
                argToInt(var3.getArgs()[0]).ifPresent(this::updateMainStep);
            }

            if (this.captureQuest != null && this.captureDispatched && this.captureTicks > 0) {
                boolean var4 = var3 != null && this.captureQuest.isMain();
                if (!var4 && !this.isGuideMessage(var1)) {
                    return true;
                }

                if (!var4 && this.captureBuffer.stream().noneMatch(var1::equals)) {
                    this.captureBuffer.add(var1.copy());
                }

                this.captureQuietTicks = 10;
                return this.captureChatVisible;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void updateMainStep(int var1) {
        if (this.mainStep != var1) {
            this.mainStep = var1;
            this.descriptions.remove("MAIN:main");
            if (this.captureQuest != null && this.captureQuest.isMain()) {
                this.captureBuffer.clear();
            }
        }
    }

    private boolean isGuideMessage(Component var1) {
        String var2 = var1.getString().strip();
        if (this.captureQuest.isMain()) {
            return var2.startsWith("_.-*°")
                    || var2.startsWith("-<°>-")
                    || hasColor(var1, 11141120) && hasColor(var1, 5636095);
        } else if (this.captureQuest.isDaily()) {
            String var3 = this.captureQuest.city();
            String var4 = "att2.dailyquest." + var3 + ".";
            boolean[] var5 = new boolean[] {false};
            visit(
                    var1,
                    var3x -> {
                        if (var3x.getContents() instanceof TranslatableContents var4x) {
                            String var6 = var4x.getKey();
                            if (var6.equals(var4 + "name")
                                    || var6.startsWith(var4 + this.captureQuest.number() + ".")
                                    || var6.matches("att2[.]dailyquest[.][a-z_]+[.]progress")) {
                                var5[0] = true;
                            }
                        }
                    });
            return var5[0];
        } else {
            return var2.startsWith("-°-") || var2.equals("[???]") || var2.startsWith("[(<!>)]");
        }
    }

    private static boolean hasColor(Component var0, int var1) {
        boolean[] var2 = new boolean[] {false};
        visit(
                var0,
                var2x -> {
                    TextColor var3 = var2x.getStyle().getColor();
                    if (var3 != null && var3.getValue() == var1) {
                        var2[0] = true;
                    }
                });
        return var2[0];
    }

    public List<QuestInfo> getSideQuests() {
        return Collections.unmodifiableList(this.sideQuests);
    }

    public boolean hasSideQuestData() {
        return this.lastSideQuestUpdate > 0L;
    }

    public List<QuestInfo> getDailyQuests() {
        ArrayList var1 = new ArrayList();
        this.dailyQuests.values().forEach(var1::addAll);
        return var1;
    }

    public boolean hasDailyQuestData() {
        return this.hasDailyData;
    }

    public boolean isWaitingForDailyQuests() {
        return this.dailyDialogWaitTicks > 0 || !this.pendingCityTriggers.isEmpty();
    }

    public boolean isWaitingForSideQuests() {
        return this.dialogWaitTicks > 0;
    }

    public boolean isCapturing() {
        return this.captureQuest != null || this.pendingDetails != null;
    }

    public int getSideCompleted() {
        return this.sideCompleted;
    }

    public int getSideTotal() {
        return this.sideTotal;
    }

    public int getMainStep() {
        return this.mainStep;
    }

    public int getMainMaxStep() {
        return 300;
    }

    public QuestInfo getMainQuest() {
        MutableComponent var1 = ModLanguageManager.get("quest_book.main_quest");
        return new QuestInfo(
                QuestType.MAIN,
                Math.max(this.mainStep, 0),
                var1,
                this.mainStep >= 300 ? QuestStatus.COMPLETED : QuestStatus.STARTED,
                1068);
    }

    public List<Component> getDescription(QuestInfo var1) {
        if (var1 == null) {
            return List.of();
        } else if (this.pendingDetails != null && this.pendingDetails.key().equals(var1.key())) {
            return List.of();
        } else {
            return var1.key().equals(this.captureKey)
                    ? Collections.unmodifiableList(this.captureBuffer)
                    : this.descriptions.getOrDefault(var1.key(), List.of());
        }
    }

    public String getSelectedKey() {
        return this.selectedKey;
    }

    public void setSelected(QuestInfo var1) {
        this.selectedKey = var1 == null ? null : var1.key();
    }

    public QuestInfo findByKey(String var1) {
        if (var1 == null) {
            return null;
        }

        if (var1.equals(this.getMainQuest().key())) {
            return this.getMainQuest();
        }

        QuestInfo var2 =
                this.sideQuests.stream()
                        .filter(var1x -> var1x.key().equals(var1))
                        .findFirst()
                        .orElse(null);
        return var2 != null
                ? var2
                : this.getDailyQuests().stream()
                        .filter(var1x -> var1x.key().equals(var1))
                        .findFirst()
                        .orElse(null);
    }

    private static int findSideQuestNumber(Component var0) {
        int[] var1 = new int[] {-1};
        visit(
                var0,
                var1x -> {
                    if (var1[0] < 0 && var1x.getContents() instanceof TranslatableContents var2) {
                        Matcher var4 = SIDEQUEST_NAME_KEY.matcher(var2.getKey());
                        if (var4.matches()) {
                            var1[0] = Integer.parseInt(var4.group(1));
                        }
                    }
                });
        return var1[0];
    }

    private static TextColor findColor(Component var0) {
        TextColor[] var1 = new TextColor[] {null};
        visit(
                var0,
                var1x -> {
                    if (var1[0] == null && var1x.getStyle().getColor() != null) {
                        var1[0] = var1x.getStyle().getColor();
                    }
                });
        return var1[0];
    }

    private static boolean bodyContainsKey(CommonDialogData var0, String var1) {
        if (containsKey(var0.title(), var1)) {
            return true;
        }

        for (DialogBody var3 : var0.body()) {
            if (var3 instanceof PlainMessage var4 && containsKey(var4.contents(), var1)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsKey(Component var0, String var1) {
        return findTranslatable(var0, var1) != null;
    }

    private static TranslatableContents findTranslatable(Component var0, String var1) {
        TranslatableContents[] var2 = new TranslatableContents[] {null};
        visit(
                var0,
                var2x -> {
                    if (var2[0] == null
                            && var2x.getContents() instanceof TranslatableContents var3
                            && var3.getKey().equals(var1)) {
                        var2[0] = var3;
                    }
                });
        return var2[0];
    }

    private static void visit(Component var0, Consumer<Component> var1) {
        if (var0 != null) {
            var1.accept(var0);
            if (var0.getContents() instanceof TranslatableContents var2) {
                for (Object var6 : var2.getArgs()) {
                    if (var6 instanceof Component var7) {
                        visit(var7, var1);
                    }
                }
            }

            for (Component var10 : var0.getSiblings()) {
                visit(var10, var1);
            }
        }
    }

    private static Optional<Integer> argToInt(Object var0) {
        String var1 = var0 instanceof Component var2 ? var2.getString() : String.valueOf(var0);

        try {
            return Optional.of(Integer.parseInt(var1.trim()));
        } catch (NumberFormatException var3) {
            return Optional.empty();
        }
    }
}
