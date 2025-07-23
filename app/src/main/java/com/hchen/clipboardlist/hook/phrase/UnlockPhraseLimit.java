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
package com.hchen.clipboardlist.hook.phrase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.hchen.dexkitcache.DexkitCache;
import com.hchen.dexkitcache.IDexkit;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.base.BaseData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 解除常用语 20 条限制和字数限制
 *
 * @author 焕晨HChen
 */
public class UnlockPhraseLimit extends HCBase {

    @Override
    public void init() {
        // 解除 20 条限制
        Class<?> InputMethodUtil = findClass("com.miui.inputmethod.InputMethodUtil");
        setStaticField(InputMethodUtil, "sPhraseListSize", 0);
        hookMethod(InputMethodUtil,
            "queryPhrase",
            Context.class,
            new IHook() {
                @Override
                public void after() {
                    setStaticField(InputMethodUtil, "sPhraseListSize", 0);
                    observeCall();
                }
            }
        );

        Class<?> AddPhraseActivity = findClass("com.miui.phrase.AddPhraseActivity");
        hookMethod("com.miui.phrase.PhraseEditActivity",
            "onClick",
            View.class,
            new IHook() {
                @Override
                public void before() {
                    Activity activity = (Activity) thisObject();
                    Intent intent = new Intent(activity, AddPhraseActivity);
                    intent.setAction("com.miui.intent.action.PHRASE_ADD");
                    activity.startActivityForResult(intent, 0);
                    returnNull();
                }
            }
        );

        // 解除字数限制
        Method method = DexkitCache.findMember("phrase$1", new IDexkit() {
            @NonNull
            @Override
            public BaseData dexkit(@NonNull DexKitBridge bridge) throws ReflectiveOperationException {
                return bridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("phrase_list")
                        )
                        .usingStrings("layout_inflater")
                    )
                ).single();
            }
        });

        Field field = DexkitCache.findMember("phrase$2", new IDexkit() {
            @NonNull
            @Override
            public BaseData dexkit(@NonNull DexKitBridge bridge) throws ReflectiveOperationException {
                return bridge.findField(FindField.create()
                    .matcher(FieldMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("phrase_list")
                        )
                        .type(EditText.class)
                    )
                ).single();
            }
        });

        hook(method, new IHook() {
                @Override
                public void after() {
                    EditText editText = (EditText) getField(thisObject(), field);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.MAX_VALUE)});
                }
            }
        );
    }
}
