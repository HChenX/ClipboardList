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
package com.hchen.clipboardlist.clipboard;

import static com.hchen.hooktool.log.XposedLog.logE;
import static com.hchen.hooktool.log.XposedLog.logI;
import static com.hchen.hooktool.log.XposedLog.logW;

import android.content.ClipData;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hchen.clipboardlist.data.ContentModel;
import com.hchen.clipboardlist.file.FileHelper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.callback.IAction;
import com.hchen.hooktool.tool.ParamTool;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class NewClipboardList extends BaseHC {
    private static String dataPath;
    private Gson gson;
    private boolean isHooked = false;
    private boolean isNew = false;

    @Override
    public void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        hook("android.inputmethodservice.InputMethodModuleManager",
                "loadDex", ClassLoader.class, String.class,
                new IAction() {
                    @Override
                    public void after() throws Throwable {
                        if (!isHooked) {
                            dataPath = lpparam.appInfo.dataDir + "/files/clipboard_data.dat";
                            logI(TAG, "class loader: " + first());

                            FileHelper.TAG = TAG;
                            if (!FileHelper.exists(dataPath)) {
                                logE(TAG, "file create failed!");
                                return;
                            }
                            oldMethod(first());
                            isHooked = true;
                        }
                    }
                });
    }

    private void oldMethod(ClassLoader classLoader) {
        ContentModel.baseHC = this;
        ContentModel.classLoader = classLoader;
        if (!existsClass("com.miui.inputmethod.MiuiClipboardManager", classLoader)) {
            chain("com.miui.inputmethod.InputMethodUtil", classLoader,
                    method("getClipboardData", Context.class)
                            .hook(new IAction() {
                                @Override
                                public void before() throws Throwable {
                                    getClipboardData(paramTool);
                                }
                            })

                            .method("addClipboard", String.class, String.class, int.class, Context.class)
                            .hook(new IAction() {
                                @Override
                                public void before() throws Throwable {
                                    addClipboard(second(), third(), fourth());
                                    returnNull();
                                }
                            })

                            .method("setClipboardModelList", Context.class, ArrayList.class)
                            .hook(new IAction() {
                                @Override
                                public void before() throws Throwable {
                                    ArrayList<?> dataList = second();
                                    FileHelper.write(dataPath, gson.toJson(dataList));
                                    if (!dataList.isEmpty()) returnNull();
                                }
                            })
            );
        } else newMethod(classLoader);
    }

    private String text = null;
    private int max = -1;

    private void newMethod(ClassLoader classLoader) {
        isNew = true;
        /*setStaticField("com.miui.inputmethod.MiuiClipboardManager", classLoader,
                "MAX_CLIP_CONTENT_SIZE", Integer.MAX_VALUE);*/

        chain("com.miui.inputmethod.MiuiClipboardManager", classLoader,
                method("addClipDataToPhrase", Context.class, InputMethodService.class,
                        "com.miui.inputmethod.ClipboardContentModel")
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                Object clipboardContentModel = third();
                                String content = ContentModel.getContent(clipboardContentModel);
                                int type = ContentModel.getType(clipboardContentModel);
                                // long time = ContentModel.getTime(clipboardContentModel);
                                addClipboard(content, type, first());
                                returnNull();
                            }
                        })

                        .method("getClipboardData", Context.class)
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                getClipboardData(paramTool);
                            }
                        })

                        .method("setClipboardModelList", Context.class, ArrayList.class)
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                ArrayList<?> dataList = second();
                                FileHelper.write(dataPath, gson.toJson(dataList));
                                if (!dataList.isEmpty()) returnNull();
                            }
                        })

                        .method("commitClipDataAndTrack", Context.class, InputMethodService.class,
                                "com.miui.inputmethod.ClipboardContentModel", int.class)
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                int type = fourth();
                                if (type == 3 || type == 2) {
                                    fourth(10);
                                }
                            }
                        })

                        .method("processSingleItemOfClipData", ClipData.class, String.class)
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                ClipData clipData = first();
                                ClipData.Item item = clipData.getItemAt(0);
                                if (item.getText() != null && !TextUtils.isEmpty(item.getText().toString())) {
                                    text = item.getText().toString();
                                }
                            }
                        })

                        .method("buildClipDataItemModelBasedTextData", String.class)
                        .hook(new IAction() {
                            @Override
                            public void before() throws Throwable {
                                if (max == -1)
                                    max = getStaticField("com.miui.inputmethod.MiuiClipboardManager", classLoader,
                                            "MAX_CLIP_CONTENT_SIZE");
                                if (max == -1) max = 5000;
                                String string = first();
                                if (string.length() == max) {
                                    if (text != null) first(text);
                                }
                                text = null;
                            }
                        })
        );

    }

    private void getClipboardData(ParamTool param) {
        ArrayList<ContentModel> readData = toContentModelList(FileHelper.read(dataPath));
        if (readData.isEmpty()) {
            String data = getData(param.first());
            if (data.isEmpty()) param.setResult(new ArrayList<>());
            ArrayList<ContentModel> contentModels = toContentModelList(data);
            FileHelper.write(dataPath, gson.toJson(contentModels));
            param.setResult(toClipboardList(contentModels));
            return;
        }
        ArrayList<?> clipboardList = toClipboardList(readData);
        param.setResult(clipboardList);
    }

    private void addClipboard(String add, int type, Context context) {
        if (FileHelper.empty(dataPath)) {
            // 数据库为空时写入数据
            String string = getData(context);
            ArrayList<ContentModel> dataList = new ArrayList<>();
            if (string.isEmpty())
                dataList = toContentModelList(string);
            if (dataList.isEmpty())
                dataList.add(0, new ContentModel(add, type, System.currentTimeMillis()));
            FileHelper.write(dataPath, gson.toJson(dataList));
            // param.setResult(toClipboardList(dataList));
            return;
        }
        ArrayList<ContentModel> readData = toContentModelList(FileHelper.read(dataPath));
        if (readData.isEmpty()) {
            logW(TAG, "can't read any data!");
        } else {
            if (readData.stream().anyMatch(contentModel -> contentModel.content.equals(add))) {
                readData.removeIf(contentModel -> contentModel.content.equals(add));
            }
            readData.add(0, new ContentModel(add, type, System.currentTimeMillis()));
            FileHelper.write(dataPath, gson.toJson(readData));
        }
        // param.setResult(toClipboardList(dataList));
    }

    private String getData(Context context) {
        Bundle call = context.getContentResolver().call(Uri.parse((!isNew) ? "content://com.miui.input.provider"
                        : "content://com.miui.phrase.input.provider"),
                "getClipboardList", null, new Bundle());
        return call != null ? call.getString("savedClipboard") : "";
    }

    private ArrayList<ContentModel> toContentModelList(String str) {
        if (str.isEmpty()) return new ArrayList<>();
        ArrayList<ContentModel> contentModels = gson.fromJson(str,
                new TypeToken<ArrayList<ContentModel>>() {
                }.getType());
        if (contentModels == null) return new ArrayList<>();
        return contentModels;
    }

    private ArrayList<?> toClipboardList(ArrayList<ContentModel> dataList) {
        return dataList.stream().map(list ->
                ContentModel.createContentModel(list.content, list.type, list.time)
        ).collect(Collectors.toCollection(ArrayList::new));
    }
}
