package com.hchen.clipboardlist.hook;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Hook extends Log {
    public String tag = getClass().getSimpleName();

    public XC_LoadPackage.LoadPackageParam loadPackageParam;

    public abstract void init();

    public void runHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            SetLoadPackageParam(loadPackageParam);
            init();
            logI(tag, "Hook Done!");
        } catch (Throwable s) {
//            logE(tag, "Hook Failed: " + e);
        }
    }

    public void SetLoadPackageParam(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    public Class<?> findClass(String className) {
        return findClass(className, loadPackageParam.classLoader);
    }

    public Class<?> findClass(String className, ClassLoader classLoader) {
        return XposedHelpers.findClass(className, classLoader);
    }

    public Class<?> findClassIfExists(String className) {
        try {
            return findClass(className);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Class no found: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Find " + newClassName + " & " + oldClassName + " is null: " + e);
            return null;
        }
    }

    public abstract static class HookAction extends XC_MethodHook {

        protected void before(MethodHookParam param) throws Throwable {
        }

        protected void after(MethodHookParam param) throws Throwable {
        }

        public HookAction() {
            super();
        }

        public HookAction(int priority) {
            super(priority);
        }

        public static HookAction returnConstant(final Object result) {
            return new HookAction(PRIORITY_DEFAULT) {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    super.before(param);
                    param.setResult(result);
                }
            };
        }

        public static final HookAction DO_NOTHING = new HookAction(PRIORITY_HIGHEST * 2) {

            @Override
            protected void before(MethodHookParam param) throws Throwable {
                super.before(param);
                param.setResult(null);
            }

        };

        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            try {
                before(param);
            } catch (Throwable e) {
                logE("beforeHookedMethod", "" + e);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            try {
                after(param);
            } catch (Throwable e) {
                logE("afterHookedMethod", "" + e);
            }
        }
    }

    public abstract static class ReplaceHookedMethod extends HookAction {

        public ReplaceHookedMethod() {
            super();
        }

        public ReplaceHookedMethod(int priority) {
            super(priority);
        }

        protected abstract Object replace(MethodHookParam param) throws Throwable;

        @Override
        public void beforeHookedMethod(MethodHookParam param) {
            try {
                Object result = replace(param);
                param.setResult(result);
            } catch (Throwable t) {
                logE("replaceHookedMethod", "" + t);
            }
        }
    }

    public void hookMethod(Method method, HookAction callback) {
        try {
            if (method == null) {
                logE(tag, "method is null");
                return;
            }
            XposedBridge.hookMethod(method, callback);
            logI(tag, "Hook: " + method);
        } catch (Throwable e) {
            logE(tag, "Hook: " + method);
        }
    }

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            /*获取class*/
            if (parameterTypesAndCallback.length != 1) {
                Object[] newArray = new Object[parameterTypesAndCallback.length - 1];
                System.arraycopy(parameterTypesAndCallback, 0, newArray, 0, newArray.length);
                getDeclaredMethod(clazz, methodName, newArray);
                /*旧实现*/
                /*Class<?>[] classes = new Class<?>[newArray.length];
                Class<?> newclass = null;
                for (int i = 0; i < newArray.length; i++) {
                    Object type = newArray[i];
                    if (type instanceof Class) {
                        newclass = (Class<?>) newArray[i];
                    } else if (type instanceof String) {
                        newclass = findClassIfExists((String) type);
                        if (newclass == null) {
                            logE(tag, "class can't is null class:" + clazz + " method: " + methodName);
                            return;
                        }
                    }
                    classes[i] = newclass;
                }
                checkDeclaredMethod(clazz, methodName, classes);*/
            }
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            logI(tag, "Hook: " + clazz + " method: " + methodName);
        } catch (Throwable e) {
            logE(tag, "Not find method: " + methodName + " in: " + clazz);
        }
    }

    public void findAndHookMethod(String className, String methodName, Object... parameterTypesAndCallback) {
        findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
    }

    public void findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
            logI(tag, "Hook: " + clazz);
        } catch (Throwable f) {
            logE(tag, "findAndHookConstructor: " + f + " class: " + clazz);
        }
    }

    public void findAndHookConstructor(String className, Object... parameterTypesAndCallback) {
        findAndHookConstructor(findClassIfExists(className), parameterTypesAndCallback);
    }

    public void hookAllMethods(String className, String methodName, HookAction callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e);
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, HookAction callback) {
        try {
            int Num = XposedBridge.hookAllMethods(hookClass, methodName, callback).size();
            logI(tag, "Hook: " + hookClass + " methodName: " + methodName + " Num is: " + Num);
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e);
        }
    }

    public void hookAllConstructors(String className, HookAction callback) {
        Class<?> hookClass = findClassIfExists(className);
        if (hookClass != null) {
            hookAllConstructors(hookClass, callback);
        }
    }

    public void hookAllConstructors(Class<?> hookClass, HookAction callback) {
        try {
            XposedBridge.hookAllConstructors(hookClass, callback);
        } catch (Throwable f) {
            logE(tag, "hookAllConstructors: " + f + " class: " + hookClass);
        }
    }

    public void hookAllConstructors(String className, ClassLoader classLoader, HookAction callback) {
        Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
        if (hookClass != null) {
            hookAllConstructors(hookClass, callback);
        }
    }

    public Object callMethod(Object obj, String methodName, Object... args) throws Throwable {
        try {
            return XposedHelpers.callMethod(obj, methodName, args);
        } catch (Throwable e) {
            logE(tag, "callMethod: " + obj.toString() + " method: " + methodName + " args: " + Arrays.toString(args) + " e: " + e);
            throw new Throwable(tag + ": callMethod error");
        }
    }

    public Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    public Method getDeclaredMethod(String className, String method, Object... type) throws NoSuchMethodException {
        return getDeclaredMethod(findClassIfExists(className), method, type);
    }

    public Method getDeclaredMethod(Class<?> clazz, String method, Object... type) throws NoSuchMethodException {
//        String tag = "getDeclaredMethod";
        ArrayList<Method> haveMethod = new ArrayList<>();
        Method hqMethod = null;
        int methodNum;
        if (clazz == null) {
            logE(tag, "find class is null: " + method);
            throw new NoSuchMethodException("find class is null");
        }
        for (Method getMethod : clazz.getDeclaredMethods()) {
            if (getMethod.getName().equals(method)) {
                haveMethod.add(getMethod);
            }
        }
        if (haveMethod.isEmpty()) {
            logE(tag, "find method is null: " + method);
            throw new NoSuchMethodException("find method is null");
        }
        methodNum = haveMethod.size();
        if (type != null) {
            Class<?>[] classes = new Class<?>[type.length];
            Class<?> newclass = null;
            Object getType;
            for (int i = 0; i < type.length; i++) {
                getType = type[i];
                if (getType instanceof Class<?>) {
                    newclass = (Class<?>) getType;
                }
                if (getType instanceof String) {
                    newclass = findClassIfExists((String) getType);
                    if (newclass == null) {
                        logE(tag, "get class error: " + i);
                        throw new NoSuchMethodException("get class error");
                    }
                }
                classes[i] = newclass;
            }
            boolean noError = true;
            for (int i = 0; i < methodNum; i++) {
                hqMethod = haveMethod.get(i);
                boolean allHave = true;
                if (hqMethod.getParameterTypes().length != classes.length) {
                    if (methodNum - 1 == i) {
                        logE(tag, "class length bad: " + Arrays.toString(hqMethod.getParameterTypes()));
                        throw new NoSuchMethodException("class length bad");
                    } else {
                        noError = false;
                        continue;
                    }
                }
                for (int t = 0; t < hqMethod.getParameterTypes().length; t++) {
                    Class<?> getClass = hqMethod.getParameterTypes()[t];
                    if (!getClass.getSimpleName().equals(classes[t].getSimpleName())) {
                        allHave = false;
                        break;
                    }
                }
                if (!allHave) {
                    if (methodNum - 1 == i) {
                        logE(tag, "type bad: " + Arrays.toString(hqMethod.getParameterTypes())
                                + " input: " + Arrays.toString(classes));
                        throw new NoSuchMethodException("type bad");
                    } else {
                        noError = false;
                        continue;
                    }
                }
                if (noError) {
                    break;
                }
            }
            return hqMethod;
        } else {
            if (methodNum > 1) {
                logE(tag, "no type method must only have one: " + haveMethod);
                throw new NoSuchMethodException("no type method must only have one");
            }
        }
        return haveMethod.get(0);
    }

    public void getDeclaredField(XC_MethodHook.MethodHookParam param, String iNeedString, Object iNeedTo) {
        if (param != null) {
            try {
                Field setString = param.thisObject.getClass().getDeclaredField(iNeedString);
                setString.setAccessible(true);
                try {
                    setString.set(param.thisObject, iNeedTo);
                    Object result = setString.get(param.thisObject);
                    checkLast("getDeclaredField", iNeedString, iNeedTo, result);
                } catch (IllegalAccessException e) {
                    logE(tag, "IllegalAccessException to: " + iNeedString + " Need to: " + iNeedTo + " :" + e);
                }
            } catch (NoSuchFieldException e) {
                logE(tag, "No such the: " + iNeedString + " : " + e);
            }
        } else {
            logE(tag, "Param is null Code: " + iNeedString + " & " + iNeedTo);
        }
    }

    public void checkLast(String setObject, Object fieldName, Object value, Object last) {
        if (value != null && last != null) {
            if (value == last || value.equals(last)) {
                logSI(tag, setObject + " Success! set " + fieldName + " to " + value);
            } else {
                logSE(tag, setObject + " Failed! set " + fieldName + " to " + value + " hope: " + value + " but: " + last);
            }
        } else {
            logSE(tag, setObject + " Error value: " + value + " or last: " + last + " is null");
        }
    }

    public Object getObjectField(Object obj, String fieldName) throws Throwable {
        try {
            return XposedHelpers.getObjectField(obj, fieldName);
        } catch (Throwable e) {
            logE(tag, "getObject: " + obj.toString() + " field: " + fieldName);
            throw new Throwable(tag + ": getObject error");
        }
    }

    public void setInt(Object obj, String fieldName, int value) {
        checkAndHookField(obj, fieldName,
                () -> XposedHelpers.setIntField(obj, fieldName, value),
                () -> checkLast("setInt", fieldName, value,
                        XposedHelpers.getIntField(obj, fieldName)));
    }

    public void setBoolean(Object obj, String fieldName, boolean value) {
        checkAndHookField(obj, fieldName,
                () -> XposedHelpers.setBooleanField(obj, fieldName, value),
                () -> checkLast("setBoolean", fieldName, value,
                        XposedHelpers.getBooleanField(obj, fieldName)));
    }

    public void setObject(Object obj, String fieldName, Object value) {
        checkAndHookField(obj, fieldName,
                () -> XposedHelpers.setObjectField(obj, fieldName, value),
                () -> checkLast("setObject", fieldName, value,
                        XposedHelpers.getObjectField(obj, fieldName)));
    }

    public void checkDeclaredMethod(String className, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> hookClass = findClassIfExists(className);
        if (hookClass != null) {
            hookClass.getDeclaredMethod(name, parameterTypes);
            return;
        }
        throw new NoSuchMethodException();
    }

    public void checkDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        if (clazz != null) {
            clazz.getDeclaredMethod(name, parameterTypes);
            return;
        }
        throw new NoSuchMethodException();
    }

    public void checkAndHookField(Object obj, String fieldName, Runnable setField, Runnable checkLast) {
        try {
            obj.getClass().getDeclaredField(fieldName);
            setField.run();
            checkLast.run();
        } catch (Throwable e) {
            logE(tag, "No such field: " + fieldName + " in param: " + obj + " : " + e);
        }
    }

    public static Context findContext() {
        Context context;
        try {
            context = (Application) XposedHelpers.callStaticMethod(XposedHelpers.findClass(
                            "android.app.ActivityThread", null),
                    "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread",
                                null),
                        "currentActivityThread");
                if (currentActivityThread != null)
                    context = (Context) XposedHelpers.callMethod(currentActivityThread,
                            "getSystemContext");
            }
            return context;
        } catch (Throwable ignore) {
        }
        return null;
    }
}

