package org.mediasync;

import org.mediasync.Adb;
import org.mediasync.Runner;
import org.mediasync.Cache;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.out.println("Hello and welcome!");

        var adbParser = new Adb();
        Adb.Device device = adbParser.collect();
	if (device.serial().isEmpty() || device.serial().contains("Optional") || device.storageTotal() <= 0)
	{
	    System.err.println("\033[31mError\033[0m: No device connected, aborting");
	    return;
	}
	try
	{
	    var cache = new Cache(device);
	    System.out.println("Cache root: " + cache.root() + ", cache device dir: " + cache.devDir());
	    var asyncd = Set.of("/sdcard/DCIM/Camera/IMG_001.jpg", "/sdcard/DCIM/Camera/IMG_002.png");
	    cache.saveAsyncd(asyncd);
	    System.out.println("Loaded: " + cache.loadAsyncd());
	} catch (IOException ignored) {}
        System.out.println("Device data struct: " + device.toString());
	
	
    }
}
