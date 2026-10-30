package org.mediasync.engine;

import java.util.*;
import org.mediasync.Runner;

public class FileLister
{
    private final Runner r;
    private final String s;

    public FileLister(Runner runner, String serial)
    {
	this.r = runner;
	this.s = serial;
    }

    public List<String> list(String remoteDir)
    {
	Runner.RunResult rr = r.adb("-s", s, "shell", "find", remoteDir, "-type", "-f");

	if (rr.exitc() != 0) { return List.of(); }

	List<String> result = new ArrayList<>();
	for (String ln : rr.output().lines().toList())
	{
	    String path = line.strip();
	    if (path.isEmpty()) continue;
	    if (path.startsWith("*") || path.startsWith("adb:")) continue;

	    result.add(path);
	}
	return result;
    }
}
