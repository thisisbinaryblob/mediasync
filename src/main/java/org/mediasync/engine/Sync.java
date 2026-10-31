package org.mediasync.engine;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.mediasync.Cache;
import org.mediasync.Main;
import org.mediasync.Runner;

public class Sync
{
    private final FileLister l;
    private final Puller p;
    private final Media m;
    private final Cache c;
    private final String remoteRoot;
    private final Main.Options opt;
    private final Runner runner;

    public Sync(FileLister lis, Puller pul, Media med, Cache cac,
                String rer, Main.Options opts, Runner runner)
    {
        this.l = lis;
        this.p = pul;
        this.m = med;
        this.c = cac;
        this.opt = opts;
        this.remoteRoot = rer;
        this.runner = runner;
    }

    public record SyncResult(
            int total,
            int alreadySynced,
            List<String> copied,
            List<String> skipped,
            List<String> failedPull,
            List<String> failedMove,
            List<String> deleted,
            List<String> failedDelete,
            long durationMillis)
    {
        public static Builder builder() { return new Builder(); }

        public static class Builder
        {
            private int total, alreadySynced;
            private final List<String> copied       = new ArrayList<>();
            private final List<String> skipped      = new ArrayList<>();
            private final List<String> failedPull   = new ArrayList<>();
            private final List<String> failedMove   = new ArrayList<>();
            private final List<String> deleted      = new ArrayList<>();
            private final List<String> failedDelete = new ArrayList<>();
            private long durationMillis;

            public Builder total(int v) { this.total = v; return this; }
            public Builder alreadySynced(int v) { this.alreadySynced = v; return this; }
            public Builder copied(String v) { copied.add(v); return this; }
            public Builder skipped(String v) { skipped.add(v); return this; }
            public Builder failedPull(String v) { failedPull.add(v); return this; }
            public Builder failedMove(String v) { failedMove.add(v); return this; }
            public Builder deleted(String v) { deleted.add(v); return this; }
            public Builder failedDelete(String v) { failedDelete.add(v); return this; }
            public Builder durationMillis(long v) { this.durationMillis = v; return this; }

            public SyncResult build()
            {
                return new SyncResult(total, alreadySynced, copied, skipped,
                        failedPull, failedMove, deleted, failedDelete, durationMillis);
            }
        }

        public String summary()
        {
            return String.format(
                    "total=%d, already=%d, copied=%d, skipped=%d, failedPull=%d, failedMove=%d, deleted=%d, failedDelete=%d, %.1fs",
                    total, alreadySynced, copied.size(), skipped.size(),
                    failedPull.size(), failedMove.size(),
                    deleted.size(), failedDelete.size(), durationMillis / 1000.0);
        }
    }

    public SyncResult sync(ProgressListener progress) throws IOException
    {
        long start = System.currentTimeMillis();

        List<String> remote = l.list(remoteRoot, opt.noRecursive());
        Set<String> synced = c.loadAsyncd();

        List<String> newFiles = new ArrayList<>();
        for (String path : remote)
            if (!synced.contains(path)) newFiles.add(path);

        SyncResult.Builder result = SyncResult.builder()
                .total(remote.size())
                .alreadySynced(remote.size() - newFiles.size());

        int current = 0;
        for (String remotePath : newFiles)
        {
            current++;
            String filename = Path.of(remotePath).getFileName().toString();

            if (progress != null) progress.onFile(remotePath, current, newFiles.size());

            Path cacheFile = c.devDir().resolve(filename);

            if (!p.pull(remotePath, cacheFile))
            {
                result.failedPull(remotePath);
                continue;
            }

            // customLocation: всё в одну папку, без классификации
            Path targetDir;
            if (opt.customLocation().isPresent())
            {
                targetDir = opt.customLocation().get();
            }
            else
            {
                Media.Kind kind = m.classify(filename);
                targetDir = m.targetDir(kind);
                if (targetDir == null)
                {
                    result.skipped(remotePath);
                    continue;
                }
            }

            Path target = targetDir.resolve(filename);
            try
            {
                Files.createDirectories(targetDir);
                Files.move(cacheFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException io)
            {
                result.failedMove(remotePath);
                continue;
            }

            synced.add(remotePath);
            result.copied(remotePath);

            if (opt.deleteAfterSync())
            {
                Runner.RunResult rm = runner.adb("-s", p.getSerial(),
                        "shell", "rm", remotePath);
                if (rm.exitc() != 0)
                    result.failedDelete(remotePath);
                else
                    result.deleted(remotePath);
            }
        }

        c.saveAsyncd(synced);

        return result.durationMillis(System.currentTimeMillis() - start).build();
    }

    public interface ProgressListener
    {
        void onFile(String remotePath, int current, int total);
    }
}