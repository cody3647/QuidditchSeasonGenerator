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

package info.codywilliams.qsg.output;

import java.util.*;

public abstract class Element implements ElementOutputs {
    final protected HashMap<String, String> attributes;
    final protected HashSet<String> classes;
    protected String id;
    protected String title;

    public Element() {
        attributes = new HashMap<>();
        classes = new HashSet<>();
    }

    protected static void createClassesString(Set<String> classes, StringBuilder stringBuilder) {
        if (!classes.isEmpty()) {
            stringBuilder.append(" class=\"").append(String.join(" ", classes)).append('"');
        }
    }

    protected static void createAttributeString(Map<String, String> attributes, StringBuilder stringBuilder) {
        if (!attributes.isEmpty()) {
            for (Map.Entry<String, String> attribute : attributes.entrySet())
                stringBuilder.append(' ').append(attribute.getKey()).append("=\"").append(attribute.getValue()).append('"');
        }
    }

    abstract public String getTagName();

    abstract public boolean isTagClosedOnNewLine();

    /**
     * Add attributes to the element
     *
     * @param name  String name of attribute
     * @param value String value of attribute
     */
    public void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * Adds css class names to the element
     *
     * @param names Class names
     */
    public void addClass(String... names) {
        classes.addAll(Arrays.asList(names));
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public HashSet<String> getClasses() {
        return classes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        attributes.put("id", id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        attributes.put("title", title);
    }

    protected StringBuilder openHtmlTag(StringBuilder stringBuilder, int tabs) {
        appendNewLineAndTabs(stringBuilder, tabs);
        stringBuilder.append("<").append(getTagName());

        createClassesString(classes, stringBuilder);
        createAttributeString(attributes, stringBuilder);

        stringBuilder.append(">");

        return stringBuilder;
    }

    protected StringBuilder closeHtmlTag(StringBuilder stringBuilder, int tabs) {
        if (isTagClosedOnNewLine())
            appendNewLineAndTabs(stringBuilder, tabs);
        stringBuilder.append("</").append(getTagName()).append(">");

        return stringBuilder;
    }

    protected void appendNewLineAndTabs(StringBuilder stringBuilder, int tabs) {
        if (tabs == 0)
            return;

        stringBuilder.append('\n');
        for (; tabs > 0; tabs--) {
            stringBuilder.append('\t');
        }
    }
}
