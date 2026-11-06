package fr.poubone.att2.client.gambling;

import java.util.List;
import java.util.OptionalInt;

public final class ScratchAutoReveal {
    private boolean active;
    private boolean hasBudget;
    private long budget;
    private long lastSent;
    private long sentRevision;
    private int lastLocked;
    private List<Integer> shape = List.of();
    private ScratchAutoReveal.Target pending;
    private ScratchAutoReveal.Reason reason = ScratchAutoReveal.Reason.NONE;

    public boolean active() {
        return this.active;
    }

    public boolean busy() {
        return this.pending != null;
    }

    public ScratchAutoReveal.Reason reason() {
        return this.reason;
    }

    public void stop(ScratchAutoReveal.Reason var1) {
        this.active = false;
        this.reason = var1;
    }

    public void reset() {
        this.active = false;
        this.hasBudget = false;
        this.budget = 0L;
        this.pending = null;
        this.shape = List.of();
        this.reason = ScratchAutoReveal.Reason.NONE;
        this.lastLocked = 0;
    }

    private static List<Integer> shape(ScratchAutoReveal.View var0) {
        return var0.board().stream().map(List::size).toList();
    }

    private static int locked(ScratchAutoReveal.View var0) {
        int var1 = 0;

        for (List<GamblingModel.GridCell> var3 : var0.board()) {
            for (GamblingModel.GridCell var5 : var3) {
                if (var5.isLocked()) {
                    var1++;
                }
            }
        }

        return var1;
    }

    private static ScratchAutoReveal.Target first(ScratchAutoReveal.View var0) {
        for (int var1 = 0; var1 < var0.board().size(); var1++) {
            for (int var2 = 0; var2 < var0.board().get(var1).size(); var2++) {
                GamblingModel.GridCell var3 = var0.board().get(var1).get(var2);
                if (var3.isLocked() && var3.trigger() >= 2332 && var3.trigger() <= 2843) {
                    return new ScratchAutoReveal.Target(var1, var2, var3);
                }
            }
        }

        return null;
    }

    public boolean canStart(ScratchAutoReveal.View var1) {
        return !this.active
                && this.pending == null
                && !var1.board().isEmpty()
                && first(var1) != null
                && var1.near()
                && !var1.waiting()
                && !var1.insufficient()
                && var1.price() >= 0
                && var1.affordable()
                && var1.balance().isPresent()
                && Math.min(
                                var1.balance().getAsInt(),
                                this.hasBudget ? this.budget : Long.MAX_VALUE)
                        >= var1.price();
    }

    public boolean start(ScratchAutoReveal.View var1, long var2) {
        if (!this.canStart(var1)) {
            return false;
        }

        this.shape = shape(var1);
        this.lastLocked = locked(var1);
        this.budget =
                Math.min(var1.balance().getAsInt(), this.hasBudget ? this.budget : Long.MAX_VALUE);
        this.hasBudget = true;
        this.active = true;
        this.reason = ScratchAutoReveal.Reason.NONE;
        this.lastSent = var2 - 150L;
        return true;
    }

    public ScratchAutoReveal.Target next(ScratchAutoReveal.View var1, long var2) {
        if (!this.active && this.pending == null
                || this.shape.equals(shape(var1)) && locked(var1) <= this.lastLocked) {
            this.lastLocked = locked(var1);
            if ((this.active || this.pending != null) && this.lastLocked == 0) {
                this.pending = null;
                this.stop(ScratchAutoReveal.Reason.COMPLETE);
                return null;
            }

            if (this.active) {
                if (!var1.near()) {
                    this.stop(ScratchAutoReveal.Reason.TOO_FAR);
                } else if (var1.insufficient()) {
                    this.stop(ScratchAutoReveal.Reason.FUNDS);
                } else if (var1.waiting()) {
                    this.stop(ScratchAutoReveal.Reason.LOADING);
                }
            }

            if (this.pending != null) {
                GamblingModel.GridCell var4 =
                        var1.board().get(this.pending.row()).get(this.pending.column());
                if (var4.isLocked() || var1.priceRevision() <= this.sentRevision) {
                    if (var2 - this.lastSent >= 10000L) {
                        this.pending = null;
                        this.stop(ScratchAutoReveal.Reason.TIMEOUT);
                        return null;
                    } else {
                        return null;
                    }
                }

                this.pending = null;
            }

            if (!this.active) {
                return null;
            }

            if (locked(var1) == 0) {
                this.stop(ScratchAutoReveal.Reason.COMPLETE);
                return null;
            }

            if (var1.price() >= 0 && !var1.balance().isEmpty()) {
                this.budget = Math.min(this.budget, var1.balance().getAsInt());
                if (!var1.affordable() || this.budget < var1.price()) {
                    this.stop(ScratchAutoReveal.Reason.FUNDS);
                    return null;
                }

                if (var2 - this.lastSent < 150L) {
                    return null;
                }

                ScratchAutoReveal.Target var5 = first(var1);
                if (var5 == null) {
                    this.stop(ScratchAutoReveal.Reason.BUSY);
                }

                return var5;
            } else {
                this.stop(ScratchAutoReveal.Reason.NO_DATA);
                return null;
            }
        } else {
            this.reset();
            this.stop(ScratchAutoReveal.Reason.CHANGED);
            return null;
        }
    }

    public void submitted(ScratchAutoReveal.Target var1, ScratchAutoReveal.View var2, long var3) {
        if (this.active && this.pending == null && var1 != null) {
            this.pending = var1;
            this.lastSent = var3;
            this.sentRevision = var2.priceRevision();
            this.budget = this.budget - var2.price();
        } else {
            throw new IllegalStateException("No active single-cell request");
        }
    }

    public enum Reason {
        NONE,
        STOPPED,
        COMPLETE,
        FUNDS,
        NO_DATA,
        TOO_FAR,
        LOADING,
        TIMEOUT,
        CHANGED,
        BUSY;
    }

    public record Target(int row, int column, GamblingModel.GridCell cell) {}

    public record View(
            List<List<GamblingModel.GridCell>> board,
            int price,
            boolean affordable,
            OptionalInt balance,
            boolean near,
            boolean waiting,
            boolean insufficient,
            long priceRevision) {}
}
