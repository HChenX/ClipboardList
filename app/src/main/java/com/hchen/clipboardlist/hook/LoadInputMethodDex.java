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
package com.hchen.clipboardlist.hook;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IAction;

import java.util.Arrays;

/**
 * 获取常用语的 classloader。
 *
 * @author 焕晨HChen
 */
public class LoadInputMethodDex extends BaseHC {
    private final OnInputMethodDexLoad[] mOnInputMethodDexLoad;
    private boolean isHooked;

    public LoadInputMethodDex(OnInputMethodDexLoad... dexLoads) {
        mOnInputMethodDexLoad = dexLoads;
    }

    @Override
    public void init() {
        hookMethod("android.inputmethodservice.InputMethodModuleManager",
                "loadDex", ClassLoader.class, String.class,
                new IAction() {
                    @Override
                    public void after() {
                        if (isHooked) return;
                        Arrays.stream(mOnInputMethodDexLoad).forEach(load -> load.load(first()));
                        isHooked = true;
                    }
                }
        );
    }

    public interface OnInputMethodDexLoad {
        void load(ClassLoader classLoader);
    }
}
