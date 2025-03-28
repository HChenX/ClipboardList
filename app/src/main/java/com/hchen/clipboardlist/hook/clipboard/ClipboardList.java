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

import android.content.ClipData;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hchen.clipboardlist.file.FileHelper;
import com.hchen.clipboardlist.hook.LoadInputMethodDex;
import com.hchen.clipboardlist.hook.clipboard.data.ContentModel;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCData;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.ParamTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dalvik.system.PathClassLoader;

/**
 * 解除常用语剪贴板时间限制，条数限制和字数限制
 *
 * @author 焕晨HChen
 */
public class ClipboardList extends BaseHC implements LoadInputMethodDex.OnInputMethodDexLoad {
    private static PathClassLoader mPathClassLoader;
    private static Gson mGson;
    private static String mDataPath;
    private static boolean isNewMode = false;
    private static String mText = null;
    private static Integer mMaxSize = -1;

    @Override
    protected void init() {
    }

    @Override
    public void load(ClassLoader classLoader) {
        mGson = new GsonBuilder().setPrettyPrinting().create();
        mDataPath = lpparam.appInfo.dataDir + "/files/clipboard_data.dat";
        logI(TAG, "class loader: " + classLoader);

        FileHelper.TAG = TAG;
        if (!FileHelper.exists(mDataPath)) {
            logE(TAG, "file create failed!");
            return;
        }

        mPathClassLoader = new PathClassLoader(HCData.getModulePath(), classLoader);
        if (isNewMethod(classLoader)) newHook(classLoader);
        else oldHook(classLoader);
    }

    @Override
    protected void onApplicationAfter(Context context) {
        context.getContentResolver().registerContentObserver(
            Uri.parse("content://com.miui.phrase.input.provider/across"),
            false,
            new ContentObserver(new Handler(context.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    if (selfChange || !isNewMode) return;
                    Bundle call = context.getContentResolver().call(
                        Uri.parse("content://com.miui.phrase.input.provider/get/across"),
                        "method_get_the_across_devices_data",
                        null, new Bundle()
                    );
                    if (call == null) return;

                    String string = call.getString("list");
                    if (string == null || string.isEmpty()) return;

                    ArrayList<ContentModel> contentModels = ContentModel.cloneContentModel(string);
                    addClipboard(context, contentModels.toArray(new ContentModel[0]));
                }
            }
        );
    }

    private boolean isNewMethod(ClassLoader classLoader) {
        isNewMode = existsClass("com.miui.inputmethod.MiuiClipboardManager", classLoader);
        return isNewMode;
    }

    private void newHook(ClassLoader classLoader) {
        setStaticField("com.miui.inputmethod.MiuiClipboardManager", classLoader, "MAX_CLIP_DATA_ITEM_SIZE", Integer.MAX_VALUE);
        chain("com.miui.inputmethod.MiuiClipboardManager", classLoader,
            anyMethod("addClipDataToPhrase").hook(new IHook() {
                    @Override
                    public void before() {
                        Object clipboardContentModel = getArgs(2);
                        ArrayList<ContentModel> cloneContentModel = ContentModel.cloneContentModel(clipboardContentModel);
                        addClipboard((Context) getArgs(0), cloneContentModel.toArray(new ContentModel[0]));
                        logI(TAG, "addClipDataToPhrase: " + cloneContentModel);
                        // returnNull();
                    }
                }) // 添加剪贴板条目

                .method("getClipboardData", Context.class).hook(new IHook() {
                    @Override
                    public void before() {
                        getClipboardData(this);
                    }
                }) // 获取剪贴板数据

                .anyMethod("setClipboardModelList").hook(new IHook() {
                    @Override
                    public void before() {
                        List<?> dataList = (List<?>) getArgs(1);
                        FileHelper.write(mDataPath, mGson.toJson(dataList));
                        if (!dataList.isEmpty()) returnNull();
                        logI(TAG, "saveClipboardModelList!!");
                    }
                }) // 保存剪贴板数据

                .methodIfExist("clearClipBoardData", Context.class).hook(new IHook() {
                    @Override
                    public void after() {
                        FileHelper.write(mDataPath, "[]");
                    }
                })

                .anyMethod("commitClipDataAndTrack").hook(new IHook() {
                    @Override
                    public void before() {
                        Integer type = (Integer) getArgs(3);
                        if (type == null) return;
                        if (type == 3 || type == 2) {
                            setArgs(3, 10);
                        }
                    }
                })  // 修复小米的 BUG

                .method("processSingleItemOfClipData", ClipData.class, String.class).hook(new IHook() {
                    @Override
                    public void before() {
                        ClipData clipData = (ClipData) getArgs(0);
                        ClipData.Item item = clipData.getItemAt(0);
                        if (item.getText() != null && !TextUtils.isEmpty(item.getText().toString())) {
                            mText = item.getText().toString();
                        }
                    }
                }) // 解除 5000 字限制

                .method("buildClipDataItemModelBasedTextData", String.class).hook(new IHook() {
                    @Override
                    public void before() {
                        if (mMaxSize == -1) {
                            mMaxSize = (Integer) getStaticField(
                                "com.miui.inputmethod.MiuiClipboardManager",
                                classLoader,
                                "MAX_CLIP_CONTENT_SIZE"
                            );

                            if (mMaxSize == null)
                                mMaxSize = 5000;
                        }

                        String string = (String) getArgs(0);
                        if (string.length() == mMaxSize) {
                            if (mText != null) setArgs(0, mText);
                        }
                        mText = null;
                    }
                }) // 解除 5000 字限制
        );

        if (existsMethod("com.miui.inputmethod.InputMethodClipboardPhrasePopupView", classLoader, "setRemoteDataToView")) {
            hookMethod("com.miui.inputmethod.InputMethodClipboardPhrasePopupView",
                classLoader,
                "setRemoteDataToView",
                new IHook() {
                    @Override
                    public void before() {
                        returnNull();
                    }
                }
            );
        }
    }

    private void oldHook(ClassLoader classLoader) {
        chain("com.miui.inputmethod.InputMethodUtil", classLoader,
            method("getClipboardData", Context.class).hook(new IHook() {
                    @Override
                    public void before() {
                        getClipboardData(this);
                    }
                }) // 读取剪贴板数据

                .method("addClipboard", String.class, String.class, int.class, Context.class).hook(new IHook() {
                    @Override
                    public void before() {
                        String content = (String) getArgs(1);
                        int type = (int) getArgs(2);
                        ContentModel contentModel = new ContentModel();
                        contentModel.setContent(content);
                        contentModel.setType(type);
                        contentModel.setTime(System.currentTimeMillis());

                        addClipboard((Context) getArgs(3), contentModel);
                        // addClipboard((String) getArgs(1), (Integer) getArgs(2), (Context) getArgs(3));
                        returnNull();
                    }
                }) // 添加剪贴板条目

                .method("setClipboardModelList", Context.class, ArrayList.class).hook(new IHook() {
                    @Override
                    public void before() {
                        ArrayList<?> dataList = (ArrayList<?>) getArgs(1);
                        FileHelper.write(mDataPath, mGson.toJson(dataList));
                        if (!dataList.isEmpty()) returnNull();
                    }
                }) // 保存剪贴板数据
        );
    }

    private void getClipboardData(ParamTool param) {
        ArrayList<ContentModel> readData = toContentModelList(FileHelper.read(mDataPath));
        if (readData.isEmpty()) {
            String data = getData((Context) param.getArgs(0));
            if (data == null || data.isEmpty()) {
                param.setResult(new ArrayList<>());
                return;
            }
            ArrayList<ContentModel> contentModels = toContentModelList(data);
            FileHelper.write(mDataPath, mGson.toJson(contentModels));
            param.setResult(toClipboardList(contentModels));
            return;
        }
        ArrayList<?> clipboardList = toClipboardList(readData);
        param.setResult(clipboardList);
    }

    private void addClipboard(Context context, ContentModel... contentModel) {
        if (FileHelper.isEmpty(mDataPath)) {
            // 数据库为空时写入数据
            String string = getData(context);
            ArrayList<ContentModel> dataList;
            dataList = toContentModelList(string);
            dataList.addAll(0, Arrays.asList(contentModel));
            FileHelper.write(mDataPath, mGson.toJson(dataList));
            return;
        }
        ArrayList<ContentModel> readData = toContentModelList(FileHelper.read(mDataPath));
        if (readData.isEmpty()) {
            logW(TAG, "can't read any data!");
        } else {
            HashSet<String> contenHashSet = new HashSet<>();
            Arrays.stream(contentModel).forEach(model -> contenHashSet.add(model.getContent()));
            readData.removeIf(model -> contenHashSet.contains(model.getContent()));

            readData.addAll(0, Arrays.asList(contentModel));
            FileHelper.write(mDataPath, mGson.toJson(readData));
        }
    }

    private String getData(Context context) {
        Bundle call = context.getContentResolver().call(
            Uri.parse(isNewMode ? "content://com.miui.phrase.input.provider" : "content://com.miui.input.provider"),
            "getClipboardList",
            null, new Bundle()
        );
        return call != null ? call.getString("savedClipboard") : "";
    }

    private ArrayList<ContentModel> toContentModelList(String str) {
        if (str.isEmpty()) return new ArrayList<>();
        ArrayList<ContentModel> contentModels = mGson.fromJson(str,
            new TypeToken<ArrayList<ContentModel>>() {
            }.getType());
        if (contentModels == null) return new ArrayList<>();
        return contentModels;
    }

    private ArrayList<?> toClipboardList(ArrayList<ContentModel> dataList) {
        return dataList.stream()
            .map(this::restoreContentModel)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private Object restoreContentModel(ContentModel contentModel) {
        if (contentModel == null) return null;

        return callStaticMethod(
            "com.hchen.clipboardlist.hook.clipboard.RestoreContentModel",
            mPathClassLoader,
            "restore",
            contentModel.toJSONObject());
    }
}
