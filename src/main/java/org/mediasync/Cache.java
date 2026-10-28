package org.mediasync;

import java.nio.file.Path;
import java.util.Set;

public class Cache
{
    private final Path root;
    private final Path perDeviceDirectory;
    private final Path asyncdFile;

    public Cache(Adb.Device device);

    public Set<String> loadAsyncd();
    public void saveAsyncd();
    public boolean contains(String path);

    public Path devDir();
    public Path root();
}
