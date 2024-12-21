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
package com.hchen.clipboardlist.hook.clipboard;

import static com.hchen.hooktool.log.XposedLog.logE;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

import java.lang.reflect.Method;

/**
 * 解除搜狗输入法 150 条限制
 *
 * @author 焕晨HChen
 */
public class SoGouClipboard extends BaseHC {
    public boolean isClipboard;
    public DexKitBridge mDexKitBridge;

    public SoGouClipboard(DexKitBridge dexKitBridge) {
        mDexKitBridge = dexKitBridge;
    }

    @Override
    protected void init() {
        Method method = null;
        try {
            method = mDexKitBridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .declaredClass(ClassMatcher.create()
                                    .usingStrings("sogou_clipboard_tmp"))
                            .usingNumbers("com.sohu.inputmethod.sogou.xiaomi".equals(lpparam.packageName) ? 150 : 80064)
                    )).singleOrNull().getMethodInstance(classLoader);
        } catch (NoSuchMethodException e) {
            logE(TAG, e);
            return;
        }

        hook(method,
                new IHook() {
                    @Override
                    public void before() {
                        isClipboard = true;
                    }

                    @Override
                    public void after() {
                        isClipboard = false;
                    }
                }
        );
        hookMethod("org.greenrobot.greendao.query.QueryBuilder",
                "list",
                new IHook() {
                    @Override
                    public void before() {
                        if (isClipboard) {
                            setResult(null);
                        }
                    }
                }
        );
    }
}
