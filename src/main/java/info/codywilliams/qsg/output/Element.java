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

public abstract class Element implements Outputs{
    final protected HashMap<String, String> attributes;
    final protected HashSet<String> classes;
    final protected LinkedList<Element> children;
    protected String id;
    protected String title;
    final protected String tagName;

    public Element(String tagName) {
        this.tagName = tagName;
        attributes = new HashMap<>();
        classes = new HashSet<>();
        children = new LinkedList<>();
    }

    public Element(String tagName, Element... elements) {
        this(tagName);
        addChildren(elements);
    }

    public Element(String tagName, Collection<Element> elements) {
        this(tagName);
        addChildren(elements);
    }

    /**
     * Adds the child elements to the end of the children of this element
     *
     * @param elements Child elements to add to this element
     */
    public void addChildren(Element... elements) {
        children.addAll(Arrays.asList(elements));
    }

    public void addChildren(Collection<Element> elements) {
        children.addAll(elements);
    }

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

    /**
     * Gets an unmodifiable list of child elements
     *
     * @return Unmodifiable List of Element of Children
     */
    List<Element> getChildren() {
        return Collections.unmodifiableList(children);
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

    protected String openHtmlTag() {
        StringBuilder stringBuilder = new StringBuilder("<");
        stringBuilder.append(tagName);

        createClassesString(classes, stringBuilder);
        createAttributeString(attributes, stringBuilder);

        stringBuilder.append(">");
        return stringBuilder.toString();
    }

    protected String closeHtmlTag() {
        return "</" + tagName + ">";
    }

    @Override
    public String toHtml() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(openHtmlTag());
        for (Element child: children) {
            stringBuilder.append(child.toHtml());
        }
        stringBuilder.append(closeHtmlTag());

        return stringBuilder.toString();
    }

    protected static void createClassesString(Set<String> classes, StringBuilder stringBuilder) {
        if(!classes.isEmpty()) {
            stringBuilder.append(" class=\"").append(String.join(" ", classes)).append('"');
        }
    }

    protected static void createAttributeString(Map<String, String> attributes, StringBuilder stringBuilder) {
        if(!attributes.isEmpty()) {
            for(Map.Entry<String, String> attribute: attributes.entrySet())
                stringBuilder.append(' ').append(attribute.getKey()).append("=\"").append(attribute.getValue()).append('"');
        }
    }
}
