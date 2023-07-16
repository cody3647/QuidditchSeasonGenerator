/*
 * Quidditch Season Generator
 * Copyright (C) 2023.  Cody Williams
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.qsg.output.elements;

import info.codywilliams.qsg.output.Element;
import info.codywilliams.qsg.output.InlineElement;

public class Text extends Element implements InlineElement {
    public static String SPAN = "span";
    private String text;

    public Text(String text) {
        this.text = text;
    }

    @Override
    public String getTagName() {
        return SPAN;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toHtml(int tabs) {
        if (id != null || title != null || !classes.isEmpty() || !attributes.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            openHtmlTag(stringBuilder, 0).append(text);
            return closeHtmlTag(stringBuilder, 0).toString();
        }
        return text;
    }

    @Override
    public String toWikitext() {
        return toHtml(0);
    }

}
