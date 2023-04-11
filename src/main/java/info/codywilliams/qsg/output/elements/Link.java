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
import info.codywilliams.qsg.util.Formatters;

public class Link {
    static public final String TEAMS_DIR = "../teams/";
    public static String A = "a";

    public static class TextLink extends ElementChildren<Text> {
        String wikipage;

        public TextLink(String text, String dir, String page, String ext) {
            super(new Text(text));
            addAttribute("href", dir + Formatters.sanitizeFileNames(page) + '.' + ext);
            wikipage = page;
        }

        @Override
        public String getTagName() {
            return A;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return false;
        }

        public void setWikipage(String wikipage) {
            this.wikipage = wikipage;
        }

        static public TextLink createTeamLink(String teamName) {
            return new TextLink(teamName, TEAMS_DIR, teamName, "html");
        }

        static public TextLink createMatchLink(String text, String matchTitle) {
            return new TextLink(text, "", matchTitle, "html");
        }

        static public TextLink createTournamentLink(String text, String tournamentTitle) {
            TextLink tournamentLink = new TextLink(text, "", "index", "html");
            tournamentLink.wikipage = tournamentTitle;
            return tournamentLink;
        }

        @Override
        public String toWikitext() {
            StringBuilder stringBuilder = new StringBuilder("[[");
            stringBuilder.append(wikipage);

            if (!children.isEmpty()) {
                stringBuilder.append('|');
                for (Text text : children)
                    stringBuilder.append(text.toWikitext());
            }

            stringBuilder.append("]]");

            return stringBuilder.toString();
        }
    }

    public static class ImageLink extends Element {
        private final Image image;
        String wikipage;
        public ImageLink(String alt, String imageName, String pageDir, String page, String pageExt) {
            image = new Image(alt, imageName);
            addAttribute("href", pageDir + Formatters.sanitizeFileNames(page) + '.' + pageExt);
            wikipage = page;
        }

        @Override
        public String getTagName() {
            return A;
        }

        @Override
        public boolean isTagClosedOnNewLine() {
            return false;
        }

        static public ImageLink createTeamLink(String teamName) {
            return new ImageLink(teamName, teamName + ".png", TEAMS_DIR, teamName, "html");
        }

        public void addImageAttribute(String name, String value) {
            image.addAttribute(name, value);
        }

        public void addImageClass(String... names) {
            image.addClass(names);
        }

        @Override
        public String toHtml(int tabs) {
            StringBuilder stringBuilder = new StringBuilder();

            openHtmlTag(stringBuilder, tabs);
            stringBuilder.append(image.toHtml(tabs + 1));
            closeHtmlTag(stringBuilder, tabs);

            return stringBuilder.toString();
        }

        @Override
        public String toWikitext() {
            StringBuilder stringBuilder = new StringBuilder("[[");

            image.wikitextImageFile(stringBuilder);

            stringBuilder.append("|link=").append(wikipage);

            stringBuilder.append("]]");

            return stringBuilder.toString();
        }
    }
}
