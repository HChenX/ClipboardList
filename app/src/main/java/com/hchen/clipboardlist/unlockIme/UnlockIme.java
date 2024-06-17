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
package com.hchen.clipboardlist.unlockIme;

import static com.hchen.hooktool.log.XposedLog.logE;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.tool.ParamTool;
import com.hchen.hooktool.utils.PropUtils;

import java.util.Arrays;
import java.util.List;

public class UnlockIme extends BaseHC {

    private static final String[] miuiImeList = new String[]{
            "com.iflytek.inputmethod.miui",
            "com.sohu.inputmethod.sogou.xiaomi",
            "com.baidu.input_mi",
            "com.miui.catcherpatch"
    };

    private int navBarColor = 0;

    @Override
    public void init() {
        if (PropUtils.getProp("ro.miui.support_miui_ime_bottom", "0").equals("1")) {
            startHook();
        }
    }

    private void startHook() {
        // 检查是否为小米定制输入法
        if (Arrays.stream(miuiImeList).anyMatch(s -> s.equals(lpparam.packageName))) return;
        Class<?> sInputMethodServiceInjector = findClass("android.inputmethodservice.InputMethodServiceInjector");
        if (sInputMethodServiceInjector == null)
            sInputMethodServiceInjector = findClass("android.inputmethodservice.InputMethodServiceStubImpl");
        if (sInputMethodServiceInjector != null) {
            hookSIsImeSupport(sInputMethodServiceInjector);
            hookIsXiaoAiEnable(sInputMethodServiceInjector);
            setPhraseBgColor(sInputMethodServiceInjector);
        } else {
            logE(TAG, "Class not found: InputMethodServiceInjector");
        }

        hookDeleteNotSupportIme("android.inputmethodservice.InputMethodServiceInjector$MiuiSwitchInputMethodListener",
                lpparam.classLoader);

        // 获取常用语的ClassLoader
        classTool.findClass("immm", "android.inputmethodservice.InputMethodModuleManager")
                .getMethod("loadDex", ClassLoader.class, String.class)
                .hook(new IAction() {
                    @Override
                    public void after(ParamTool param) {
                        getSupportIme(param.first());
                        hookDeleteNotSupportIme("com.miui.inputmethod.InputMethodBottomManager$MiuiSwitchInputMethodListener",
                                param.first());
                        Class<?> InputMethodBottomManager = findClass("com.miui.inputmethod.InputMethodBottomManager",
                                param.first());
                        if (InputMethodBottomManager != null) {
                            hookSIsImeSupport(InputMethodBottomManager);
                            hookIsXiaoAiEnable(InputMethodBottomManager);
                        } else {
                            logE(TAG, "Class not found: com.miui.inputmethod.InputMethodBottomManager");
                        }
                    }
                });
    }

    /**
     * 跳过包名检查，直接开启输入法优化
     *
     * @param clazz 声明或继承字段的类
     */
    private void hookSIsImeSupport(Class<?> clazz) {
        try {
            setStaticField(clazz, "sIsImeSupport", 1);
        } catch (Throwable throwable) {
            logE(TAG, "Hook field sIsImeSupport: " + throwable);
        }
    }

    /**
     * 小爱语音输入按钮失效修复
     *
     * @param clazz 声明或继承方法的类
     */
    private void hookIsXiaoAiEnable(Class<?> clazz) {
        try {
            hook(findAnyMethod(clazz, "isXiaoAiEnable"), returnResult(false));
        } catch (Throwable throwable) {
            logE(TAG, "Hook method isXiaoAiEnable: " + throwable);
        }
    }

    /**
     * 在适当的时机修改抬高区域背景颜色
     *
     * @param clazz 声明或继承字段的类
     */
    private void setPhraseBgColor(Class<?> clazz) {
        try {
            classTool.findClass("pw", "com.android.internal.policy.PhoneWindow")
                    .getMethod("setNavigationBarColor", int.class)
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            if ((int) param.first() == 0) return;
                            navBarColor = param.first();
                            customizeBottomViewColor(clazz);
                        }
                    });


            hook(findAnyMethod(clazz, "addMiuiBottomView"),
                    new IAction() {
                        @Override
                        public void after(ParamTool param) {
                            customizeBottomViewColor(clazz);
                        }
                    });
        } catch (Throwable throwable) {
            logE(TAG, "Set the color of the MiuiBottomView: " + throwable);
        }
    }

    /**
     * 将导航栏颜色赋值给输入法优化的底图
     *
     * @param clazz 声明或继承字段的类
     */
    private void customizeBottomViewColor(Class<?> clazz) {
        try {
            if (navBarColor != 0) {
                int color = -0x1 - navBarColor;
                callStaticMethod(clazz, "customizeBottomViewColor", new Object[]{true, navBarColor, color | -0x1000000, color | 0x66000000});
            }
        } catch (Throwable e) {
            logE(TAG, "Call customizeBottomViewColor: " + e);
        }
    }

    /**
     * 针对A10的修复切换输入法列表
     *
     * @param className 声明或继承方法的类的名称
     */
    private void hookDeleteNotSupportIme(String className, ClassLoader classLoader) {
        try {
            hook(findAnyMethod(findClass(className, classLoader), "deleteNotSupportIme"),
                    doNothing());
        } catch (Throwable throwable) {
            logE(TAG, "Hook method deleteNotSupportIme: " + throwable);
        }
    }

    /**
     * 使切换输入法界面显示第三方输入法
     *
     * @param classLoader
     */
    private void getSupportIme(ClassLoader classLoader) {
        try {
            classTool.findClass("imbm", "com.miui.inputmethod.InputMethodBottomManager", classLoader)
                    .getMethod("getSupportIme")
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) {
                            List<?> getEnabledInputMethodList = callMethod(getField(getStaticField(
                                    findClass("com.miui.inputmethod.InputMethodBottomManager", classLoader),
                                    "sBottomViewHelper"), "mImm"), "getEnabledInputMethodList");
                            param.setResult(getEnabledInputMethodList);
                        }
                    });
        } catch (Throwable e) {
            logE(TAG, "Hook method getSupportIme: " + e);
        }
    }
}
