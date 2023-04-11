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
import info.codywilliams.qsg.output.ElementOutputs;
import info.codywilliams.qsg.output.InlineElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefinitionList extends ElementChildren<DefinitionList.Item> {
    public static String DL = "dl";
    private final List<Item> items;
    public DefinitionList() {
        items = new ArrayList<>();
    }

    public DefinitionList(Item... items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public DefinitionList(Collection<Item> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public String getTagName() {
        return DL;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return true;
    }

    @Override
    public String toWikitext() {
        return toHtml(0);
    }

    public static class Term extends ElementChildren<InlineElement> implements Item {
        public static String DT = "dt";

        public Term(InlineElement element) {
            super(element);
        }

        public Term(String text) {
            super(new Text(text));
        }

        @Override
        public String getTagName() {
            return DT;
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

    public static class Def extends ElementChildren<InlineElement> implements Item{
        public static String DD = "dd";

        public Def(InlineElement element) {
            super(element);
        }

        public Def(String text) {
            super(new Text(text));
        }

        @Override
        public String getTagName() {
            return DD;
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

    public interface Item extends ElementOutputs {

    }
}
