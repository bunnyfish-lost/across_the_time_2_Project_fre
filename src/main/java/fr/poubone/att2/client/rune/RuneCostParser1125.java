package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.data.Att2Triggers;

import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class RuneCostParser1125 {
    private static final Pattern NUMBER =
            Pattern.compile("(?:0|[1-9][0-9]*|[1-9][0-9]{0,2}(?:,[0-9]{3})+)");

    private RuneCostParser1125() {}

    public static Map<Integer, RuneCostParser1125.Cost> parse(
            Component var0, Function<String, OptionalInt> var1) {
        LinkedHashMap var2 = new LinkedHashMap();
        if (var0 != null) {
            visit(var0, var1, var2, 0);
        }

        return var2;
    }

    private static void visit(
            Component var0,
            Function<String, OptionalInt> var1,
            Map<Integer, RuneCostParser1125.Cost> var2,
            int var3) {
        if (var3 <= 64) {
            if (var0.getStyle().getClickEvent() instanceof RunCommand var4) {
                int var10 = Att2Triggers.parseTriggerCommand(var4.command());
                Optional var6 = byTrigger(var10);
                if (var6.isPresent()) {
                    ArrayList<RuneCostParser1125.Atom> var7 = new ArrayList();
                    flatten(var0, var10, (RuneCatalog.RuneDef) var6.get(), var1, var7, 0);
                    if (var7.stream().anyMatch(var0x -> var0x.label == 1)) {
                        RuneCostParser1125.Cost var8 = cost(var7, (RuneCatalog.RuneDef) var6.get());
                        if (var2.containsKey(var10)
                                && !((RuneCostParser1125.Cost) var2.get(var10)).equals(var8)) {
                            var8 =
                                    new RuneCostParser1125.Cost(
                                            OptionalInt.empty(), OptionalInt.empty());
                        }

                        var2.put(var10, var8);
                    }
                }
            }

            for (Component var11 : var0.getSiblings()) {
                visit(var11, var1, var2, var3 + 1);
            }
        }
    }

    public static Optional<RuneCatalog.RuneDef> byTrigger(int var0) {
        return RuneCatalog.allRunes().stream()
                .filter(var1 -> var1.craftTrigger() == var0)
                .findFirst();
    }

    private static void flatten(
            Component var0,
            int var1,
            RuneCatalog.RuneDef var2,
            Function<String, OptionalInt> var3,
            List<RuneCostParser1125.Atom> var4,
            int var5) {
        if (var5 <= 64 && var4.size() <= 512) {
            if (var0.getStyle().getClickEvent() instanceof RunCommand var6
                    && Att2Triggers.parseTriggerCommand(var6.command()) != var1) {
                var4.add(new RuneCostParser1125.Atom(0, "?"));
            } else {
                ComponentContents var11 = var0.getContents();
                if (var11 instanceof TranslatableContents var12) {
                    String var9 = var12.getKey();
                    if (var9.equals("att2.runes.recipe.runepowder")
                            && var12.getArgs().length == 0) {
                        var4.add(new RuneCostParser1125.Atom(1, ""));
                    } else if (var9.equals("att2.runes.recipe.esc")
                            && var12.getArgs().length == 0) {
                        var4.add(new RuneCostParser1125.Atom(2, ""));
                    } else {
                        var4.add(new RuneCostParser1125.Atom(0, "?"));
                    }
                } else if (var11 instanceof ScoreContents var8) {
                    String var15 = var8.name().right().orElse("");
                    if (var8.objective().equals("PRICES")
                            && (var15.equals(var2.priceHolder())
                                    || var15.equals(var2.escPriceHolder()))) {
                        OptionalInt var10 = (OptionalInt) var3.apply(var15);
                        var4.add(
                                new RuneCostParser1125.Atom(
                                        0,
                                        var10.isPresent()
                                                ? Integer.toString(var10.getAsInt())
                                                : "?"));
                    } else {
                        var4.add(new RuneCostParser1125.Atom(0, "?"));
                    }
                } else {
                    var11.visit(
                            var1x -> {
                                var4.add(new RuneCostParser1125.Atom(0, var1x));
                                return Optional.empty();
                            });
                }

                for (Component var14 : var0.getSiblings()) {
                    flatten(var14, var1, var2, var3, var4, var5 + 1);
                }
            }
        } else {
            var4.add(new RuneCostParser1125.Atom(0, "?"));
        }
    }

    private static RuneCostParser1125.Cost cost(
            List<RuneCostParser1125.Atom> var0, RuneCatalog.RuneDef var1) {
        int var2 = -1;
        int var3 = -1;

        for (int var4 = 0; var4 < var0.size(); var4++) {
            if (((RuneCostParser1125.Atom) var0.get(var4)).label == 1) {
                if (var2 >= 0) {
                    return unknown();
                }

                var2 = var4;
            }

            if (((RuneCostParser1125.Atom) var0.get(var4)).label == 2) {
                if (var3 >= 0) {
                    return unknown();
                }

                var3 = var4;
            }
        }

        if (var2 >= 0 && (var3 < 0 || var3 >= var2) && join(var0, 0, var2).strip().equals("[")) {
            String var7 = join(var0, var2 + 1, var3 >= 0 ? var3 : var0.size());
            OptionalInt var5 = number(var7, var3 < 0);
            OptionalInt var6 =
                    var3 < 0
                            ? OptionalInt.empty()
                            : number(join(var0, var3 + 1, var0.size()), true);
            if (var3 < 0 && var1.level() < 18 && var5.isPresent()) {
                var6 = OptionalInt.of(0);
            }

            return new RuneCostParser1125.Cost(var5, var6);
        } else {
            return unknown();
        }
    }

    private static RuneCostParser1125.Cost unknown() {
        return new RuneCostParser1125.Cost(OptionalInt.empty(), OptionalInt.empty());
    }

    private static String join(List<RuneCostParser1125.Atom> var0, int var1, int var2) {
        StringBuilder var3 = new StringBuilder();

        for (int var4 = var1; var4 < var2; var4++) {
            if (((RuneCostParser1125.Atom) var0.get(var4)).label != 0) {
                return "?";
            }

            var3.append(((RuneCostParser1125.Atom) var0.get(var4)).text);
        }

        return var3.toString();
    }

    private static OptionalInt number(String var0, boolean var1) {
        String var2 = var0.strip();
        if (var1) {
            if (!var2.endsWith("]")) {
                return OptionalInt.empty();
            }

            var2 = var2.substring(0, var2.length() - 1).strip();
        }

        if (!var2.startsWith(":") && !var2.startsWith("：")) {
            return OptionalInt.empty();
        }

        var2 = var2.substring(1).strip();
        if (!NUMBER.matcher(var2).matches()) {
            return OptionalInt.empty();
        }

        try {
            return OptionalInt.of(Integer.parseInt(var2.replace(",", "")));
        } catch (NumberFormatException var4) {
            return OptionalInt.empty();
        }
    }

    private record Atom(int label, String text) {}

    public record Cost(OptionalInt powder, OptionalInt esc) {
        public boolean complete() {
            return this.powder.isPresent() && this.esc.isPresent();
        }
    }
}
