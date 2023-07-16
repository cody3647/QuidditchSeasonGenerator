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

public class Header extends ElementChildren<Text> {
    public final static String wikitextTag = "======";
    public static String H = "h";
    public int level;

    public Header(int level, String text) {
        super(new Text(text));
        if (level > 6)
            this.level = 6;
        else if (level < 1)
            this.level = 1;
        else
            this.level = level;
    }

    @Override
    public String getTagName() {
        return H + level;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\n');
        addWikitextTag(stringBuilder);

        for (Text text : children) {
            stringBuilder.append(text.toWikitext());
        }

        addWikitextTag(stringBuilder);
        stringBuilder.append('\n');

        return stringBuilder.toString();
    }

    private void addWikitextTag(StringBuilder stringBuilder) {
        stringBuilder.append(wikitextTag, 0, level);
    }


}
