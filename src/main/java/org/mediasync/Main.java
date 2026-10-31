package org.mediasync;

import org.mediasync.Adb;
import org.mediasync.Runner;
import org.mediasync.Cache;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import org.mediasync.engine.*;
import java.util.Optional;

public class Main
{
    public static final String VERSION = "v0.1.0";

    public record Options(
            String serial,
            String remoteRoot,
            Optional<Path> customLocation,
            boolean noRecursive,
            boolean verbose,
            boolean deleteAfterSync,
            int ttlDays,
            boolean version,
            boolean about,
            boolean help
    )
    {
        public static Options parse(String[] argus)
        {
            String serial = "";
            String remoteRoot = "/sdcard/DCIM";
            Path customLocation = null;
            boolean noRecursive = false;
            boolean verbose = false;
            int ttlDays = 7;
            boolean deleteAfterSync = false;
            boolean version = false;
            boolean about = false;
            boolean help = false;
            for (int i = 0; i < argus.length; i++)
            {
                switch (argus[i])
                {
                    case "-s", "--serial" -> {
                        if (i + 1 < argus.length) serial = argus[++i];
                    }
                    case "-rR", "--remoteRoot" -> {
                        if (i + 1 < argus.length) remoteRoot = argus[++i];
                    }
                    case "--customLocation", "-cL" -> {
                        if (i + 1 < argus.length) customLocation = Path.of(argus[++i]);
                    }
                    case "--ttlDays", "-t" -> {
                        if (i + 1 < argus.length) try { ttlDays = Integer.parseInt(argus[++i]); } catch (NumberFormatException e) { System.err.println("Bad ttl! Reseting to default..."); ttlDays = 7; }
                    }
                    case "-R", "--noRecursive" -> noRecursive = true;
                    case "-V", "--verbose" -> verbose = true;
                    case "-v", "--version" -> {
                        System.out.println("MediAsync " + VERSION);
                        version = true;
                    }
                    case "-h", "--help" -> {
                        printHelpMessage();
                        help = true;
                    }
                    case "-a", "--about" -> {
                        printAbout();
                        about = true;
                    }
                    case "-d", "--deleteAfterSync" ->  deleteAfterSync = true;
                    default -> printHelpMessage();
                };
            }
            return new Options(serial, remoteRoot, Optional.ofNullable(customLocation), noRecursive, verbose, deleteAfterSync, ttlDays, version, about, help);
        }

        private static void printHelpMessage()
        {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.out.println("\nMediAsync help");
            System.out.println("\t--serial/-s\t\t|Specify a serial that program will be forced to use.");
            System.out.println("\t--remoteRoot/-rR\t|Specify a custom remote root where program will be scanning for media.");
            System.out.println("\t--customLocation/-cL\t|Specify a custom local root where program will be locate the media.");
            System.out.println("\t--noRecursive/-R\t|Say the program to scan your phone only in one parent folder.");
            System.out.println("\t--verbose/-V\t\t|Say the program to print out the debug logs.");
            System.out.println("\t--ttlDays/-t\t\t|Specify the ttl in days which on end will be clearing the cache directory.");
            System.out.println("\t--deleteAfterSync/-d\t|Delete after sync.");
            System.out.println("\t--version/-v\t\t|Show version.");
            System.out.println("\t--help/-h\t\t|Show this message.");
            System.out.println("\t--about/-a\t\t|Show message about mediasync.");
            return;
        }

        private static void printAbout()
        {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.out.println("© 2026 MediAsync. The MediAsync Program");
            System.out.println("MediAsync is licensed under GNU GPL v3. Written in Java.");
            System.out.println("Using adb which is licensed under Apache 2.0 as external program.");
            try
            {
                String pathStr = Main.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .getPath();
                File jarFile = new File(pathStr);
                File jarDir = jarFile.getParentFile();

                System.out.println("Located at: " + jarDir.getAbsolutePath());
            } catch (URISyntaxException e) {
                System.out.println("An cause with getting your location: Traceback: " + e.getCause());
            }
            Finder f = new Finder();
            System.out.println("Adb located at: " + f.findAdb().toString());
            return;
        }
    }

    public static void main(String[] args)
    {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Options opt = Options.parse(args);
        if (opt.version() || opt.about() || opt.help()) return;
        var adbParser = new Adb();
        var runner = new Runner();
        var find = new Finder();
        Adb.Device device = adbParser.collect();

        if (device.serial().isEmpty() || device.serial().contains("Optional") || device.storageTotal() <= 0)
        {
            System.err.println("\033[31mError\033[0m: No device connected, aborting");
            return;
        }
        try
        {
            var lister = new FileLister(runner, device.serial());
            var puller = new Puller(runner, device.serial());
            var classifier = new Media(Path.of(find.findPictures().orElse("Pictures")),
                    Path.of(find.findMusic().orElse("Music")),
                    Path.of(find.findVideos().orElse("Videos")),
                    Path.of("Other"));
            var cache = new Cache(device);

        } catch (IOException ignored) {}
        System.out.println("Device data struct: " + device.toString());
    }
}
