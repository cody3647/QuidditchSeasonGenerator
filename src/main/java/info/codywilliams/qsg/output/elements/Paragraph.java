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

import info.codywilliams.qsg.output.ElementChildren;
import info.codywilliams.qsg.output.InlineElement;

import java.util.Collection;

public class Paragraph extends ElementChildren<InlineElement> {
    public static String P = "p";

    public Paragraph(InlineElement... elements) {
        super(elements);
    }

    public Paragraph(Collection<InlineElement> elements) {
        super(elements);
    }

    public Paragraph(String text) {
        super(new Text(text));
    }

    @Override
    public String getTagName() {
        return P;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("\n\n");

        for (InlineElement child : children) {
            stringBuilder.append(child.toWikitext());
        }

        return stringBuilder.toString();
    }
}
