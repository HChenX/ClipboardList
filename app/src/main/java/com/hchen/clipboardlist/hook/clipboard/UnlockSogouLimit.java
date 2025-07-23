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
package com.hchen.clipboardlist.hook.clipboard;

import androidx.annotation.NonNull;

import com.hchen.dexkitcache.DexkitCache;
import com.hchen.dexkitcache.IDexkit;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.base.BaseData;

import java.lang.reflect.Method;

/**
 * 解除搜狗输入法 150 条限制
 *
 * @author 焕晨HChen
 */
public class UnlockSogouLimit extends HCBase {
    public boolean isClipboard;

    @Override
    protected void init() {
        Method method = DexkitCache.findMember("Sogou$1", new IDexkit() {
            @NonNull
            @Override
            public BaseData dexkit(@NonNull DexKitBridge bridge) throws ReflectiveOperationException {
                return bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("sogou_clipboard_tmp"))
                        .usingNumbers("com.sohu.inputmethod.sogou.xiaomi".equals(loadPackageParam.packageName) ? 150 : 80064)
                    )).single();
            }
        });

        hook(method, new IHook() {
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
                    if (isClipboard)
                        setResult(null);
                }
            }
        );
    }
}
