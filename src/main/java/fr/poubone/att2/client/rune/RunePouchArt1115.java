package fr.poubone.att2.client.rune;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class RunePouchArt1115 {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("att2", "textures/gui/rune_pouch_fixed.png");

    private RunePouchArt1115() {}

    public static void render(GuiGraphics var0, int var1, int var2, int var3) {
        var0.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE, var1, var2, 0.0F, 0.0F, var3, var3, 16, 16);
    }
}
