package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.hud.ModToast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

public final class RuneWordCraft119 {
    private static final int RESPONSE_TICKS = 100;
    private static final int QUEUE_TICKS = 200;
    private static RuneWorkshopRecipes119.Recipe active;
    private static int remaining;
    private static int trigger;
    private static int ticks;
    private static int queuedTicks;
    private static boolean dispatched;
    private static boolean confirmed;
    private static boolean everDispatched;
    private static Object world;
    private static int[] before;

    private RuneWordCraft119() {}

    public static boolean busy() {
        return active != null;
    }

    public static String statusLabel() {
        if (active == null) {
            return "发送中…";
        } else if (!dispatched) {
            return confirmed ? "确认中…" : "发送中…";
        } else {
            return confirmed ? "制作中…" : "等待回复…";
        }
    }

    public static boolean start(RuneWorkshopRecipes119.Recipe var0, int var1) {
        Minecraft var2 = Minecraft.getInstance();
        if (busy()
                || var0 == null
                || var0.word() == null
                || var0.mode() != RuneWorkshopRecipes119.Mode.DIRECT
                || var0.word().trigger() <= 0
                || var1 < 1
                || var2 == null
                || var2.player == null
                || var2.player.connection == null
                || var2.level == null
                || FlashbackCompat.isInReplay()) {
            return false;
        }

        if (!hasMaterials(var0, var2.player)) {
            return false;
        }

        active = var0;
        remaining = var1;
        trigger = var0.word().trigger();
        world = var2.level;
        next(var2.player);
        return true;
    }

    private static boolean hasMaterials(RuneWorkshopRecipes119.Recipe var0, LocalPlayer var1) {
        return var0.rows().stream()
                .allMatch(var1x -> var1x.material().owned(var1) >= var1x.needed());
    }

    private static int[] snapshot(LocalPlayer var0) {
        return active.rows().stream().mapToInt(var1 -> var1.material().owned(var0)).toArray();
    }

    private static void next(LocalPlayer var0) {
        before = snapshot(var0);
        everDispatched = false;
        confirmed = false;
        dispatched = false;
        ticks = 0;
        queuedTicks = 200;
        Att2Triggers.sendAlways(trigger);
    }

    public static void onTriggerSent(int var0) {
        if (active != null && var0 == trigger) {
            if (!everDispatched) {
                Minecraft var1 = Minecraft.getInstance();
                if (var1 != null && var1.player != null) {
                    before = snapshot(var1.player);
                }
            }

            dispatched = true;
            everDispatched = true;
            ticks = 100;
            queuedTicks = 0;
        }
    }

    public static boolean onMessage(Component var0, boolean var1) {
        if (active == null || !everDispatched || FlashbackCompat.isInReplay()) {
            return true;
        }

        if (!contains(var0, "commands.trigger.failed.unprimed")
                && !contains(var0, "commands.trigger.failed.invalid")) {
            if (contains(var0, "att2.runes.quick_recipe.sure") || var1 && localizedSure(var0)) {
                if (dispatched && !confirmed) {
                    Minecraft var2 = Minecraft.getInstance();
                    if (var2 == null || var2.player == null || !hasMaterials(active, var2.player)) {
                        fail("材料不足，已停止制作");
                        return true;
                    }

                    confirmed = true;
                    dispatched = false;
                    ticks = 0;
                    queuedTicks = 200;
                    Att2Triggers.sendAlways(trigger);
                }

                return !(Minecraft.getInstance().screen instanceof RuneCodexScreen);
            } else {
                return true;
            }
        } else {
            fail("地图拒绝了制作请求，已停止；请检查材料后重试");
            return true;
        }
    }

    private static boolean contains(Component var0, String var1) {
        if (var0 == null) {
            return false;
        }

        if (var0.getContents() instanceof TranslatableContents var2) {
            if (var1.equals(var2.getKey())) {
                return true;
            }

            for (Object var6 : var2.getArgs()) {
                if (var6 instanceof Component var7 && contains(var7, var1)) {
                    return true;
                }
            }
        }

        for (Component var10 : var0.getSiblings()) {
            if (contains(var10, var1)) {
                return true;
            }
        }

        return false;
    }

    private static boolean localizedSure(Component var0) {
        if (var0 != null && !(var0.getContents() instanceof TranslatableContents)) {
            Language var1 = Language.getInstance();
            String var2 = "att2.runes.quick_recipe.sure";
            if (!var1.has(var2)) {
                return false;
            }

            String var3 = var1.getOrDefault(var2).replaceAll("§.", "").strip();
            String var4 = var0.getString().replaceAll("§.", "").strip();
            return !var3.isEmpty() && var3.equals(var4);
        } else {
            return false;
        }
    }

    public static void tick(Minecraft var0) {
        if (active != null) {
            if (var0 == null
                    || var0.player == null
                    || var0.player.connection == null
                    || var0.level != world
                    || FlashbackCompat.isInReplay()) {
                clear();
            } else if (!Att2Triggers.isGameplayPaused(var0)) {
                if (everDispatched) {
                    boolean var1 = true;
                    List var2 = active.rows();

                    for (int var3 = 0; var3 < var2.size(); var3++) {
                        if (before[var3]
                                        - ((RuneWorkshopRecipes119.Row) var2.get(var3))
                                                .material()
                                                .owned(var0.player)
                                < ((RuneWorkshopRecipes119.Row) var2.get(var3)).needed()) {
                            var1 = false;
                            break;
                        }
                    }

                    if (var1) {
                        if (--remaining <= 0) {
                            clear();
                            return;
                        }

                        if (!hasMaterials(active, var0.player)) {
                            fail("材料不足，已停止后续制作");
                            return;
                        }

                        next(var0.player);
                        return;
                    }
                }

                if (!dispatched) {
                    if (--queuedTicks <= 0) {
                        fail("制作请求等待过久，已取消排队；请检查网络后重试");
                    }
                } else {
                    if (--ticks <= 0) {
                        fail("未收到制作结果，已停止等待；请先检查成品和材料再重试");
                    }
                }
            }
        }
    }

    private static void fail(String var0) {
        clear();
        ModToast.showError(Component.literal(var0));
    }

    public static void clear() {
        if (active != null && !dispatched) {
            Att2Triggers.cancel(trigger);
        }

        active = null;
        world = null;
        queuedTicks = 0;
        ticks = 0;
        trigger = 0;
        remaining = 0;
        everDispatched = false;
        confirmed = false;
        dispatched = false;
        before = null;
    }
}
