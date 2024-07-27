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
package com.hchen.clipboardlist.data;

import com.hchen.hooktool.BaseHC;

public class ContentModel {
    public static BaseHC baseHC;
    public static ClassLoader classLoader;
    public String content;
    public long time;
    public int type;

    public ContentModel(String content, int type, long time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public static Object createContentModel(String content, int type, long time) {
        return baseHC.newInstance("com.miui.inputmethod.ClipboardContentModel", classLoader,
                content, type, time);
    }

    public static boolean putContent(Object data, String content) {
        return baseHC.setField(data, "content", content);
    }

    public static boolean putType(Object data, int type) {
        return baseHC.setField(data, "type", type);
    }

    public static boolean putTime(Object data, long time) {
        return baseHC.setField(data, "time", time);
    }

    public static String getContent(Object data) {
        return baseHC.getField(data, "content");
    }

    public static int getType(Object data) {
        return baseHC.getField(data, "type");
    }

    public static long getTime(Object data) {
        return baseHC.getField(data, "time");
    }
}
