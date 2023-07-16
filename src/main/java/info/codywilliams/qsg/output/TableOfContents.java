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

public class TableOfContents extends Element implements ElementOutputs {
    /**
     * Returns the element as HTML string
     *
     * @param tabs
     * @return HTML String
     */
    @Override
    public String toHtml(int tabs) {
        return null;
    }

    @Override
    public String toWikitext() {
        return "\n{{TOC right}}";
    }

    @Override
    public String getTagName() {
        return "";
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }
}
