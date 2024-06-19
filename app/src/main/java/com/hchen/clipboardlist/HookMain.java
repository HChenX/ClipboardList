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
package com.hchen.clipboardlist;

import static com.hchen.hooktool.log.XposedLog.logE;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.hchen.clipboardlist.clipboard.NewClipboardList;
import com.hchen.clipboardlist.phrase.UnPhraseLimit;
import com.hchen.clipboardlist.unlockIme.UnlockIme;
import com.hchen.hooktool.HCInit;
import com.hchen.hooktool.utils.ContextUtils;

import org.luckypray.dexkit.DexKitBridge;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage {
    public static List<String> mAppsUsingInputMethod = new ArrayList<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (mAppsUsingInputMethod.isEmpty()) {
            mAppsUsingInputMethod = getAppsUsingInputMethod(ContextUtils.getContext(ContextUtils.FLAG_ALL));
        }
        String pkg = lpparam.packageName;
        HCInit.setTAG("ClipboardList");
        HCInit.setLogLevel(HCInit.LOG_D);
        if (lpparam.packageName.equals("com.miui.phrase")) {
            HCInit.initLoadPackageParam(lpparam);
            System.loadLibrary("dexkit");
            DexKitBridge dexKitBridge = DexKitBridge.create(lpparam.appInfo.sourceDir);
            new UnPhraseLimit(dexKitBridge).onCreate();
            dexKitBridge.close();
            return;
        }
        if (isInputMethod(pkg)) {
            HCInit.initLoadPackageParam(lpparam);
            new NewClipboardList().onCreate();
            new UnlockIme().onCreate();
        }
    }

    private List<String> getAppsUsingInputMethod(Context context) {
        try {
            if (context == null) {
                logE("ClipboardList", "context is null");
                return new ArrayList<>();
            }
            List<String> pkgName = new ArrayList<>();
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> enabledInputMethods = inputMethodManager.getEnabledInputMethodList();
            for (InputMethodInfo inputMethodInfo : enabledInputMethods) {
                pkgName.add(inputMethodInfo.getServiceInfo().packageName);
            }
            return pkgName;
        } catch (Throwable throwable) {
            logE("ClipboardList", throwable);
            return new ArrayList<>();
        }
    }

    private boolean isInputMethod(String pkgName) {
        if (mAppsUsingInputMethod.isEmpty()) {
            return false;
        }
        for (String inputMethod : mAppsUsingInputMethod) {
            if (inputMethod.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }

}
