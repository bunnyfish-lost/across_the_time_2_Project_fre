package fr.poubone.att2.client.shop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public final class ShopTheme {
    private ShopTheme() {}

    public static void text(
            GuiGraphics graphics,
            Component text,
            int x,
            int y,
            int width,
            float scale,
            int color,
            boolean centered) {
        if (width > 0) {
            Font font = Minecraft.getInstance().font;
            int limit = Math.max(1, (int) (width / scale));
            Component line = text;
            if (font.width(text) > limit) {
                MutableComponent clipped = Component.empty();
                font.substrByWidth(text, Math.max(0, limit - font.width("…")))
                        .visit(
                                (style, part) -> {
                                    clipped.append(Component.literal(part).withStyle(style));
                                    return Optional.empty();
                                },
                                Style.EMPTY);
                line = clipped.append("…");
            }

            float left = centered ? x + (width - font.width(line) * scale) / 2.0F : x;
            graphics.pose().pushMatrix();
            graphics.pose().translate(left, y);
            graphics.pose().scale(scale, scale);
            graphics.drawString(font, line, 0, 0, color, false);
            graphics.pose().popMatrix();
        }
    }
}
