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

public class Table extends Element{
    public static String TABLE = "table";
    public Table() {
        super(TABLE);
    }

    public Table(Element... elements) {
        super(TABLE, elements);
    }

    public Table(Collection<Element> elements) {
        super(TABLE, elements);
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("\n{|");

        createClassesString(classes, stringBuilder);
        createAttributeString(attributes, stringBuilder);

        for(Element element: children) {
            stringBuilder.append(element.toWikitext());
        }

        stringBuilder.append("\n|}");

        return stringBuilder.toString();
    }
}
