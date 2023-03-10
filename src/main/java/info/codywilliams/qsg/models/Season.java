/*
 * Quidditch Season Generator
 * Copyright (C) 2022.  Cody Williams
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

package info.codywilliams.qsg.models;

import info.codywilliams.qsg.models.match.Match;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class Season {
    private final SortedSet<Team> teams;
    private final Map<LocalDateTime, Match> matches;


    public Season(List<Team> teams){
        matches = new ConcurrentSkipListMap<>();
        this.teams = new TreeSet<>(teams);
    }

    public Match addMatch(Match match){
        return matches.put(match.getStartDateTime(), match);
    }

    public SortedSet<Team> getTeams() {
        return teams;
    }

    public Map<LocalDateTime, Match> getMatches() {
        return matches;
    }
}
