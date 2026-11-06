package fr.poubone.att2.client.quest;

import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class QuestRibbon1130 {
    private QuestRibbon1130() {}

    public static boolean chinese() {
        return "zh".equals(ModLanguageManager.getCurrentLanguage());
    }

    public static String label(QuestType var0) {
        boolean var1 = chinese();
        if (var0 == QuestType.MAIN) {
            return var1 ? "主线任务" : "Récit";
        } else if (var0 == QuestType.SIDE) {
            return var1 ? "支线任务" : "Annexes";
        } else if (var0 == QuestType.DAILY) {
            return var1 ? "日常任务" : "Jour";
        } else {
            return "";
        }
    }

    public static int ribbon(
            GuiGraphics var0,
            Font var1,
            int var2,
            int var3,
            int var4,
            QuestType var5,
            String var6,
            boolean var7,
            boolean var8) {
        String var9 = label(var5);
        if (!chinese()) {
            return QuestJournalArt.ribbon(var0, var1, var2, var3, var4, var5, var9, var7, var8);
        }

        int var10 = QuestJournalArt.ribbon(var0, var1, var2, var3, var4, var5, "", var7, var8);
        int var11 = var1.width(var9);
        float var12 = Math.min(1.0F, (float) Math.max(1, var4 - 6) / Math.max(1, var11));
        var0.pose().pushMatrix();
        var0.pose()
                .translate(
                        var2 + (var4 - var11 * var12) / 2.0F,
                        var3 + 6 + (9.0F - 9.0F * var12) / 2.0F);
        var0.pose().scale(var12, var12);
        var0.drawString(var1, var9, 0, 0, var7 ? -793924 : -1427713366, false);
        var0.pose().popMatrix();
        return var10;
    }
}
