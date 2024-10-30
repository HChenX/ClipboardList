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
package com.hchen.clipboardlist.hook.unlockIme;

import static com.hchen.hooktool.log.XposedLog.logE;

import android.content.Context;

import com.hchen.clipboardlist.hook.LoadInputMethodDex;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IAction;
import com.hchen.hooktool.tool.additional.PropTool;

import java.util.Arrays;
import java.util.List;

public class UnlockIme extends BaseHC implements LoadInputMethodDex.OnInputMethodDexLoad {
    private boolean shouldHook = false;

    private static final String[] miuiImeList = new String[]{
            "com.iflytek.inputmethod.miui",
            "com.sohu.inputmethod.sogou.xiaomi",
            "com.baidu.input_mi",
            "com.miui.catcherpatch"
    };

    private int navBarColor = 0;

    @Override
    public void load(ClassLoader classLoader) {
        if (!shouldHook) return;
        fakeSupportImeList(classLoader);
        notDeleteNotSupportIme("com.miui.inputmethod.InputMethodBottomManager$MiuiSwitchInputMethodListener", classLoader);
        Class<?> InputMethodBottomManager = findClass("com.miui.inputmethod.InputMethodBottomManager", classLoader).get();
        if (InputMethodBottomManager != null) {
            fakeIsSupportIme(InputMethodBottomManager);
            fakeIsXiaoAiEnable(InputMethodBottomManager);
        } else {
            logE(TAG, "Class not found: com.miui.inputmethod.InputMethodBottomManager");
        }
    }

    @Override
    public void init() {
        if (PropTool.getProp("ro.miui.support_miui_ime_bottom", "0").equals("1")) {
            startHook();
        }
    }

    private void startHook() {
        // 检查是否为小米定制输入法
        if (Arrays.stream(miuiImeList).anyMatch(s -> s.equals(lpparam.packageName))) return;
        shouldHook = true;
        Class<?> sInputMethodServiceInjector = findClass("android.inputmethodservice.InputMethodServiceInjector").get();
        if (sInputMethodServiceInjector == null)
            sInputMethodServiceInjector = findClass("android.inputmethodservice.InputMethodServiceStubImpl").get();
        if (sInputMethodServiceInjector != null) {
            fakeIsSupportIme(sInputMethodServiceInjector);
            fakeIsXiaoAiEnable(sInputMethodServiceInjector);
            setPhraseBgColor(sInputMethodServiceInjector);
        } else {
            logE(TAG, "Class not found: InputMethodServiceInjector");
        }

        notDeleteNotSupportIme("android.inputmethodservice.InputMethodServiceInjector$MiuiSwitchInputMethodListener", classLoader);
    }

    /**
     * 跳过包名检查，直接开启输入法优化
     */
    private void fakeIsSupportIme(Class<?> clazz) {
        setStaticField(clazz, "sIsImeSupport", 1);
        hookMethod(clazz, "isImeSupport", Context.class, returnResult(true));
    }

    /**
     * 小爱语音输入按钮失效修复
     */
    private void fakeIsXiaoAiEnable(Class<?> clazz) {
        hookMethod(clazz, "isXiaoAiEnable", returnResult(false));
    }

    /**
     * 在适当的时机修改抬高区域背景颜色
     */
    private void setPhraseBgColor(Class<?> clazz) {
        hookMethod("com.android.internal.policy.PhoneWindow",
                "setNavigationBarColor", int.class,
                new IAction() {
                    @Override
                    public void after() {
                        if ((int) first() == 0) return;
                        navBarColor = first();
                        customizeBottomViewColor(clazz);
                    }
                }
        );

        hookAllMethod(clazz, "addMiuiBottomView",
                new IAction() {
                    @Override
                    public void after() {
                        customizeBottomViewColor(clazz);
                    }
                }
        );
    }

    /**
     * 将导航栏颜色赋值给输入法优化的底图
     */
    private void customizeBottomViewColor(Class<?> clazz) {
        if (navBarColor != 0) {
            int color = -0x1 - navBarColor;
            callStaticMethod(clazz, "customizeBottomViewColor", true, navBarColor, color | -0x1000000, color | 0x66000000);
        }
    }

    /**
     * 针对A10的修复切换输入法列表
     */
    private void notDeleteNotSupportIme(String className, ClassLoader classLoader) {
        hookMethod(className, classLoader, "deleteNotSupportIme", doNothing());
    }

    /**
     * 使切换输入法界面显示第三方输入法
     */
    private void fakeSupportImeList(ClassLoader classLoader) {
        hookMethod("com.miui.inputmethod.InputMethodBottomManager", classLoader, "getSupportIme",
                new IAction() {
                    @Override
                    public void before() {
                        List<?> getEnabledInputMethodList = callMethod(getField(getStaticField(
                                "com.miui.inputmethod.InputMethodBottomManager", classLoader,
                                "sBottomViewHelper"), "mImm"), "getEnabledInputMethodList");
                        setResult(getEnabledInputMethodList);
                    }
                }
        );
    }
}