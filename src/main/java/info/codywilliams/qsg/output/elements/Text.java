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
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Text extends Element {
    private String text;
    public static String SPAN = "span";
    public Text() {
        super(SPAN);
    }

    public Text(String text) {
        super(SPAN);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void addChildren(Element... elements) {
        System.err.println("Text cannot have children.");
    }

    @Override
    public void addChildren(Collection<Element> elements) {
        System.err.println("Text cannot have children.");
    }
    @Override
    public String toHtml() {
        if(id != null || title != null || !classes.isEmpty() || !attributes.isEmpty()) {
            return openHtmlTag() + text + closeHtmlTag();
        }
        return text;
    }

    @Override
    public String toWikitext() {
        return toHtml();
    }
}
