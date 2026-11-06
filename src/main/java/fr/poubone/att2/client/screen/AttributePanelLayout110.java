package fr.poubone.att2.client.screen;

public record AttributePanelLayout110(
        int x,
        int y,
        int w,
        int h,
        int rowH,
        int visible,
        int top,
        int bottom,
        boolean compact,
        int nameX,
        int nameW,
        int totalX,
        int totalW,
        int effectX,
        int effectW,
        int buttonX,
        int buttonW,
        int levelX,
        int levelW,
        int costX,
        int costW) {
    public static AttributePanelLayout110 fit(int var0, int var1) {
        byte var2 = 10;
        byte var3 = 10;
        short var4 = 800;
        short var5 = 460;
        byte var6 = 36;
        byte var7 = 9;
        int var8 = var3 + 72 + 28;
        int var9 = var8 + var6 * var7;
        int var10 = var2 + 10;
        short var11 = 142;
        int var12 = var10 + var11 + 6;
        byte var13 = 68;
        int var14 = var12 + var13 + 6;
        byte var15 = 52;
        int var16 = var14 + var15 + 6;
        byte var17 = 104;
        int var18 = var16 + 3 * (var17 + 6);
        byte var19 = 60;
        int var20 = var18 + var19 + 6;
        byte var21 = 90;
        return new AttributePanelLayout110(
                var2, var3, var4, var5, var6, var7, var8, var9, false, var10, var11, var14, var15,
                var16, var17, var20, var21, var12, var13, var18, var19);
    }

    public int nextX() {
        return this.effectX + this.effectW + 6;
    }

    public int gainX() {
        return this.nextX() + this.effectW + 6;
    }

    public int rowY(int var1) {
        return this.top + var1 * this.rowH;
    }

    public int maxOffset() {
        return 0;
    }
}
