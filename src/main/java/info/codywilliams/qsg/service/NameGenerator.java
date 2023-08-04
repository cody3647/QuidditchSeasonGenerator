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

package info.codywilliams.qsg.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;

public class NameGenerator {
    private final LinkedList<String> names;
    private int used;
    private final int reshuffle;

    public NameGenerator(String filename) {
        names = new LinkedList<>();
        String line;
        try (
                InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/info/codywilliams/qsg/names/" + filename));
                BufferedReader br = new BufferedReader(isr)
        ) {
            while ((line = br.readLine()) != null) {
                names.add(line);
            }
        } catch (IOException ignored) {
        }

        reshuffle = names.size() / 2;
    }

    public String getNextName(boolean keep) {
        String name;
        synchronized (this) {
            if (used % reshuffle == 0)
                Collections.shuffle(names);
            name = names.poll();
            used++;
        }

        if (keep)
            names.add(name);
        return name;
    }

    public String getNextName() {
        return getNextName(true);
    }
}
