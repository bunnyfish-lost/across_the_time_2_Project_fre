package fr.poubone.att2.client.data;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public final class MapStatBar {
    private static final String DISPLAY_PREFIX = "att2.stat.display.";
    private static final Map<String, Component> DETAILED_VALUES = new LinkedHashMap<>();
    private static float dahalProgress = Float.NaN;
    private static boolean sawPointTotals;
    private static boolean sawDetailedValues;

    private MapStatBar() {}

    public static void reset() {
        AttributeObservedValues111.reset();
        clearTotals();
        sawPointTotals = false;
        sawDetailedValues = false;
        DETAILED_VALUES.clear();
        dahalProgress = Float.NaN;
    }

    public static boolean hasDahalProgress() {
        return !Float.isNaN(dahalProgress);
    }

    public static float dahalProgress() {
        return dahalProgress;
    }

    public static boolean sawPointTotals() {
        return sawPointTotals;
    }

    public static boolean sawDetailedValues() {
        return sawDetailedValues;
    }

    public static Component detailedValue(String statKey) {
        return DETAILED_VALUES.get(statKey);
    }

    public static boolean isStatBar(BossEvent event) {
        return event == null
                ? false
                : hasPointTotals(event.getName()) || hasDetailedValues(event.getName());
    }

    static boolean isDahalBar(Component name) {
        if (name == null) {
            return false;
        } else if (!hasPointTotals(name) && !hasDetailedValues(name)) {
            String flat = name.getString();
            return flat.contains("Dahal") && flat.contains("|");
        } else {
            return true;
        }
    }

    public static boolean hasPointTotals(Component name) {
        return name != null && !parse(name).isEmpty();
    }

    public static boolean hasDetailedValues(Component name) {
        return containsDetailedValue(name);
    }

    public static void capture(Map<UUID, ? extends BossEvent> events, boolean expireIfAbsent) {
        AttributeObservedValues111.capture(events);
        boolean found = false;
        boolean detailed = false;
        boolean dahalFound = false;
        Map<String, Component> detailedValues = new LinkedHashMap<>();
        if (events != null) {
            for (BossEvent event : events.values()) {
                if (event != null) {
                    if (isDahalBar(event.getName())) {
                        dahalProgress = event.getProgress();
                        dahalFound = true;
                    }

                    if (hasPointTotals(event.getName())) {
                        Map<String, Integer> parsed = parse(event.getName());
                        if (!parsed.isEmpty()) {
                            found = true;
                            parsed.forEach(ScoreCache::put);
                        }
                    }

                    if (hasDetailedValues(event.getName())) {
                        detailed = true;
                        collectDetailedValues(event.getName(), detailedValues);
                    }
                }
            }
        }

        sawPointTotals = found;
        sawDetailedValues = detailed && !found;
        DETAILED_VALUES.clear();
        if (sawDetailedValues) {
            DETAILED_VALUES.putAll(detailedValues);
        }

        if (expireIfAbsent && !found) {
            clearTotals();
        }

        if (expireIfAbsent && !detailed) {
            DETAILED_VALUES.clear();
        }

        if (expireIfAbsent && !dahalFound) {
            dahalProgress = Float.NaN;
        }
    }

    private static void collectDetailedValues(Component component, Map<String, Component> out) {
        if (component != null) {
            if (component.getContents() instanceof TranslatableContents translatable) {
                String key = translatable.getKey();
                if (key.startsWith("att2.stat.display.")) {
                    String suffix = key.substring("att2.stat.display.".length());
                    int dot = suffix.indexOf(46);
                    String stat =
                            (dot < 0 ? suffix : suffix.substring(0, dot)).toUpperCase(Locale.ROOT);
                    if (StatManager.STAT_KEYS.contains(stat)
                            && (key.endsWith(".value")
                                    || key.contains(".value.")
                                    || stat.equals("STR"))) {
                        out.put(stat, component.copy());
                    }
                }
            }

            if (component.getContents() instanceof TranslatableContents translatable) {
                for (Object arg : translatable.getArgs()) {
                    if (arg instanceof Component nested) {
                        collectDetailedValues(nested, out);
                    }
                }
            }

            for (Component sibling : component.getSiblings()) {
                collectDetailedValues(sibling, out);
            }
        }
    }

    static Map<String, Integer> parse(Component name) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (name == null) {
            return out;
        }

        if (containsDetailedValue(name)) {
            return out;
        }

        walk(name, out);
        MapStatBarParser.collectFromFlattened(name.getString(), out);
        return out;
    }

    private static boolean containsDetailedValue(Component component) {
        if (component == null) {
            return false;
        } else if (component.getContents() instanceof TranslatableContents translatable
                && MapStatBarParser.isDetailedValueTranslation(translatable.getKey())) {
            return true;
        } else {
            if (component.getContents() instanceof TranslatableContents translatable) {
                for (Object arg : translatable.getArgs()) {
                    if (arg instanceof Component inner && containsDetailedValue(inner)) {
                        return true;
                    }
                }
            }

            for (Component sibling : component.getSiblings()) {
                if (containsDetailedValue(sibling)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static void clearTotals() {
        for (String key : StatManager.STAT_KEYS) {
            ScoreCache.remove(key + "_TOT");
        }
    }

    private static void walk(Component component, Map<String, Integer> out) {
        if (component != null) {
            ComponentContents contents = component.getContents();
            if (contents instanceof TranslatableContents translatable) {
                List<String> args = new ArrayList<>();

                for (Object arg : translatable.getArgs()) {
                    if (arg instanceof Component inner) {
                        args.add(flattenArg(inner));
                        walk(inner, out);
                    } else if (arg != null) {
                        args.add(String.valueOf(arg));
                    }
                }

                MapStatBarParser.collect(translatable.getKey(), args, out);
            } else if (contents instanceof ScoreContents score) {
                String objective = score.objective();
                if (objective != null && objective.endsWith("_TOT")) {
                    ScoreCache.readLive(objective).ifPresent(value -> out.put(objective, value));
                }
            }

            for (Component sibling : component.getSiblings()) {
                walk(sibling, out);
            }
        }
    }

    private static String flattenArg(Component component) {
        if (component.getContents() instanceof PlainTextContents text) {
            return text.text();
        } else {
            if (component.getContents() instanceof ScoreContents score) {
                OptionalInt live = ScoreCache.readLive(score.objective());
                if (live.isPresent()) {
                    return Integer.toString(live.getAsInt());
                }
            }

            return component.getString();
        }
    }
}
