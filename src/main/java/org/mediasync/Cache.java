package org.mediasync;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import org.mediasync.Adb;

public class Cache
{
    private static final String APP_DIR = ".mediasync";
    private final Path root;
    private final Path perDeviceDirectory;
    private final Path asyncdFile;

    public Cache(Adb.Device device) throws IOException 
    {
        this.root = Path.of(System.getProperty("user.home"), APP_DIR);
        String hash = Long.toUnsignedString(fnv1a64(device.serial() + device.manufacturer() + device.version()), 16);
        this.perDeviceDirectory = root.resolve(hash);
        this.asyncdFile = perDeviceDirectory.resolve("asyncd.txt");
        Files.createDirectories(perDeviceDirectory);
    }

    public Set<String> loadAsyncd() throws IOException
    {
        if (!Files.exists(asyncdFile)) return new HashSet<>();
        return new HashSet<>(Files.readAllLines(asyncdFile, StandardCharsets.UTF_8));
    }

    public void saveAsyncd(Set<String> asyncd) throws IOException
    {
        Path tmp = asyncdFile.resolveSibling(asyncdFile.getFileName() + ".tmp");
        Files.write(tmp, asyncd, StandardCharsets.UTF_8);

        try {
            Files.move(tmp, asyncdFile, 
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e)
        {
            Files.move(tmp, asyncdFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (AccessDeniedException e) 
        {
            // реран для залупы под названием "Шиндовс"
            for (int i = 0; i < 5; i++)
            {
                try { Thread.sleep(100); } catch (InterruptedException in) 
                { Thread.currentThread().interrupt(); break; }
                try 
                {
                    Files.move(tmp, asyncdFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    return;
                } catch (AccessDeniedException ignored) {}
            }
            throw e;    
        }
    }

    public Path devDir() { return perDeviceDirectory; }
    public Path root() { return root; }

    private static long fnv1a64(String s) 
    {
        long h = 0xcbf29ce484222325L;
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            h ^= (b & 0xff);
            h *= 0x100000001b3L;
        }
        return h;
    }
}
