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
package com.miui.inputmethod;

import org.json.JSONObject;

public class ClipboardContentModel {
    public static final String PAD = "pad";
    public static final String PC = "pc";
    public static final String PHONE = "phone";
    public static final String TAG = "ClipboardContentModel";
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

    public ClipboardContentModel() {
        throw new RuntimeException("Stub!");
    }

    public ClipboardContentModel(String str) {
        throw new RuntimeException("Stub!");
    }

    public ClipboardContentModel(String str, int i7) {
        throw new RuntimeException("Stub!");
    }

    public ClipboardContentModel(String str, int i7, long j7) {
        throw new RuntimeException("Stub!");
    }

    public static ClipboardContentModel fromJSONObject(JSONObject jSONObject) {
        throw new RuntimeException("Stub!");
    }

    public boolean equals(Object obj) {
        throw new RuntimeException("Stub!");
    }

    public String getContent() {
        throw new RuntimeException("Stub!");
    }

    public String getDetermineContent() {
        throw new RuntimeException("Stub!");
    }

    public String getDeviceId() {
        throw new RuntimeException("Stub!");
    }

    public String getDeviceName() {
        throw new RuntimeException("Stub!");
    }

    public String getDeviceType() {
        throw new RuntimeException("Stub!");
    }

    public boolean getIsShow() {
        throw new RuntimeException("Stub!");
    }

    public long getTime() {
        throw new RuntimeException("Stub!");
    }

    public int getType() {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
        throw new RuntimeException("Stub!");
    }

    public boolean isAcrossDevices() {
        throw new RuntimeException("Stub!");
    }

    public boolean isTemp() {
        throw new RuntimeException("Stub!");
    }

    public void setAcrossDevices(boolean z6) {
        throw new RuntimeException("Stub!");
    }

    public void setContent(String str) {
        throw new RuntimeException("Stub!");
    }

    public void setDetermineContent(String str) {
        throw new RuntimeException("Stub!");
    }

    public void setDeviceId(String str) {
        throw new RuntimeException("Stub!");
    }

    public void setDeviceName(String str) {
        throw new RuntimeException("Stub!");
    }

    public void setDeviceType(String str) {
        throw new RuntimeException("Stub!");
    }

    public void setIsShow(boolean z6) {
        throw new RuntimeException("Stub!");
    }

    public void setTemp(boolean z6) {
        throw new RuntimeException("Stub!");
    }

    public void setTime(long j7) {
        throw new RuntimeException("Stub!");
    }

    public void setType(int i7) {
        throw new RuntimeException("Stub!");
    }

    public JSONObject toJSONObject() {
        throw new RuntimeException("Stub!");
    }
}
