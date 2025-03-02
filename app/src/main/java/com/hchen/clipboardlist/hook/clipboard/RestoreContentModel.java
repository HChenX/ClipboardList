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

import com.miui.inputmethod.ClipboardContentModel;

import org.json.JSONObject;

/**
 * 还原数据
 *
 * @author 焕晨HChen
 */
public class RestoreContentModel {
    public static ClipboardContentModel restore(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        return ClipboardContentModel.fromJSONObject(jsonObject);
    }
}
