package org.mediasync;


import java.io.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.mediasync.Finder;

public class Runner
{
    private final Finder finder = new Finder();
    public record RunResult (String output, int exitc)
    {
        public RunResult()
        {
            this("", 0);
        }
    }

    public RunResult adb(String... args)
    {
        try
        {
            var f = new Finder();
            Optional<String> adbpath = f.findAdb();

            if (adbpath.isEmpty()) {
                System.out.println("Error: adb not found, install platform-tools");
                return new RunResult("adb not found", 1);
            }
            String adb = adbpath.get();
            //System.out.println("Info: using adb at " + adb);
            String[] cmd = new String[args.length + 1];
            cmd[0] = adb;
            System.arraycopy(args, 0, cmd, 1, args.length);
            Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();

            String output;
            try (var reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                return new RunResult(e.getMessage(), -1);
            }

            if (!proc.waitFor(30, TimeUnit.SECONDS))
            {
                proc.destroyForcibly();
                return new RunResult("timeout", -2);
            }

            return new RunResult(output, proc.exitValue());
        } catch (IOException e) {
            return new RunResult("ADB failed: " + e.getMessage(), 1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RunResult("interrupted", -3);
        }
    }
}
