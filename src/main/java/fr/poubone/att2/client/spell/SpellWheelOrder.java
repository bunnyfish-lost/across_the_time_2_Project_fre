package fr.poubone.att2.client.spell;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class SpellWheelOrder {
    private final Path file;
    private List<Integer> order = new ArrayList<>(SpellWheelCatalog.IDS);

    public SpellWheelOrder(Path var1) {
        this.file = var1;

        try {
            if (Files.isRegularFile(var1) && Files.size(var1) <= 65536L) {
                JsonObject var2 =
                        JsonParser.parseString(Files.readString(var1, StandardCharsets.UTF_8))
                                .getAsJsonObject();
                ArrayList var3 = new ArrayList();

                for (JsonElement var5 : var2.getAsJsonArray("order")) {
                    if (var5.isJsonPrimitive() && var5.getAsJsonPrimitive().isNumber()) {
                        var3.add(var5.getAsInt());
                    }
                }

                this.order = normalize(var3);
            }
        } catch (Exception var6) {
            System.err.println("[ATT2] Spell wheel order could not be read: " + var6.getMessage());
        }
    }

    public static List<Integer> normalize(List<Integer> var0) {
        LinkedHashSet var1 = new LinkedHashSet();
        if (var0 != null) {
            for (Integer var3 : var0) {
                if (var3 != null && SpellWheelCatalog.IDS.contains(var3)) {
                    var1.add(var3);
                }
            }
        }

        var1.addAll(SpellWheelCatalog.IDS);
        return new ArrayList<>(var1);
    }

    public List<Integer> ids() {
        return List.copyOf(this.order);
    }

    public SpellWheelOrder.Result swap(int var1, int var2) {
        if (var1 >= 0
                && var2 >= 0
                && var1 < this.order.size()
                && var2 < this.order.size()
                && var1 != var2) {
            Collections.swap(this.order, var1, var2);

            try {
                JsonObject var3 = new JsonObject();
                var3.addProperty("version", 1);
                JsonArray var4 = new JsonArray();

                for (int var6 : this.order) {
                    var4.add(var6);
                }

                var3.add("order", var4);
                Files.createDirectories(this.file.getParent());
                Path var9 = this.file.resolveSibling(this.file.getFileName() + ".tmp");
                Files.writeString(
                        var9,
                        new GsonBuilder().setPrettyPrinting().create().toJson(var3),
                        StandardCharsets.UTF_8);

                try {
                    Files.move(
                            var9,
                            this.file,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException var7) {
                    Files.move(var9, this.file, StandardCopyOption.REPLACE_EXISTING);
                }

                return SpellWheelOrder.Result.SAVED;
            } catch (Exception var8) {
                Collections.swap(this.order, var1, var2);
                System.err.println("[ATT2] Spell wheel order save failed: " + var8.getMessage());
                return SpellWheelOrder.Result.FAILED;
            }
        } else {
            return SpellWheelOrder.Result.UNCHANGED;
        }
    }

    public enum Result {
        UNCHANGED,
        SAVED,
        FAILED;
    }
}
