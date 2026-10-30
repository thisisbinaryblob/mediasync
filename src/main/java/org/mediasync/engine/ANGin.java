package org.mediasync.engine;

public class ANGin {
    private final Device dev;
    private final Runner run;
    private final Cache cache;

    public ANGin(Runner run, Cache cache, Device dev)
    {
	this.run = run;
	this.cache = cache;
	this.dev = dev;
    }

    public SyncResult run()
    {
	
    }
}
