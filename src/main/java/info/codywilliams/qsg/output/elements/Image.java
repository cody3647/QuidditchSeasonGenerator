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
import info.codywilliams.qsg.output.InlineElement;
import info.codywilliams.qsg.util.Formatters;

import java.util.Map;
import java.util.Set;

public class Image extends Element implements InlineElement {
    public static final String IMAGES_DIR = "../images/";
    private static final Set<String> specialClasses = Set.of("border", "frameless", "frame", "thumb");
    private static final Set<String> specialAttributes = Set.of("height", "width");
    public static String IMG = "img";
    public String imageName;

    public Image(String alt, String imageName) {
        addAttribute("src", IMAGES_DIR + Formatters.sanitizeFileNames(imageName));
        addAttribute("alt", alt);
        this.imageName = imageName;
    }

    @Override
    public String getTagName() {
        return IMG;
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }

    @Override
    public String toHtml(int tabs) {
        StringBuilder stringBuilder = new StringBuilder();
        appendNewLineAndTabs(stringBuilder, tabs);
        stringBuilder.append("<img");

        createClassesString(classes, stringBuilder);
        createAttributeString(attributes, stringBuilder);

        stringBuilder.append('>');
        return stringBuilder.toString();
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("[[");
        wikitextImageFile(stringBuilder);
        stringBuilder.append("]]");

        return stringBuilder.toString();
    }

    protected StringBuilder wikitextImageFile(StringBuilder stringBuilder) {
        stringBuilder.append("File:")
                .append(imageName);

        StringBuilder wikitextClass = new StringBuilder();
        for (String c : classes) {
            if (specialClasses.contains(c))
                stringBuilder.append("|").append(c);
            else
                wikitextClass.append(c);
        }

        if (!wikitextClass.isEmpty()) {
            stringBuilder.append("|class=").append(wikitextClass);
        }

        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            if (specialAttributes.contains(attribute.getKey())) {
                stringBuilder.append('|');

                if (attribute.getKey().equals("height"))
                    stringBuilder.append('x');

                stringBuilder.append(attribute.getValue());
            } else {
                if (attribute.getKey().equals("src"))
                    continue;
                stringBuilder.append('|').append(attribute.getKey()).append('=').append(attribute.getValue());
            }
        }

        return stringBuilder;
    }

    static public class Gallery extends Div {
        static final String DEFAULT_MODE = "packed";
        final String mode;

        public Gallery(String mode) {
            super();
            if (mode == null)
                mode = DEFAULT_MODE;
            this.mode = mode;
        }

        public void addImages(Image... images) {
            addChildren(images);
        }

        @Override
        public String toHtml(int tabs) {
            super.classes.add("image-gallery");
            return super.toHtml(0);
        }

        @Override
        public String toWikitext() {
            StringBuilder stringBuilder = new StringBuilder("<gallery mode=").append(mode);
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                stringBuilder.append(' ').append(attribute.getKey()).append('=').append(attribute.getValue());
            }
            stringBuilder.append(">\n");

            for (Element child : children) {
                if (!(child instanceof Image image))
                    continue;

                stringBuilder.append("File:").append(image.imageName).append('\n');
            }

            stringBuilder.append("</gallery>");
            return stringBuilder.toString();
        }
    }

}
