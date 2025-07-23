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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.clipboardlist;

import androidx.annotation.NonNull;

import com.hchen.clipboardlist.hook.LoadInputMethodDex;
import com.hchen.clipboardlist.hook.clipboard.UnlockClipboardLimit;
import com.hchen.clipboardlist.hook.clipboard.UnlockSogouLimit;
import com.hchen.clipboardlist.hook.phrase.UnlockPhraseLimit;
import com.hchen.clipboardlist.hook.unlockIme.UnlockIme;
import com.hchen.dexkitcache.DexkitCache;
import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;

import java.util.Objects;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class HookInit extends HCEntrance {
    public static String TAG = "ClipboardList";

    @NonNull
    @Override
    public HCInit.BasicData initHC(@NonNull HCInit.BasicData basicData) {
        return basicData
            .setTag(TAG)
            .setLogLevel(HCInit.LOG_D)
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogExpandPath("com.hchen.clipboardlist.hook");
    }

    @NonNull
    @Override
    public String[] ignorePackageNameList() {
        return new String[]{
            "com.miui.contentcatcher",
            "com.android.providers.settings",
            "com.android.server.telecom",
            "com.google.android.webview"
        };
    }

    @Override
    public void onLoadPackage(@NonNull LoadPackageParam lpparam) throws Throwable {
        // 加载 Dexkit 缓存工具
        if (lpparam.appInfo != null) {
            DexkitCache.init(
                "clipboard_list",
                lpparam.classLoader,
                lpparam.appInfo.sourceDir,
                lpparam.appInfo.dataDir
            );
        }

        HCInit.initLoadPackageParam(lpparam);

        if (Objects.equals(lpparam.packageName, "com.miui.phrase")) {
            new UnlockPhraseLimit().onLoadPackage();
            return;
        }

        if (
            Objects.equals(lpparam.packageName, "com.sohu.inputmethod.sogou.xiaomi") ||
                Objects.equals(lpparam.packageName, "com.sohu.inputmethod.sogou")
        ) {
            new UnlockSogouLimit().onLoadPackage();
        }

        new LoadInputMethodDex().onLoadPackage();
        new UnlockIme().onLoadPackage();
        new UnlockClipboardLimit().onApplication().onLoadPackage();
    }
}