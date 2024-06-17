package com.hchen.clipboardlist.data;

import com.hchen.hooktool.tool.ClassTool;
import com.hchen.hooktool.tool.ExpandTool;

public class ContentModel {
    public static ClassTool classTool;
    public static ExpandTool expandTool;
    public String content;
    public long time;
    public int type;

    public ContentModel(String content, int type, long time) {
        this.content = content;
        this.type = type;
        this.time = time;
    }

    public static Object createContentModel(String content, int type, long time) {
        return classTool.newInstance("ccm", new Object[]{content, type, time});
    }

    public static boolean putContent(Object data, String content) {
        return expandTool.setField(data, "content", content);
    }

    public static boolean putType(Object data, int type) {
        return expandTool.setField(data, "type", type);
    }

    public static boolean putTime(Object data, long time) {
        return expandTool.setField(data, "time", time);
    }

    public static String getContent(Object data) {
        return expandTool.getField(data, "content");
    }

    public static int getType(Object data) {
        return expandTool.getField(data, "type");
    }

    public static long getTime(Object data) {
        return expandTool.getField(data, "time");
    }
}
