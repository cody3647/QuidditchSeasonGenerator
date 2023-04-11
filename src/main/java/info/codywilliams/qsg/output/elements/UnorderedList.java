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
import info.codywilliams.qsg.output.ElementChildren;

import java.util.Collection;

public class UnorderedList extends ElementChildren<UnorderedList.Item> {
    public static String UL = "ul";

    public UnorderedList(Item... elements) {
        super(elements);
    }

    public UnorderedList(Collection<Item> elements) {
        super(elements);
    }

    @Override
    public String getTagName() {
        return UL;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return true;
    }

    @Override
    public String toWikitext() {
        return toHtml(0);
    }

    public static class Item extends ElementChildren<Element> {
        public static String LI = "li";

        public Item(Element... elements) {
            super(elements);
        }

        public Item(Collection<Element> elements) {
            super(elements);
        }

        public Item(String text) {
            super(new Text(text));
        }

        @Override
        public String getTagName() {
            return LI;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return false;
        }

        @Override
        public String toWikitext() {
            return toHtml(0);
        }
    }
}
