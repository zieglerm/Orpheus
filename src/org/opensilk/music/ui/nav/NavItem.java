/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.ui.nav;

import org.opensilk.music.util.MarkedForRemoval;

/**
 * Created by drew on 7/15/14.
 */
@MarkedForRemoval
public class NavItem {

    public enum Type {
        HEADER,
        ITEM,
    }

    public final Type type;
    public final CharSequence title;
    public final Runnable action;

    public NavItem(Type type, CharSequence title, Runnable action) {
        this.type = type;
        this.title = title;
        this.action = action;
    }

}
