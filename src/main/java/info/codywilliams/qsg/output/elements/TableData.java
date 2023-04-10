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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableData extends Element{
    public static String TD = "td";
    public TableData() {
        super(TD);
    }
    public TableData(Element... elements) {
        super(TD, elements);
    }

    public TableData(Collection<Element> elements) {
        super(TD, elements);
    }

    public TableData(String text) {
        super(TD, new Text(text));
    }

    @Override
    public String toWikitext() {
        return createWikiCellString(classes, attributes, children, '|');
    }

    private static String createWikiCellString(Set<String> classes, Map<String, String> attributes,
                                               List<Element> children, char firstChar) {
        StringBuilder stringBuilder = new StringBuilder("\n").append(firstChar);

        if(!classes.isEmpty() || !attributes.isEmpty()) {
            createClassesString(classes, stringBuilder);
            createAttributeString(attributes, stringBuilder);
            stringBuilder.append("| ");
        }

        for(Element child: children) {
            stringBuilder.append(child.toWikitext());
        }

        return stringBuilder.toString();
    }

    static public class HeaderCell extends Element {
        public static String TH = "th";
        public HeaderCell() {
            super(TH);
        }

        public HeaderCell(Element element) {
            super(TH, element);
        }

        public HeaderCell(Element... elements) {
            super(TH, elements);
        }

        public HeaderCell(Collection<Element> elements) {
            super(TH, elements);
        }

        public HeaderCell(String text) {
            super(TH, new Text(text));
        }

        @Override
        public String toWikitext() {
            return createWikiCellString(classes, attributes, children,'!');
        }
    }
}
