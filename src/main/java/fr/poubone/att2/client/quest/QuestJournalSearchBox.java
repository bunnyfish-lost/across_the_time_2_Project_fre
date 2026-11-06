package fr.poubone.att2.client.quest;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class QuestJournalSearchBox extends EditBox {
    private static final FontDescription FONT =
            new Resource(Identifier.fromNamespaceAndPath("att2", "journal_search"));
    private static final Style STYLE = Style.EMPTY.withFont(FONT);
    private final Font font;
    private final Component hint;

    public QuestJournalSearchBox(
            Font var1, int var2, int var3, int var4, int var5, Component var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.font = var1;
        this.hint = var6;
        this.setBordered(false);
    }

    private Component styled(String var1) {
        return Component.literal(var1).setStyle(STYLE);
    }

    @Override
    public void renderWidget(GuiGraphics var1, int var2, int var3, float var4) {
        if (this.isVisible()) {
            int var5 = this.getX();
            int var6 = this.getY();
            int var7 = Math.max(1, this.width - 4);
            String var8 = this.getValue();
            int var9 = Math.max(0, Math.min(this.getCursorPosition(), var8.length()));
            if (var8.isEmpty() && !this.isFocused()) {
                MutableComponent var17 = this.hint.copy().setStyle(STYLE);
                var1.enableScissor(var5, var6, var5 + this.width, var6 + this.height);
                var1.drawString(this.font, var17, var5 + 1, var6 + 1, -1771155924, false);
                var1.disableScissor();
            } else {
                int var10 = 0;

                while (var10 < var9
                        && this.font.width(this.styled(var8.substring(var10, var9))) > var7 - 2) {
                    var10 = var8.offsetByCodePoints(var10, 1);
                }

                int var11 = Math.max(var10, var9);

                while (var11 < var8.length()) {
                    int var12 = var8.offsetByCodePoints(var11, 1);
                    if (this.font.width(this.styled(var8.substring(var10, var12))) > var7) {
                        break;
                    }

                    var11 = var12;
                }

                String var18 = var8.substring(var10, var11);
                var1.enableScissor(var5, var6, var5 + this.width, var6 + this.height);
                String var13 = this.getHighlighted();
                if (!var13.isEmpty()) {
                    int var14 = var8.indexOf(var13, var10);
                    if (var14 >= var10 && var14 < var11) {
                        int var15 =
                                var5
                                        + 1
                                        + this.font.width(
                                                this.styled(var8.substring(var10, var14)));
                        int var16 =
                                var5
                                        + 1
                                        + this.font.width(
                                                this.styled(
                                                        var8.substring(
                                                                var10,
                                                                Math.min(
                                                                        var11,
                                                                        var14 + var13.length()))));
                        var1.fill(
                                var15,
                                var6,
                                Math.min(var5 + this.width, var16),
                                var6 + this.height,
                                1715956376);
                    }
                }

                var1.drawString(
                        this.font, this.styled(var18), var5 + 1, var6 + 1, -12966894, false);
                if (this.isFocused() && System.currentTimeMillis() / 500L % 2L == 0L) {
                    int var19 =
                            var5 + 1 + this.font.width(this.styled(var8.substring(var10, var9)));
                    var1.fill(
                            Math.min(var19, var5 + this.width - 1),
                            var6 + 1,
                            Math.min(var19 + 1, var5 + this.width),
                            var6 + 10,
                            -12966894);
                }

                var1.disableScissor();
            }
        }
    }
}
