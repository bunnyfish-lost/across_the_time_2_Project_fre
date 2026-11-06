package fr.poubone.att2.client.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.network.chat.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class QuestTrackingStore {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, QuestTrackingStore.Profile> profiles = new LinkedHashMap<>();

    public QuestTrackingStore(Path var1) {
        this.file = var1;

        try {
            if (Files.isRegularFile(var1)) {
                QuestTrackingStore.Data var2 =
                        (QuestTrackingStore.Data)
                                this.gson.fromJson(
                                        Files.readString(var1, StandardCharsets.UTF_8),
                                        QuestTrackingStore.Data.class);
                if (var2 != null && var2.profiles != null) {
                    this.profiles = var2.profiles;
                }
            }
        } catch (Exception var3) {
            System.err.println(
                    "[ATT2] Quest tracking state could not be read: " + var3.getMessage());
        }
    }

    private QuestTrackingStore.Profile profile(String var1) {
        return this.profiles.computeIfAbsent(var1, var0 -> new QuestTrackingStore.Profile());
    }

    public QuestInfo pinned(String var1, Function<String, QuestInfo> var2) {
        return this.resolve(this.profile(var1).pinned, var2);
    }

    public QuestInfo target(String var1, Function<String, QuestInfo> var2) {
        QuestTrackingStore.Profile var3 = this.profile(var1);
        return this.resolve(var3.pinned != null ? var3.pinned : var3.last, var2);
    }

    public boolean isPinned(String var1, QuestInfo var2) {
        QuestTrackingStore.Snapshot var3 = this.profile(var1).pinned;
        return var2 != null && var3 != null && var2.key().equals(var3.key);
    }

    public void togglePin(String var1, QuestInfo var2) {
        if (var2 != null) {
            QuestTrackingStore.Profile var3 = this.profile(var1);
            var3.pinned = this.isPinned(var1, var2) ? null : QuestTrackingStore.Snapshot.of(var2);
            this.save();
        }
    }

    public void recordLast(String var1, QuestInfo var2) {
        if (var2 != null) {
            this.profile(var1).last = QuestTrackingStore.Snapshot.of(var2);
            this.save();
        }
    }

    private QuestInfo resolve(QuestTrackingStore.Snapshot var1, Function<String, QuestInfo> var2) {
        if (var1 == null) {
            return null;
        }

        QuestInfo var3 = var1.restore();
        if (var3 == null) {
            return null;
        }

        QuestInfo var4 = (QuestInfo) var2.apply(var1.key);
        return var4 != null ? var4 : var3;
    }

    private void save() {
        Path var1 = this.file.resolveSibling(this.file.getFileName() + ".tmp");

        try {
            Files.createDirectories(this.file.getParent());
            QuestTrackingStore.Data var2 = new QuestTrackingStore.Data();
            var2.profiles = this.profiles;
            Files.writeString(var1, this.gson.toJson(var2), StandardCharsets.UTF_8);

            try {
                Files.move(
                        var1,
                        this.file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException var4) {
                Files.move(var1, this.file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception var5) {
            System.err.println(
                    "[ATT2] Quest tracking state could not be saved: " + var5.getMessage());
        }
    }

    private static final class Data {
        Map<String, QuestTrackingStore.Profile> profiles = new LinkedHashMap<>();
    }

    private static final class Profile {
        QuestTrackingStore.Snapshot pinned;
        QuestTrackingStore.Snapshot last;
    }

    private static final class Snapshot {
        String key;
        String type;
        String name;
        String status;
        String city;
        int number;
        int trigger;

        static QuestTrackingStore.Snapshot of(QuestInfo var0) {
            QuestTrackingStore.Snapshot var1 = new QuestTrackingStore.Snapshot();
            var1.key = var0.key();
            var1.type = var0.type().name();
            var1.name = var0.plainName();
            var1.status = var0.status().name();
            var1.city = var0.city();
            var1.number = var0.number();
            var1.trigger = var0.progressTrigger();
            return var1;
        }

        QuestInfo restore() {
            try {
                if (this.name != null
                        && this.name.length() <= 4096
                        && this.number >= 0
                        && this.trigger >= 0
                        && this.trigger <= 10000000) {
                    QuestInfo var1 =
                            new QuestInfo(
                                    QuestType.valueOf(this.type),
                                    this.number,
                                    Component.literal(this.name),
                                    QuestStatus.valueOf(this.status),
                                    this.trigger,
                                    this.city,
                                    -1);
                    return var1.key().equals(this.key) ? var1 : null;
                } else {
                    return null;
                }
            } catch (Exception var2) {
                return null;
            }
        }
    }
}
