package fr.poubone.att2.client.quest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class QuestPaper111 {
    private static final Identifier LEFT =
            Identifier.fromNamespaceAndPath("att2", "textures/quest_book/parchment_left.png");
    private static final Identifier RIGHT =
            Identifier.fromNamespaceAndPath("att2", "textures/quest_book/parchment_right.png");

    private QuestPaper111() {}

    public static void page(
            GuiGraphics var0, int var1, int var2, int var3, int var4, boolean var5) {
        int[] var6 = new int[] {var1, var1 + 8, var1 + var3 - 8};
        int[] var7 = new int[] {var2, var2 + 8, var2 + var4 - 8};
        int[] var8 = new int[] {8, var3 - 16, 8};
        int[] var9 = new int[] {8, var4 - 16, 8};
        int[] var10 = new int[] {0, 16, 276};
        int[] var11 = new int[] {0, 16, 344};
        int[] var12 = new int[] {16, 260, 16};
        int[] var13 = new int[] {16, 328, 16};

        for (int var14 = 0; var14 < 3; var14++) {
            for (int var15 = 0; var15 < 3; var15++) {
                if (var8[var15] > 0 && var9[var14] > 0) {
                    var0.blit(
                            RenderPipelines.GUI_TEXTURED,
                            var5 ? LEFT : RIGHT,
                            var6[var15],
                            var7[var14],
                            var10[var15],
                            var11[var14],
                            var8[var15],
                            var9[var14],
                            var12[var15],
                            var13[var14],
                            292,
                            360);
                }
            }
        }
    }
}
