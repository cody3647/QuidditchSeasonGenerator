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
import javafx.scene.Node;

import java.util.Collection;

public class DefinitionList extends Element {
    public static String DL = "dl";
    public DefinitionList() {
        super(DL);
    }

    public DefinitionList(Element... elements) {
        super(DL, elements);
    }

    public DefinitionList(Collection<Element> elements) {
        super(DL, elements);
    }

    @Override
    public String toWikitext() {
        return toHtml();
    }

    public static class Term extends Element {
        public static String DT = "dt";
        public Term() {
            super(DT);
        }

        public Term(Element element) {
            super(DT, element);
        }

        public Term(Element... elements) {
            super(DT, elements);
        }

        public Term(Collection<Element> elements) {
            super(DT, elements);
        }

        public Term(String text) {
            super(DT, new Text(text));
        }

        @Override
        public String toWikitext() {
            return toHtml();
        }
    }

    public static class Defintion extends  Element {
        public static String DD = "dd";
        public Defintion() {
            super(DD);
        }

        public Defintion(Element element) {
            super(DD, element);
        }

        public Defintion(Element... elements) {
            super(DD, elements);
        }

        public Defintion(Collection<Element> elements) {
            super(DD, elements);
        }

        public Defintion(String text) {
            super(DD, new Text(text));
        }

        @Override
        public String toWikitext() {
            return toHtml();
        }
    }
}
