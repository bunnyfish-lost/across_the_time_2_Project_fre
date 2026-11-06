package fr.poubone.att2.client.spell;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;

import java.util.function.Predicate;

public final class SpellWheelMovement {
    private final KeyMapping[] keys;
    private final Predicate<KeyMapping> physical;
    private final Predicate<KeyMapping> toggle;
    private final boolean[] before;
    private final boolean[] previous;
    private boolean started;
    private boolean closed;

    public SpellWheelMovement(Minecraft var1) {
        this(
                bindings(var1),
                var1x -> SpellWheelInput.held(var1, var1x),
                var1x -> toggle(var1, var1x));
    }

    public SpellWheelMovement(
            KeyMapping[] var1, Predicate<KeyMapping> var2, Predicate<KeyMapping> var3) {
        this.keys = (KeyMapping[]) var1.clone();
        this.physical = var2;
        this.toggle = var3;
        this.before = new boolean[this.keys.length];
        this.previous = new boolean[this.keys.length];

        for (int var4 = 0; var4 < this.keys.length; var4++) {
            this.before[var4] = this.keys[var4].isDown();
            this.previous[var4] = var2.test(this.keys[var4]);
        }
    }

    private static KeyMapping[] bindings(Minecraft var0) {
        Options var1 = var0.options;
        return var1 == null
                ? new KeyMapping[0]
                : new KeyMapping[] {
                    var1.keyUp,
                    var1.keyDown,
                    var1.keyLeft,
                    var1.keyRight,
                    var1.keyJump,
                    var1.keyShift,
                    var1.keySprint
                };
    }

    private static boolean toggle(Minecraft var0, KeyMapping var1) {
        Options var2 = var0.options;
        return var2 != null
                && (var1 == var2.keyShift && var2.toggleCrouch().get()
                        || var1 == var2.keySprint && var2.toggleSprint().get());
    }

    public void start() {
        if (!this.started && !this.closed) {
            this.started = true;

            for (int var1 = 0; var1 < this.keys.length; var1++) {
                KeyMapping var2 = this.keys[var1];
                if (var2 instanceof ToggleKeyMapping var3) {
                    var3.shouldRestoreStateOnScreenClosed();
                }

                if (this.toggle.test(var2) && this.before[var1] && !var2.isDown()) {
                    var2.setDown(true);
                }
            }

            this.update();
        }
    }

    public void update() {
        if (!this.closed) {
            if (!this.started) {
                this.start();
            } else {
                for (int var1 = 0; var1 < this.keys.length; var1++) {
                    KeyMapping var2 = this.keys[var1];
                    boolean var3 = this.physical.test(var2);
                    if (this.toggle.test(var2)) {
                        if (var3 && !this.previous[var1]) {
                            var2.setDown(true);
                        }
                    } else {
                        var2.setDown(var3);
                    }

                    this.previous[var1] = var3;
                }
            }
        }
    }

    public void close(boolean var1) {
        if (!this.closed) {
            if (var1) {
                this.update();
            } else {
                for (KeyMapping var5 : this.keys) {
                    if (this.toggle.test(var5)) {
                        if (var5.isDown()) {
                            var5.setDown(true);
                        }
                    } else {
                        var5.setDown(false);
                    }
                }
            }

            for (KeyMapping var10 : this.keys) {
                if (var10 instanceof ToggleKeyMapping var6) {
                    var6.shouldRestoreStateOnScreenClosed();
                }
            }

            this.closed = true;
        }
    }
}
