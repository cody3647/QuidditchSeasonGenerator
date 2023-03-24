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

package info.codywilliams.qsg.util;

import java.util.ResourceBundle;

public class OutputBundle {
    private ResourceBundle bundle;
    private String leagueName;
    private String yearRange;

    public OutputBundle() {
        this.bundle = DependencyInjector.getOutputBundle();
    }

    public String getString(String key) {
        return bundle.getString(key);
    }

    public String getTournamentString(String key) {
        String text = bundle.getString(key);
        text = text.replace("${leagueName}", leagueName);
        text = text.replace("${yearRange}", yearRange);
        return text;
    }

    public void setTournamentString(String leagueName, String yearRange) {
        this.leagueName = leagueName;
        this.yearRange = yearRange;
    }
}
