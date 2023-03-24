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

    static public class Header extends Element {
        public static String TH = "th";
        public Header() {
            super(TH);
        }

        public Header(Element element) {
            super(TH, element);
        }

        public Header(Element... elements) {
            super(TH, elements);
        }

        public Header(Collection<Element> elements) {
            super(TH, elements);
        }

        public Header(String text) {
            super(TH, new Text(text));
        }
    }
}
