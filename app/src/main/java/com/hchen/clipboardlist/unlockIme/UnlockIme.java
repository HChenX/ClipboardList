package com.hchen.clipboardlist.unlockIme;

import static com.hchen.clipboardlist.hook.Hook.HookAction.returnConstant;

import android.view.inputmethod.InputMethodManager;

import com.hchen.clipboardlist.hook.Hook;

import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class UnlockIme extends Hook {

    private final String[] miuiImeList = new String[]{
            "com.iflytek.inputmethod.miui",
            "com.sohu.inputmethod.sogou.xiaomi",
            "com.baidu.input_mi",
            "com.miui.catcherpatch"
    };

    private int navBarColor = 0;

    @Override
    public void init() {
        if (getProp("ro.miui.support_miui_ime_bottom", "0").equals("1")) {
            startHook(loadPackageParam);
        }
    }

    private void startHook(LoadPackageParam param) {
        // 检查是否为小米定制输入法
        boolean isNonCustomize = true;
        for (String isMiui : miuiImeList) {
            if (isMiui.equals(param.packageName)) {
                isNonCustomize = false;
                break;
            }
        }
        if (!isNonCustomize) {
            return;
        }
        Class<?> sInputMethodServiceInjector = findClassIfExists("android.inputmethodservice.InputMethodServiceInjector");
        if (sInputMethodServiceInjector == null)
            sInputMethodServiceInjector = findClassIfExists("android.inputmethodservice.InputMethodServiceStubImpl");
        if (sInputMethodServiceInjector != null) {
            hookSIsImeSupport(sInputMethodServiceInjector);
            hookIsXiaoAiEnable(sInputMethodServiceInjector);
            setPhraseBgColor(sInputMethodServiceInjector);
        } else {
            logE(tag, "Class not found: InputMethodServiceInjector");
        }

        hookDeleteNotSupportIme("android.inputmethodservice.InputMethodServiceInjector$MiuiSwitchInputMethodListener",
                param.classLoader);

        // 获取常用语的ClassLoader
        findAndHookMethod("android.inputmethodservice.InputMethodModuleManager",
                "loadDex", ClassLoader.class, String.class,
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        getSupportIme((ClassLoader) param.args[0]);
                        hookDeleteNotSupportIme("com.miui.inputmethod.InputMethodBottomManager$MiuiSwitchInputMethodListener", (ClassLoader) param.args[0]);
                        Class<?> InputMethodBottomManager = findClassIfExists("com.miui.inputmethod.InputMethodBottomManager", (ClassLoader) param.args[0]);
                        if (InputMethodBottomManager != null) {
                            hookSIsImeSupport(InputMethodBottomManager);
                            hookIsXiaoAiEnable(InputMethodBottomManager);
                            try {
                                // 针对A11的修复切换输入法列表
                                hookAllMethods(InputMethodBottomManager, "getSupportIme",
                                        new HookAction() {
                                            @Override
                                            protected void before(MethodHookParam param) {
                                                param.setResult(((InputMethodManager) getObjectField(
                                                        getStaticObjectField(
                                                                InputMethodBottomManager,
                                                                "sBottomViewHelper"),
                                                        "mImm")).getEnabledInputMethodList());
                                            }
                                        }
                                );
                            } catch (Throwable throwable) {
                                logE(tag, "getSupportIme: " + throwable);
                            }
                        } else {
                            logE(tag, "Class not found: com.miui.inputmethod.InputMethodBottomManager");
                        }
                    }
                }
        );
    }

    /**
     * 跳过包名检查，直接开启输入法优化
     *
     * @param clazz 声明或继承字段的类
     */
    private void hookSIsImeSupport(Class<?> clazz) {
        try {
            setStaticObjectField(clazz, "sIsImeSupport", 1);
        } catch (Throwable throwable) {
            logE(tag, "Hook field sIsImeSupport: " + throwable);
        }
    }

    /**
     * 小爱语音输入按钮失效修复
     *
     * @param clazz 声明或继承方法的类
     */
    private void hookIsXiaoAiEnable(Class<?> clazz) {
        try {
            hookAllMethods(clazz, "isXiaoAiEnable", returnConstant(false));
        } catch (Throwable throwable) {
            logE(tag, "Hook method isXiaoAiEnable: " + throwable);
        }
    }

    /**
     * 在适当的时机修改抬高区域背景颜色
     *
     * @param clazz 声明或继承字段的类
     */
    private void setPhraseBgColor(Class<?> clazz) {
        try {
            findAndHookMethod("com.android.internal.policy.PhoneWindow",
                    "setNavigationBarColor", int.class,
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            if ((int) param.args[0] == 0) return;
                            navBarColor = (int) param.args[0];
                            customizeBottomViewColor(clazz);
                        }
                    }
            );
            hookAllMethods(clazz, "addMiuiBottomView",
                    new HookAction() {
                        @Override
                        protected void after(MethodHookParam param) {
                            customizeBottomViewColor(clazz);
                        }
                    }
            );
        } catch (Throwable throwable) {
            logE(tag, "Failed to set the color of the MiuiBottomView: " + throwable);
        }
    }

    /**
     * 将导航栏颜色赋值给输入法优化的底图
     *
     * @param clazz 声明或继承字段的类
     */
    private void customizeBottomViewColor(Class<?> clazz) {
        if (navBarColor != 0) {
            int color = -0x1 - navBarColor;
            callStaticMethod(clazz, "customizeBottomViewColor", true, navBarColor, color | -0x1000000, color | 0x66000000);
        }
    }

    /**
     * 针对A10的修复切换输入法列表
     *
     * @param className 声明或继承方法的类的名称
     */
    private void hookDeleteNotSupportIme(String className, ClassLoader classLoader) {
        try {
            hookAllMethods(findClassIfExists(className, classLoader), "deleteNotSupportIme", returnConstant(null));
        } catch (Throwable throwable) {
            logE(tag, "Hook method deleteNotSupportIme: " + throwable);
        }
    }

    /**
     * 使切换输入法界面显示第三方输入法
     *
     * @param classLoader
     */
    private void getSupportIme(ClassLoader classLoader) {
        try {
            findAndHookMethod("com.miui.inputmethod.InputMethodBottomManager",
                    classLoader, "getSupportIme",
                    new HookAction() {

                        @Override
                        protected void before(MethodHookParam param) {
                            List<?> getEnabledInputMethodList = (List<?>) callMethod(getObjectField(getStaticObjectField(
                                    findClassIfExists("com.miui.inputmethod.InputMethodBottomManager", classLoader),
                                    "sBottomViewHelper"), "mImm"), "getEnabledInputMethodList");
                            param.setResult(getEnabledInputMethodList);
                        }
                    }
            );
        } catch (Throwable e) {
            logE(tag, "Hook method getSupportIme: " + e);
        }
    }
}
