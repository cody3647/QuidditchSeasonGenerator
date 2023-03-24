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
    public Text() {
        super(null);
    }

    public Text(String text) {
        super(null);
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
    public void addAttribute(String name, String value) {
        System.err.println("Text cannot have attributes.");
    }

    @Override
    public void addClass(String... names) {
        System.err.println("Text cannot have classes.");
    }

    @Override
    public HashMap<String, String> getAttributes() {
        System.err.println("Text does not have attributes.");
        return super.getAttributes();
    }

    @Override
    public ArrayList<String> getClasses() {
        System.err.println("Text does not have classes.");
        return super.getClasses();
    }

    @Override
    public String getId() {
        System.err.println("Text does not have an id.");
        return super.getId();
    }

    @Override
    public void setId(String id) {
        System.err.println("Text cannot have an id.");
    }

    @Override
    public String getTitle() {
        System.err.println("Text does not have a title.");
        return null;
    }

    @Override
    public void setTitle(String title) {
        System.err.println("Text cannot have a title.");
    }

    @Override
    public String toHtml() {
        return text;
    }

}
