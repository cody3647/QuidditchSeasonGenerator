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

public abstract class ElementChildren<T extends ElementOutputs> extends Element {
    final protected LinkedList<T> children;

    public ElementChildren() {
        children = new LinkedList<>();
    }

    public ElementChildren(T... elements) {
        this();
        addChildren(elements);
    }

    public ElementChildren(Collection<T> elements) {
        this();
        addChildren(elements);
    }

    /**
     * Adds the child elements to the end of the children of this element
     *
     * @param elements Child elements to add to this element
     */
    public void addChildren(T... elements) {
        children.addAll(Arrays.asList(elements));
    }

    public void addChildren(Collection<T> elements) {
        children.addAll(elements);
    }

    /**
     * Gets an unmodifiable list of child elements
     *
     * @return Unmodifiable List of Element of Children
     */
    List<T> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public String toHtml(int tabs) {
        StringBuilder stringBuilder = new StringBuilder();

        openHtmlTag(stringBuilder, tabs);
        for (T child : children) {
            stringBuilder.append(child.toHtml(tabs + 1));
        }
        closeHtmlTag(stringBuilder, tabs);

        return stringBuilder.toString();
    }

    public String toWikitextHtml() {
        StringBuilder stringBuilder = new StringBuilder();
        openHtmlTag(stringBuilder, 0);

        for (T child : children) {
            stringBuilder.append(child.toWikitext());
        }

        closeHtmlTag(stringBuilder, 0);
        return stringBuilder.toString();
    }
}
