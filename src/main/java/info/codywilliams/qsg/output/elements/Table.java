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
import info.codywilliams.qsg.output.ElementOutputs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table extends ElementChildren<Table.Row>{
    public static String TABLE = "table";
    private String caption = null;

    public Table(Row... rows) {
        super(rows);
    }

    public Table(Collection<Row> rows) {
        super(rows);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    protected StringBuilder openHtmlTag(StringBuilder stringBuilder, int tabs) {
        super.openHtmlTag(stringBuilder, tabs);
        appendNewLineAndTabs(stringBuilder, tabs);
        stringBuilder.append("<caption>").append(caption).append("</caption");
        return stringBuilder;
    }

    @Override
    public String getTagName() {
        return TABLE;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return true;
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("\n{|");
        createClassesString(classes, stringBuilder);
        createAttributeString(attributes, stringBuilder);

        if (caption != null) {
            stringBuilder.append("\n|+").append(caption);
        }
        for(Row row: children) {
            stringBuilder.append(row.toWikitext());
        }

        stringBuilder.append("\n|}");

        return stringBuilder.toString();
    }

    public static class Cell extends ElementChildren<Element> implements TableCell{
        public static String TD = "td";

        public Cell(Element... elements) {
            super(elements);
        }

        public Cell(String text) {
            super(new Text(text));
        }

        @Override
        public String getTagName() {
            return TD;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return false;
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

    }

    static public class HeaderCell extends ElementChildren<Element> implements TableCell{
        public static String TH = "th";

        public HeaderCell(Element... elements) {
            super(elements);
        }

        public HeaderCell(String text) {
            super(new Text(text));
        }

        @Override
        public String getTagName() {
            return TH;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return false;
        }

        @Override
        public String toWikitext() {
            return Cell.createWikiCellString(classes, attributes, children,'!');
        }
    }

    public static class Row extends ElementChildren<TableCell>{
        public static String TR = "tr";

        public Row(TableCell... cells) {
            super(cells);
        }

        public Row(Collection<TableCell> cells) {
            super(cells);
        }

        @Override
        public String getTagName() {
            return TR;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return true;
        }

        @Override
        public String toHtml(int tabs) {
            StringBuilder stringBuilder = new StringBuilder();

            openHtmlTag(stringBuilder, tabs);
            for(TableCell cell: children) {
                stringBuilder.append(cell.toHtml(tabs + 1));
            }

            closeHtmlTag(stringBuilder, tabs);

            return stringBuilder.toString();
        }

        @Override
        public String toWikitext() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n|-");

            if(!classes.isEmpty() || !attributes.isEmpty()) {
                createClassesString(classes, stringBuilder);
                createAttributeString(attributes, stringBuilder);
            }

            for(TableCell cell: children) {
                stringBuilder.append(cell.toWikitext());
            }


            return stringBuilder.toString();
        }
    }

    public interface TableCell extends ElementOutputs {
    }
}
