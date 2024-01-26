/*
 * This file is part of ClipboardList.

 * ClipboardList is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 ClipboardList Contributions
 */
package com.hchen.clipboardlist.hook;

import de.robv.android.xposed.XposedBridge;

public class Log {
    public static final String hookMain = "[HChen]";
    public static final String mHook = "[HChen:";
    public static final String mode = "]";

    public static void logI(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "I: " + Log);
    }

    public static void logI(String name, String tag, String Log) {
        XposedBridge.log(mHook + name + mode + "[" + tag + "]: " + "I: " + Log);
    }

    public static void logW(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "W: " + Log);
    }

    public static void logE(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "E: " + Log);
    }

    public static void logSI(String name, String tag, String log) {
        android.util.Log.i(mHook + name + mode, "[" + tag + "]: I: " + log);
    }

    public static void logSI(String tag, String log) {
        android.util.Log.i(hookMain, "[" + tag + "]: I: " + log);
    }

    public static void logSW(String tag, String log) {
        android.util.Log.w(hookMain, "[" + tag + "]: W: " + log);
    }

    public void logSE(String tag, String log) {
        android.util.Log.e(hookMain, "[" + tag + "]: E: " + log);
    }
}
