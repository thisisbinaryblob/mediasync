package org.mediasync;

import org.mediasync.Runner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adb
{
    Runner adbRunner = new Runner();
    private static final Pattern PROP = Pattern.compile("^\\[(.+?)]: \\[(.*)]$");
    private static final Pattern MEM_TOTAL = Pattern.compile("^MemTotal:\\s+(\\d+)\\s+kB$");

    public record Device (
        String model,
        String manufacturer,
        String serial,
        String version,
        int sdk,
        long storageTotal,
        long storageAvailable,
        long ramTotal
    ) {
        public long storageUsed() { return storageTotal - storageAvailable; }
    }

    public Optional<String> getFirstDeviceSerial()
    {
        Runner.RunResult rr = adbRunner.adb("devices");
        if (rr.exitc() != 0) return Optional.empty();

        return rr.output().lines()
                .skip(1)
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .map(line -> line.split("\\s+"))
                .filter(parts -> parts.length >= 2 && "device".equals(parts[1]))
                .map(parts -> parts[0])
                .findFirst();
    }

    public Map<String,String> getAndParseProps()
    {
        Runner.RunResult rr = adbRunner.adb("shell", "getprop");
        if (rr.exitc() != 0) return Map.of();

        Map<String, String> props = new HashMap<>();
        rr.output().lines()
                .map(String::strip)
                .map(PROP::matcher)
                .filter(Matcher::matches)
                .forEach(m -> props.put(m.group(1), m.group(2)));
        return props;
    }

    public long[] getAndParseStorage()
    {
        Runner.RunResult rr = adbRunner.adb("shell", "df", "/sdcard");
        if (rr.exitc() != 0) return new long[]{-1, -1};

        return rr.output().lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("Filesystem"))
                .map(line -> line.split("\\s+"))
                .filter(parts -> parts.length >= 6)
                .findFirst()
                .map(parts -> {
                    long totalKb = Long.parseLong(parts[1]);  // 1K-blocks
                    long availKb = Long.parseLong(parts[3]);  // Available
                    return new long[]{ totalKb / 1024, availKb / 1024 }; // MB
                })
                .orElse(new long[]{-1, -1});
    }

    public long getAndParseRam()
    {
        Runner.RunResult rr = adbRunner.adb("shell", "cat", "/proc/meminfo");
        if (rr.exitc() != 0) return -1;

        return rr.output().lines()
                .map(String::strip)
                .map(MEM_TOTAL::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(m -> Long.parseLong(m.group(1)) / 1024)  // kB -> MB
                .orElse(-1L);
    }

    private static int parseIntSafe(String s, int def)
    {
        if (s == null || s.isBlank()) return def;
        try {
            return Integer.parseInt(s.strip());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public Device collect()
    {
        Map<String, String> props = getAndParseProps();
        long[] storage = getAndParseStorage();
        long ramTotal = getAndParseRam();
        Optional<String> serial = getFirstDeviceSerial();

        return new Device(
                props.getOrDefault("ro.product.model", "unknown"),
                props.getOrDefault("ro.product.manufacturer", "unknown"),
                props.getOrDefault("ro.serialno", serial.toString()),
                props.getOrDefault("ro.build.version.release", "unknown"),
                parseIntSafe(props.get("ro.build.version.sdk"), -1),
                storage[0], storage[1],
                ramTotal
        );
    }
}
