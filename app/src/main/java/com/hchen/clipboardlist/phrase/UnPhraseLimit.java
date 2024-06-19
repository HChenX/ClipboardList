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
package com.hchen.clipboardlist.phrase;

import static com.hchen.hooktool.log.XposedLog.logE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.tool.ParamTool;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;

public class UnPhraseLimit extends BaseHC {
    private final DexKitBridge dexKitBridge;

    public UnPhraseLimit(DexKitBridge dexKitBridge) {
        this.dexKitBridge = dexKitBridge;
    }

    @Override
    public void init() {
        try {
            // 解除 20 条限制
            Class<?> InputMethodUtil = findClass("com.miui.inputmethod.InputMethodUtil");
            setStaticField(InputMethodUtil, "sPhraseListSize", 0);
            classTool.add("imu", InputMethodUtil)
                    .getMethod("queryPhrase", Context.class)
                    .hook(new IAction() {
                        @Override
                        public void after(ParamTool param) throws Throwable {
                            setStaticField(InputMethodUtil, "sPhraseListSize", 0);
                        }
                    });

            Class<?> AddPhraseActivity = findClass("com.miui.phrase.AddPhraseActivity");
            classTool.findClass("pea", "com.miui.phrase.PhraseEditActivity")
                    .getMethod("onClick", View.class)
                    .hook(new IAction() {
                        @Override
                        public void before(ParamTool param) throws Throwable {
                            Activity activity = param.thisObject();
                            View view = param.first();
                            int id = activity.getResources().getIdentifier("fab", "id", "com.miui.phrase");
                            if (view.getId() == id) {
                                Intent intent = new Intent(activity, AddPhraseActivity);
                                intent.setAction("com.miui.intent.action.PHRASE_ADD");
                                activity.startActivityForResult(intent, 0);
                                param.setResult(null);
                            }
                        }
                    });

            // 解除字数限制
            MethodData methodData1 = dexKitBridge.findMethod(FindMethod.create()
                    .matcher(MethodMatcher.create()
                            .declaredClass(ClassMatcher.create()
                                    .usingStrings("phrase_list")
                            )
                            .usingStrings("layout_inflater")
                    )
            ).singleOrThrow(() -> new RuntimeException("method is null!!"));
            FieldData fieldData = dexKitBridge.findField(FindField.create()
                    .matcher(FieldMatcher.create()
                            .declaredClass(ClassMatcher.create()
                                    .usingStrings("phrase_list")
                            )
                            .type(EditText.class)
                    )
            ).singleOrThrow(() -> new RuntimeException("field is null!!"));
            Field f = fieldData.getFieldInstance(lpparam.classLoader);
            hook(methodData1.getMethodInstance(lpparam.classLoader), new IAction() {
                @Override
                public void after(ParamTool param) throws Throwable {
                    EditText editText = (EditText) f.get(param.thisObject());
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Integer.MAX_VALUE)});
                }
            });
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            logE(TAG, e);
        }
    }
}
