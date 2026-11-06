package fr.poubone.att2.client.spell;

public final class SpellWheelLayout {
    public final int cx;
    public final int cy;
    public final int innerCount;
    public final int outerCount;
    public final int innerRadius;
    public final int outerRadius;
    public final int nodeSize;

    public SpellWheelLayout(int var1, int var2, int var3) {
        this.cx = var1 / 2;
        this.cy = var2 / 2 - 12;
        this.outerRadius = Math.max(40, Math.min(136, Math.min((var2 - 48) / 2, (var1 - 44) / 2)));
        this.innerRadius = Math.round(this.outerRadius * 0.61F);
        this.innerCount = Math.min(14, Math.max(0, var3 / 2 - 3));
        this.outerCount = var3 - this.innerCount;
        this.nodeSize =
                Math.max(
                        14,
                        Math.min(
                                26,
                                (int)
                                                (2
                                                        * this.innerRadius
                                                        * Math.sin(
                                                                Math.PI
                                                                        / Math.max(
                                                                                1, this.innerCount))
                                                        / Math.sqrt(2.0))
                                        - 1));
    }

    public SpellWheelLayout.Point point(int var1) {
        boolean var2 = var1 < this.innerCount;
        int var3 = var2 ? this.innerCount : this.outerCount;
        int var4 = var2 ? var1 : var1 - this.innerCount;
        double var5 = (Math.PI * 2) * var4 / var3 - (Math.PI / 2) + (var2 ? 0.0 : Math.PI / var3);
        int var7 = var2 ? this.innerRadius : this.outerRadius;
        return new SpellWheelLayout.Point(
                this.cx + (int) Math.round(Math.cos(var5) * var7),
                this.cy + (int) Math.round(Math.sin(var5) * var7));
    }

    public int exactHit(double var1, double var3) {
        for (int var5 = 0; var5 < this.innerCount + this.outerCount; var5++) {
            SpellWheelLayout.Point var6 = this.point(var5);
            if (Math.abs(var1 - var6.x()) <= this.nodeSize / 2.0 + 1.0
                    && Math.abs(var3 - var6.y()) <= this.nodeSize / 2.0 + 1.0) {
                return var5;
            }
        }

        return -1;
    }

    public int hover(double var1, double var3) {
        double var5 = var1 - this.cx;
        double var7 = var3 - this.cy;
        if (Math.abs(var5) <= this.centerWidth() / 2.0 + 2.0 && Math.abs(var7) <= 28.0) {
            return -1;
        }

        double var9 = Math.hypot(var5, var7);
        if (var9 < this.innerRadius - this.nodeSize * 0.65) {
            return -1;
        }

        boolean var11 = var9 < (this.innerRadius + this.outerRadius) / 2.0;
        int var12 = var11 ? this.innerCount : this.outerCount;
        if (var12 == 0) {
            return -1;
        }

        double var13 = Math.atan2(var7, var5) + (Math.PI / 2) - (var11 ? 0.0 : Math.PI / var12);
        int var15 = Math.floorMod((int) Math.round(var13 / (Math.PI * 2) * var12), var12);
        return var11 ? var15 : this.innerCount + var15;
    }

    public int centerWidth() {
        double var1 = this.innerRadius - Math.sqrt(2.0) * this.nodeSize / 2.0 - 3.0;
        return Math.max(
                42, Math.min(92, (int) (2.0 * Math.sqrt(Math.max(0.0, var1 * var1 - 729.0)))));
    }

    public record Point(int x, int y) {}
}
