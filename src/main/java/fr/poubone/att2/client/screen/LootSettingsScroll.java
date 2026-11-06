package fr.poubone.att2.client.screen;

public final class LootSettingsScroll {
    private int x;
    private int top;
    private int width;
    private int bottom;
    private int contentHeight;
    private double offset;

    public void configure(int var1, int var2, int var3, int var4, int var5) {
        this.x = var1;
        this.top = var2;
        this.width = var3;
        this.bottom = var4;
        this.contentHeight = var5;
        this.offset = Math.max(0.0, Math.min(this.offset, this.maxScroll()));
    }

    public int top() {
        return this.top;
    }

    public int bottom() {
        return this.bottom;
    }

    public int height() {
        return Math.max(1, this.bottom - this.top);
    }

    public int maxScroll() {
        return Math.max(0, this.contentHeight - this.height());
    }

    public int y(int var1) {
        return this.top + var1 - (int) Math.round(this.offset);
    }

    public boolean fullyVisible(int var1, int var2) {
        int var3 = this.y(var1);
        return var3 >= this.top && var3 + var2 <= this.bottom;
    }

    public boolean contains(double var1, double var3) {
        return var1 >= this.x
                && var1 < this.x + this.width
                && var3 >= this.top
                && var3 < this.bottom;
    }

    public void scrollBy(double var1) {
        this.offset = Math.max(0.0, Math.min(this.maxScroll(), this.offset + var1));
    }

    public int barX() {
        return this.x + this.width - 6;
    }

    public boolean overBar(double var1, double var3) {
        return this.maxScroll() > 0
                && var1 >= this.barX() - 2
                && var1 < this.barX() + 6
                && var3 >= this.top
                && var3 < this.bottom;
    }

    public int thumbHeight() {
        return Math.min(
                this.height(),
                Math.max(16, this.height() * this.height() / Math.max(1, this.contentHeight)));
    }

    public int thumbY() {
        return this.top
                + (int)
                        Math.round(
                                this.offset
                                        / Math.max(1, this.maxScroll())
                                        * (this.height() - this.thumbHeight()));
    }

    public void scrollFromPointer(double var1) {
        double var3 = this.height() - this.thumbHeight();
        this.offset =
                var3 <= 0.0
                        ? 0.0
                        : Math.max(
                                        0.0,
                                        Math.min(
                                                1.0,
                                                (var1 - this.top - this.thumbHeight() / 2.0)
                                                        / var3))
                                * this.maxScroll();
    }
}
