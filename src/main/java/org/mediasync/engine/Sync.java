package org.mediasync.engine;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.mediasync.Cache;
public class Sync
{
    private final FileLister l;
    private final Puller p;
    private final Media m;
    private final Cache c;
    private final String remoteRoot;

    public Sync(FileLister lis, Puller pul, Media med, Cache cac, String rer)
    {
	this.l = lis;
	this.p = pul;
	this.m = med;
	this.c = cac;
	this.remoteRoot = rer;
    }

    public record SyncResult(
	int total,
        int alreadySynced,
        List<String> copied,
        List<String> skipped,
        List<String> failedPull,
        List<String> failedMove,
        long durationMillis)
    {
	    public static Builder builder() { return new Builder(); }

	    public static class Builder
	    {
		    private int total, alreadySynced;
		    private final List<String> copied     = new ArrayList<>();
		    private final List<String> skipped    = new ArrayList<>();
		    private final List<String> failedPull = new ArrayList<>();
		    private final List<String> failedMove = new ArrayList<>();
		    private long durationMillis;

		    public Builder total(int v) { this.total = v; return this; }
		    public Builder alreadySynced(int v) { this.alreadySynced = v; return this; }
		    public Builder copied(String v) { copied.add(p); return this; }
		    public Builder skipped(String v) { skipped.add(p); return this; }
		    public Builder failedPull(String v) { failedPull.add(p); return this; }
		    public Builder failedMove(String v) { failedMove.add(p); return this; }
		    public Builder durationMillis(long v) { this.durationMillis = v; return this; }
		    public SyncResult build()
		    {
			return new SyncResult(total, alreadySynced, copied, skipped, failedPull, failedMove, durationMillis);
		    }
	    }

	    public String symmary()
	    {
		return String.format(
		    "total=%d, already=%d, copied=%d, skipped=%d, failedPull=%d, failedMove=%d, %.lfs",
		    total, alreadySynced, copied.size(), skipped.size(), failedPull.size(), failedMove.size(), durationMillis / 1000.0);
	    }
    }

    public SyncResult sync(ProgressListener progress) throws IOException
    {
	
    }

    public interface ProgressListener {
	void onFile(String remotePath, int current, int total);
    }
}
