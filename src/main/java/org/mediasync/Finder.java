package org.mediasync;

import java.io.File;
import java.util.Optional;

public class Finder
{
    private String cached;
    public Optional<String> findAdb()
    {
        if (cached != null) return Optional.of(cached);
        String os = System.getProperty("os.name").toLowerCase();
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String exe = os.contains("win") ? "adb.exe" : "adb";
            for (String dir : pathEnv.split(File.pathSeparator)) {
                File candidate = new File(dir, exe);
                if (candidate.isFile() && candidate.canExecute()) {
                    cached = candidate.getAbsolutePath();
                    return Optional.of(cached);
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
            cached = adbFile.getAbsolutePath();
            return Optional.of(cached);
        }
        return Optional.empty();
    }
}
