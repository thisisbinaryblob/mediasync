package org.mediasync;

import org.mediasync.Adb;
import org.mediasync.Runner;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.out.println("Hello and welcome!");

        var r = new Runner();

        var adbParser = new Adb();
        Adb.Device device = adbParser.collect();
        System.out.println(device.toString());
    }
}