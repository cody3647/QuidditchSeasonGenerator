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

import info.codywilliams.qsg.util.DependencyInjector;
import info.codywilliams.qsg.util.Formatters;
import javafx.scene.Node;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Page implements Outputs{
    private String pageTitle;
    private String fileName;
    private LinkedList<Metadata> metadata;

    private LinkedList<Element> body;

    public Page(String pageTitle, String fileName) {
        this.pageTitle = pageTitle;
        this.fileName = fileName;

        metadata = new LinkedList<>();
        body = new LinkedList<>();
        addMetadata("author", "", DependencyInjector.getBundle().getString("app.title"), "");
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void addMetadata(String name, String httpEquiv, String content, String charset) {
        this.metadata.add(new Metadata(name, httpEquiv, content, charset));
    }

    public void addBodyContent(Element element) {
        body.add(element);
    }

    public void addBodyContent(Element... elements) {
        body.addAll(Arrays.asList(elements));
    }

    public void addBodyContent(Collection<Element> elements) {
        body.addAll(elements);
    }

    @Override
    public String toHtml() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<!DOCTYPE html><html><head>");
        if(pageTitle != null)
            stringBuilder.append("<title>").append(pageTitle).append("</title>");
        if(!metadata.isEmpty())
            for(Metadata meta: metadata)
                stringBuilder.append(meta.toHtml());
        stringBuilder.append("</head><body>");

        if(!body.isEmpty()) {
            for(Element element: body)
                stringBuilder.append(element.toHtml());
        }

        stringBuilder.append("</body></html>");

        return stringBuilder.toString();
    }



    static public class Metadata {
        private String name;
        private String httpEquiv;
        private String content;
        private String charset;

        public Metadata(String name, String httpEquiv, String content, String charset) {
            this.name = name;
            this.httpEquiv = httpEquiv;
            this.content = content;
            this.charset = charset;
        }

        public String toHtml() {
            String tag = "<meta ";
            if(name != null)
                tag += "name=\"" + name + "\" ";
            if(httpEquiv != null)
                tag += "http-equiv=\"" + httpEquiv + "\" ";
            if(content != null)
                tag += "content=\"" + content + "\" ";
            if(charset != null)
                tag += "charset=\"" + charset + "\" ";

            tag += ">";

            return tag;
        }
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getFileName() {
        return fileName;
    }
}
