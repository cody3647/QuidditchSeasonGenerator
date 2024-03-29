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

import info.codywilliams.qsg.output.elements.Header;
import info.codywilliams.qsg.output.elements.Text;
import info.codywilliams.qsg.service.OutputService;
import info.codywilliams.qsg.util.DependencyInjector;

import java.util.*;

public class Page implements ElementOutputs {
    public enum Type {MATCH, TOURNAMENT, TEAM, PLAYER}
    private final String pageTitle;
    private final String directory;
    private final ArrayList<Metadata> metadata;
    private final ArrayList<Element> body;
    private final ArrayList<String> styles;
    private final Page.Type pageType;

    public Page(String pageTitle, String directory, Type pageType) {
        this.pageTitle = pageTitle;
        this.directory = OutputService.sanitizeDirectories(directory);
        this.pageType = pageType;

        metadata = new ArrayList<>();
        body = new ArrayList<>();
        styles = new ArrayList<>();
        addMetadata("author", "", DependencyInjector.getBundle().getString("app.title"), "");
    }

    public void addMetadata(String name, String httpEquiv, String content, String charset) {
        this.metadata.add(new Metadata(name, httpEquiv, content, charset));
    }

    public void addBodyContent(Element element) {
        if (element != null)
            body.add(element);
    }

    public void addBodyContent(Element... elements) {
        body.addAll(Arrays.stream(elements).filter(Objects::nonNull).toList());

    }

    public void addBodyContent(Collection<Element> elements) {
        body.addAll(elements.stream().filter(Objects::nonNull).toList());
    }

    public List<String> getStyles() {
        return styles;
    }

    public void addStyle(String style) {
        this.styles.add(style);
    }

    public List<Element> getSubsetPageElements(String startHeaderExclusive, String endHeaderExclusive) {
        List<Element> newPageElements = new ArrayList<>();
        boolean seen = false;
        for (Element element: body) {
            if (element instanceof Header header) {
                String text = String.join(" ", header.children.stream().map(Text::getText).toList());
                if (text.equals(startHeaderExclusive)) {
                    seen = true;
                    continue;
                }
                if (text.equals(endHeaderExclusive)) {
                    break;
                }
            }

            if (seen) {
                newPageElements.add(element);
            }
        }

        return newPageElements;
    }

    @Override
    public String toHtml(int tabs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<meta charset=\"utf-8\">");
        if (pageTitle != null)
            stringBuilder.append("\n\t\t<title>").append(pageTitle).append("</title>");
        if (!metadata.isEmpty())
            for (Metadata meta : metadata)
                stringBuilder.append(meta.toHtml());
        if (!styles.isEmpty())
            for (String style : styles)
                stringBuilder.append("\n\t\t<link rel='stylesheet' href=\"../css/").append(style).append("\">");
        stringBuilder.append("\n</head>\n<body>\n\t<div id=\"content\">");

        stringBuilder.append("\n\t\t<h1 class=\"firstHeading\">").append(pageTitle).append("</h1>");

        if (!body.isEmpty()) {
            for (Element element : body)
                stringBuilder.append(element.toHtml(2));
        }
        stringBuilder.append("\n\t</div>");
        stringBuilder.append("\n</body>\n</html>");

        return stringBuilder.toString();
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!styles.isEmpty())
            for (String style : styles)
                stringBuilder.append("<templatestyles src=\"Styles/").append(style).append("\" />\n");

        if (!body.isEmpty()) {
            for (Element element : body)
                stringBuilder.append(element.toWikitext());
        }

        return stringBuilder.toString();
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getDirectory() {
        return directory;
    }

    public Type getPageType() {
        return pageType;
    }

    static public class Metadata {
        private final String name;
        private final String httpEquiv;
        private final String content;
        private final String charset;

        public Metadata(String name, String httpEquiv, String content, String charset) {
            this.name = name;
            this.httpEquiv = httpEquiv;
            this.content = content;
            this.charset = charset;
        }

        public String toHtml() {
            String tag = "\n\t\t<meta ";
            if (name != null)
                tag += "name=\"" + name + "\" ";
            if (httpEquiv != null)
                tag += "http-equiv=\"" + httpEquiv + "\" ";
            if (content != null)
                tag += "content=\"" + content + "\" ";
            if (charset != null)
                tag += "charset=\"" + charset + "\" ";

            tag += ">";

            return tag;
        }
    }
}
