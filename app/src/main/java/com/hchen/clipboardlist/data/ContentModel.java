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
package com.hchen.clipboardlist.data;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hchen.hooktool.log.XposedLog;
import com.hchen.hooktool.tool.CoreTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * 剪贴板数据
 *
 * @author 焕晨HChen
 */
public class ContentModel {
    public static final String TAG = "ClipboardContentModel";
    public static final String PAD = "pad";
    public static final String PC = "pc";
    public static final String PHONE = "phone";
    private String content;
    private String determineContent;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private boolean isAcrossDevices;
    private boolean isShow = true;
    private boolean isTemp;
    private long time;
    private int type;

    public static ArrayList<ContentModel> cloneContentModel(Object model) {
        JSONObject jsonObject = (JSONObject) CoreTool.callMethod(model, "toJSONObject");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);
        return cloneContentModel(jsonArray.toString());
    }

    public static ArrayList<ContentModel> cloneContentModel(String content) {
        if (content == null || content.isEmpty()) return null;

        LinkedHashSet<ContentModel> linkedHashSet = new LinkedHashSet<>();
        try {
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    linkedHashSet.add(fromJSONObject(jsonObject));
                }
            }
        } catch (JSONException e) {
            XposedLog.logE(TAG, e);
        }
        return new ArrayList<>(linkedHashSet);
    }

    private static ContentModel fromJSONObject(JSONObject jSONObject) {
        ContentModel contentModel = new ContentModel();
        contentModel.setContent(jSONObject.optString("content"));
        contentModel.setType(jSONObject.optInt("type"));
        contentModel.setTime(jSONObject.optLong("time"));
        contentModel.setDeviceType(jSONObject.optString("deviceType"));
        contentModel.setDeviceId(jSONObject.optString("deviceId"));
        contentModel.setAcrossDevices(jSONObject.optBoolean("isAcrossDevices"));
        contentModel.setDeviceName(jSONObject.optString("deviceName"));
        contentModel.setIsShow(jSONObject.optBoolean("isShow"));
        contentModel.setTemp(jSONObject.optBoolean("isTemp"));
        return contentModel;
    }

    public String getContent() {
        return content;
    }

    public String getDetermineContent() {
        return determineContent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public boolean getIsShow() {
        return isShow;
    }

    public long getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public boolean isAcrossDevices() {
        return isAcrossDevices;
    }

    public boolean isTemp() {
        return isTemp;
    }


    public void setAcrossDevices(boolean enable) {
        isAcrossDevices = enable;
    }

    public void setContent(String content) {
        this.content = content;
        setDetermineContent(content);
    }

    public void setDetermineContent(String content) {
        if (TextUtils.isEmpty(content)) return;

        try {
            JSONArray jSONArray = new JSONArray(content);
            JSONArray jSONArray2 = new JSONArray();
            if (jSONArray.length() == 0) {
                return;
            }
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = new JSONObject(jSONArray.get(i).toString());
                jSONObject.remove("thumbImage");
                jSONObject.remove("fileUri");
                jSONArray2.put(jSONObject);
            }
            determineContent = jSONArray2.toString();
        } catch (Exception e) {
            Log.e(TAG, "ClipboardContentModel: setDetermineContent Exception!", e);
        }
    }

    public void setDeviceId(String id) {
        deviceId = id;
    }

    public void setDeviceName(String name) {
        deviceName = name;
    }

    public void setDeviceType(String type) {
        deviceType = type;
    }

    public void setIsShow(boolean show) {
        isShow = show;
    }

    public void setTemp(boolean temp) {
        isTemp = temp;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "ContentModel[content=" + content + ", determineContent=" + determineContent +
            ", deviceId=" + deviceId + ", deviceName=" + deviceName + ", deviceType=" + deviceType +
            ", isAcrossDevices=" + isAcrossDevices + ", isShow=" + isShow +
            ", isTemp=" + isTemp + ", time=" + time + ", type=" + type + "]";
    }

    public JSONObject toJSONObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("content", content);
            jSONObject.put("type", type);
            jSONObject.put("time", time);
            jSONObject.put("deviceType", deviceType);
            jSONObject.put("deviceId", deviceId);
            jSONObject.put("isAcrossDevices", isAcrossDevices);
            jSONObject.put("deviceName", deviceName);
            jSONObject.put("determineContent", determineContent);
            jSONObject.put("isShow", isShow);
            jSONObject.put("isTemp", isTemp);
        } catch (JSONException e7) {
            Log.e(TAG, "toJSONObject: JSONException!", e7);
        }
        return jSONObject;
    }
}
