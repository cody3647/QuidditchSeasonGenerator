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

import java.util.Collection;

public class Image extends Element{
    public static String IMG = "img";

    public Image() {
        super(IMG);
    }

    public Image(String alt, String imageName) {
        super(IMG);
        addAttribute("src", "/images/" + imageName);
        addAttribute("alt", alt);
    }
}
