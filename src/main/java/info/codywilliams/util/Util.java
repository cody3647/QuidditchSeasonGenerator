/*
 * Copyright (c) 2022. Cody Williams
 *
 * Util.java is part of Quidditch Season Generator.
 *
 * Quidditch Season Generator is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quidditch Season Generator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    /**
     * The name of the app is {@value}.
     */
    public static final String APP_DISPLAY_NAME = "Quidditch Season Generator";
    /**
     * The name of the app without spaces, used for directories and such, is {@value}.
     */
    public static final String APP_DIR_NAME = "QuidditchSeasonGenerator";
    /**
     * The default config directory is the directory the application is run from.
     */
    public static final Path DEFAULT_SETTINGS_DIR = Paths.get(System.getProperty("user.dir"));
    /**
     * The default config file name is {@value}.
     */
    public static final String[] DEFAULT_SETTINGS_FILENAME = {"settings","json"};
}
