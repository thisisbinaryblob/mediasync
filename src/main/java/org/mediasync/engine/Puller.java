package org.mediasync.engine;

import java.io.IOException;
import java.nio.file.*;
import java.util.OptionalLong;
import org.mediasync.Runner;

public class Puller {
    private final Runner run;
    private final String serial;

    public Puller(Runner run, String serial)
    {
        this.run = run;
        this.serial = serial;
    }

    public String getSerial() {
        return this.serial;
    }

    public boolean pull(String remotePath, Path localPath) throws IOException
    {
        Files.createDirectories(localPath.getParent());

        Runner.RunResult rr = run.adb("-s", serial, "pull", remotePath, localPath.toString());
        System.out.println("find exitc=" + rr.exitc());
        System.out.println("find raw output:\n" + rr.output());
        if (rr.exitc() != 0) return false;

        if (!Files.isRegularFile(localPath)) return false;

        OptionalLong remoteSize = remoteFileSize(remotePath);

        if (remoteSize.isEmpty()) return false;

        long localSize = Files.size(localPath);
        return localSize == remoteSize.getAsLong();
    }

    private OptionalLong remoteFileSize(String remp)
    {
	Runner.RunResult rre = run.adb("-s", serial, "shell", "stat", "-c", "%s", remp);

	if (rre.exitc() != 0 || rre.output().isBlank())
	{
	    rre = run.adb("-s", serial, "shell", "wc", "-c", remp);
        if (rre.exitc() != 0 || rre.output().isBlank()) return OptionalLong.empty();
	}

	String first = rre.output().strip().split("\\s+")[0];

	try { return OptionalLong.of(Long.parseLong(first)); }
	catch (NumberFormatException e) { return OptionalLong.empty(); }
    }
}
