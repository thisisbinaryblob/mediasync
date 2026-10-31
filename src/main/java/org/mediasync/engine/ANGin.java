package org.mediasync.engine;

import org.mediasync.engine.Sync;
import org.mediasync.*;

import java.io.IOException;
import java.nio.file.Path;

public class ANGin {
    private final Runner run;
    private final Finder fin;
    private final Adb adbx;
    public ANGin(Runner run, Finder finfet, Adb adbx)
    {
	    this.run = run;
	    this.fin = finfet;
        this.adbx = adbx;
    }

    public Sync.SyncResult run(Main.Options opt) throws IOException
    {
        String adb = fin.findAdb().orElseThrow(() -> new IOException("adb not found"));

        String serial = opt.serial();
        if (serial == null || serial.isEmpty())
            serial = adbx.getFirstDeviceSerial().orElseThrow(() -> new IOException("no device connected"));

        Adb.Device dev = adbx.collect();

        Cache cac = new Cache(dev);

        FileLister lister = new FileLister(run, serial);

        Puller pull = new Puller(run, serial);

        Media media = new Media(Path.of(fin.findPictures().orElse("Pictures")),
                Path.of(fin.findMusic().orElse("Music")),
                Path.of(fin.findVideos().orElse("Videos")),
                Path.of("Other"));

        Runner run = new Runner();

        Sync sync = new Sync(lister, pull, media, cac, opt.remoteRoot(), opt, run);

        return sync.sync(opt.verbose()
            ? (path, cur, total) -> System.out.printf("[%d/%d] %s%n", cur, total, path)
            : null);
    }
}
