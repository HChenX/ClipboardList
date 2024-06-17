package com.hchen.clipboardlist.file;

import static com.hchen.hooktool.log.XposedLog.logE;
import static com.hchen.hooktool.log.XposedLog.logI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
    public static String TAG = "FileHelper";

    public static boolean exists(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent == null) {
            logE(TAG, "path: " + path + " parent is null");
            return false;
        }
        if (!parent.exists()) {
            if (parent.mkdirs()) {
                logI(TAG, "success to mkdirs: " + parent);
            } else {
                logE(TAG, "failed to mkdirs: " + parent);
                return false;
            }
        }
        if (file.exists()) {
            return true;
        } else {
            try {
                if (file.createNewFile()) {
                    return true;
                }
            } catch (IOException e) {
                logE(TAG, e);
            }
        }
        return false;
    }

    public static void write(String path, String str) {
        if (str == null) {
            logE(TAG, "str is null?? are you sure? path: " + path);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(path, false))) {
            writer.write(str);
        } catch (IOException e) {
            logE(TAG, e);
        }
    }

    public static String read(String path) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(path))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            logE(TAG, e);
            return "";
        }
    }

    public static boolean empty(String path) {
        String result = read(path);
        return result.isEmpty() || result.equals("[]");
    }
}
