package org.mediasync.engine;

import java.nio.file.*;
import java.io.IOException;

public class Media
{
    public enum Kind { PICTURES, MUSIC, VIDEO, OTHER }

    private final Path picturesDir;
    private final Path musicDir;
    private final Path videosDir;
    private final Path other;

    public Media(Path picturesDir, Path musicDir, Path videosDir, Path otherDir)
    {
            this.picturesDir = picturesDir;
        this.musicDir = musicDir;
        this.videosDir = videosDir;
        this.other = otherDir;
    }

    public Kind classify(String filename)
    {
        try
        {
            Path pa = Paths.get(filename);
            String mime = Files.probeContentType(pa);

            if (mime == null) return Kind.OTHER;

            if (mime.startsWith("image/")) return Kind.PICTURES;

            if (mime.startsWith("audio/")) return Kind.PICTURES;

            if (mime.startsWith("video/")) return Kind.VIDEO;

        } catch (IOException io) {}
        return Kind.OTHER;
    }

    public Path targetDir(Kind k)
    {
        return switch (k) {
            case PICTURES -> picturesDir;
            case VIDEO -> videosDir;
            case MUSIC -> musicDir;
            case OTHER -> other;
            default -> other;
        };
    }
}
