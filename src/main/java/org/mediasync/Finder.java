package org.mediasync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Finder
{
    private String cachedAdb;
    private String cachedPictures;
    private String cachedVideos;
    private String cachedMusic;

    public Optional<String> findAdb()
    {
        if (cachedAdb != null) return Optional.of(cachedAdb);
        String os = System.getProperty("os.name").toLowerCase();
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String exe = os.contains("win") ? "adb.exe" : "adb";
            for (String dir : pathEnv.split(File.pathSeparator)) {
                File candidate = new File(dir, exe);
                if (candidate.isFile() && candidate.canExecute()) {
                    cachedAdb = candidate.getAbsolutePath();
                    return Optional.of(cachedAdb);
                }
            }
        }

        String sdkPath = System.getenv("ANDROID_HOME");
        if (sdkPath == null)
            sdkPath = System.getenv("ANDROID_SDK_ROOT");

        if (sdkPath != null) {
            Optional<String> adb = checkAdbInSdk(sdkPath);
            if (adb.isPresent()) return adb;
        }

        String userHome = System.getProperty("user.home");
        String defaultSdkPath = null;

        if (os.contains("win"))
        {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null) defaultSdkPath = localAppData + File.separator + "Android" + File.separator + "Sdk";
        } else if (os.contains("mac")) defaultSdkPath = userHome + "/Library/Android/sdk";
        else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) defaultSdkPath = userHome + "/Android/Sdk";

        if (defaultSdkPath != null)
            return checkAdbInSdk(defaultSdkPath);

        return Optional.empty();
    }

    private Optional<String> checkAdbInSdk(String sdkPath)
    {
        String os = System.getProperty("os.name").toLowerCase();
        String adbExecutableName = os.contains("win") ? "adb.exe" : "adb";

        File platformToolsDir = new File(sdkPath + File.separator + "platform-tools");
        File adbFile = new File(platformToolsDir, adbExecutableName);

        if (adbFile.isFile() && adbFile.canExecute())
        {
            cachedAdb = adbFile.getAbsolutePath();
            return Optional.of(cachedAdb);
        }
        return Optional.empty();
    }

    public Optional<String> findPictures()
    {
        if (cachedPictures != null) return Optional.of(cachedPictures);
        Optional<String> dir = findStandardDir(new String[]{"Pictures", "Картинки", "Изображения"});
        dir.ifPresent(path -> cachedPictures = path);
        return dir;
    }

    public Optional<String> findVideos()
    {
        if (cachedVideos != null) return Optional.of(cachedVideos);
        Optional<String> dir = findStandardDir(new String[]{"Videos", "Видео"});
        dir.ifPresent(path -> cachedVideos = path);
        return dir;
    }

    public Optional<String> findMusic()
    {
        if (cachedMusic != null) return Optional.of(cachedMusic);
        Optional<String> dir = findStandardDir(new String[]{"Music", "Музыка"});
        dir.ifPresent(path -> cachedMusic = path);
        return dir;
    }

    private Optional<String> findStandardDir(String[] possibleNames)
    {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isEmpty()) return Optional.empty();

        File baseDir = new File(userHome);
        for (String name : possibleNames) {
            File candidate = new File(baseDir, name);
            if (candidate.isDirectory()) return Optional.of(candidate.getAbsolutePath());
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            File oneDriveDir = new File(baseDir, "OneDrive");
            if (oneDriveDir.isDirectory()) {
                for (String name : possibleNames) {
                    File candidate = new File(oneDriveDir, name);
                    if (candidate.isDirectory()) return Optional.of(candidate.getAbsolutePath());
                }
            }
        }

        return Optional.empty();
    }
}
