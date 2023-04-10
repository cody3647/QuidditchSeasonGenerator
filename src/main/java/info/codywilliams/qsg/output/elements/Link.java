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
import info.codywilliams.qsg.util.Formatters;

public class Link extends Element {
    public static String A = "a";
    String href;
    String title;
    private Link(Element element, String dir, String href, String title) {
        super(A, element);
        addAttribute("href", dir + Formatters.sanitizeFileNames(href));
        addAttribute("title", title);

        this.href = href;
        this.title = title;
    }

    @Override
    public String toWikitext() {
        Element child = children.getFirst();
        if(child instanceof Text) {
            String ret = "[[" + href;
            if(title != null)
                ret += "|" + title;

            return ret + "]]";
        }
        else if (child instanceof Image) {
            child.addAttribute("link", href);
            return child.toWikitext();
        }

        return "";
    }

    public static class Team extends Link {
        public Team(String text, String teamName) {
            this(new Text(text), teamName, text);
        }

        public Team(Element element, String teamName, String title) {
            super(element, "../teams/",  teamName + ".html", title);
            href = teamName;
            this.title = title;
        }


    }

    public static class Match extends Link {
        public Match(String text, String matchTitle) {
            this(new Text(text), matchTitle, text);
        }

        public Match(Element element, String matchTitle, String title) {
            super(element, "", matchTitle + ".html", title);
            href = matchTitle;
            this.title = title;
        }
    }

    public static class Tournament extends Link {
        public Tournament(String text) {
            this(new Text(text), text);
        }

        public Tournament(Element element, String title) {
            super(element, "", "index.html", title);
            href = title;
            this.title = title;
        }
    }

}
