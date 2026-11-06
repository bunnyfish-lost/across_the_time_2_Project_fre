package fr.poubone.att2.client.quest;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public final class QuestDetailClip1112 {
    public static final int HEADER_INSET = 14;
    public static final int GLYPH_TOP_PADDING = 2;

    private QuestDetailClip1112() {}

    public static void draw(
            GuiGraphics var0,
            Font var1,
            FormattedCharSequence var2,
            int var3,
            int var4,
            int var5,
            boolean var6) {
        var0.drawString(var1, var2, var3, var4 + 2, var5, var6);
    }

    public static int bodyTop(int var0, int var1) {
        return Math.min(var1, var0 + 14);
    }

    public static void enable(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        var0.enableScissor(var1, bodyTop(var2, var4), var3, var4);
    }
}
