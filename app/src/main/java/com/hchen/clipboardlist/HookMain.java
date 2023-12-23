package com.hchen.clipboardlist;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.hchen.clipboardlist.clipboard.ClipboardList;
import com.hchen.clipboardlist.hook.Hook;
import com.hchen.clipboardlist.hook.Log;
import com.hchen.clipboardlist.unlockIme.UnlockIme;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage {
    public static List<String> mAppsUsingInputMethod = new ArrayList<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (mAppsUsingInputMethod.isEmpty()) {
            mAppsUsingInputMethod = getAppsUsingInputMethod(Hook.findContext());
        }
        String pkg = lpparam.packageName;
        initHook(new ClipboardList(), lpparam, isInputMethod(pkg));
        initHook(new UnlockIme(), lpparam, isInputMethod(pkg));
    }

    public static void initHook(Hook hook, LoadPackageParam param, boolean needHook) {
        if (needHook)
            hook.runHook(param);
    }

    private List<String> getAppsUsingInputMethod(Context context) {
        if (context == null) {
            Log.logE("getAppsUsingInputMethod", "context is null");
            return new ArrayList<>();
        }
        List<String> pkgName = new ArrayList<>();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledInputMethods = inputMethodManager.getEnabledInputMethodList();
        for (InputMethodInfo inputMethodInfo : enabledInputMethods) {
            pkgName.add(inputMethodInfo.getServiceInfo().packageName);
        }
        return pkgName;
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
