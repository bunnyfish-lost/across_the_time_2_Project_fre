package fr.poubone.att2.client.shop;

/** Coordinates in Minecraft GUI units: honor GUI scale, then shrink to fit small windows. */
public record ShopViewport(float scale, float left, float top) {
    public static final int MIN_SIZE_PERCENT = 50;
    public static final int MAX_SIZE_PERCENT = 150;
    public static final int DEFAULT_SIZE_PERCENT = 100;

    public static int clampSizePercent(int percent) {
        return Math.max(MIN_SIZE_PERCENT, Math.min(MAX_SIZE_PERCENT, percent));
    }

    public static ShopViewport fit(
            int screenWidth, int screenHeight, int panelWidth, int panelHeight) {
        return fit(screenWidth, screenHeight, panelWidth, panelHeight, DEFAULT_SIZE_PERCENT);
    }

    /**
     * {@code sizePercent} scales the panel's preferred size; it still shrinks to fit the screen.
     */
    public static ShopViewport fit(
            int screenWidth, int screenHeight, int panelWidth, int panelHeight, int sizePercent) {
        float preferred = clampSizePercent(sizePercent) / 100f;
        float scale =
                Math.max(
                        0.01f,
                        Math.min(
                                preferred,
                                Math.min(
                                        (screenWidth - 12f) / panelWidth,
                                        (screenHeight - 12f) / panelHeight)));
        return new ShopViewport(
                scale,
                (screenWidth - panelWidth * scale) / 2f,
                (screenHeight - panelHeight * scale) / 2f);
    }

    /**
     * Fits a shop either in GUI units (following Minecraft's GUI scale) or in physical pixels
     * (keeping its apparent size stable while the GUI scale changes).
     */
    public static ShopViewport fit(
            int screenWidth,
            int screenHeight,
            int panelWidth,
            int panelHeight,
            int sizePercent,
            double guiScale,
            boolean followGuiScale) {
        if (followGuiScale) {
            return fit(screenWidth, screenHeight, panelWidth, panelHeight, sizePercent);
        }
        float gui = (float) Math.max(0.5, guiScale);
        float preferred = clampSizePercent(sizePercent) / 100f;
        float physicalScale =
                Math.max(
                        0.01f,
                        Math.min(
                                preferred,
                                Math.min(
                                        (screenWidth * gui - 12f) / panelWidth,
                                        (screenHeight * gui - 12f) / panelHeight)));
        float scale = physicalScale / gui;
        return new ShopViewport(
                scale,
                (screenWidth - panelWidth * scale) / 2f,
                (screenHeight - panelHeight * scale) / 2f);
    }

    public double localX(double x) {
        return (x - left) / scale;
    }

    public double localY(double y) {
        return (y - top) / scale;
    }

    public int screenX(double x) {
        return Math.round((float) x * scale + left);
    }

    public int screenY(double y) {
        return Math.round((float) y * scale + top);
    }
}
