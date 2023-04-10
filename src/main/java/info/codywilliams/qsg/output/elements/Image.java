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

import java.util.Map;
import java.util.Set;

public class Image extends Element{
    public static String IMG = "img";
    private static Set<String> specialClasses = Set.of("border", "frameless", "frame", "thumb");
    private static Set<String> specialAttributes = Set.of("height", "width");

    public String imageName;

    public Image(String alt, String imageName) {
        super(IMG);
        addAttribute("src", "/images/" + Formatters.sanitizeFileNames(imageName));
        addAttribute("alt", alt);
        this.imageName = imageName;
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("[[");
        wikitextImage(stringBuilder);
        stringBuilder.append("]]");

        return stringBuilder.toString();
    }

    private StringBuilder wikitextImage(StringBuilder stringBuilder) {
        stringBuilder.append("File:")
                .append(imageName);

        StringBuilder wikitextClass = new StringBuilder();
        for(String c: classes) {
            if (specialClasses.contains(c))
                stringBuilder.append("|").append(c);
            else
                wikitextClass.append(c);
        }

        if(!wikitextClass.isEmpty()){
            stringBuilder.append("|class=").append(wikitextClass);
        }

        for(Map.Entry<String, String> attribute: attributes.entrySet()) {
            if(specialAttributes.contains(attribute.getKey())) {
                stringBuilder.append('|');

                if(attribute.getKey().equals("height"))
                    stringBuilder.append('x');

                stringBuilder.append(attribute.getValue());
            }
            else {
                stringBuilder.append('|').append(attribute.getKey()).append('=').append(attribute.getValue());
            }
        }

        return stringBuilder;
    }
}
