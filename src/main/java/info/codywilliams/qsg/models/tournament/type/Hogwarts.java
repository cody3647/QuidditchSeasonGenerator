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

package info.codywilliams.qsg.models.tournament.type;

import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class Hogwarts extends Tournament {
    public Hogwarts(TournamentOptions tournamentOptions) {
        super(tournamentOptions, TournamentType.HOGWARTS);
    }

    public Hogwarts(TournamentOptions tournamentOptions, TournamentType type) {
        super(tournamentOptions, type);
    }

    @Override
    protected int calculateTotalMatches(int numTeams) {
        return 6;
    }

    @Override
    protected int calculateTotalRounds(int numTeams) {
        return 3;
    }

    @Override
    protected int calculateMatchesPerRound(int numTeams) {
        return 2;
    }

    @Override
    protected LocalDate calculateMatchDates() {
        matches.clear();

        int year = tournamentOptions.getStartDate().getYear();

        // First Match Second Weekend November
        LocalDate date = LocalDate.of(year, 11, 1);
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SATURDAY));
        matches.add(new Match(1, 1, date.atTime(11, 0)));
        tournamentOptions.setStartDate(date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)));

        // Second Match Fourth Weekend November
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SATURDAY));
        matches.add(new Match(2, 1, date.atTime(11, 0)));

        // Third Match Fourth Weekend February
        date = LocalDate.of(++year, 2, 1);
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SATURDAY));
        matches.add(new Match(3, 2, date.atTime(11, 0)));

        // Fourth Match Second Weekend March
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(6, DayOfWeek.SATURDAY));
        matches.add(new Match(4, 2, date.atTime(11, 0)));

        // Fifth Match First Weekend May
        date = LocalDate.of(year, 5, 1);
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.SATURDAY));
        matches.add(new Match(5, 3, date.atTime(11, 0)));

        // Sixth Match Third Weekend May
        date = date.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.SATURDAY));
        matches.add(new Match(6, 3, date.atTime(11, 0)));

        return date;
    }

    @Override
    public TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed) {
        // Only first four teams
        Collections.sort(teams);
        Team teamG = teams.get(0);
        Team teamH = teams.get(1);
        Team teamR = teams.get(2);
        Team teamS = teams.get(3);

        teamList = new ArrayList<>(teams.subList(0, 4));

        List<Match> matchesList = new ArrayList<>(matches);
        // First Match Second Weekend November
        Match match = matchesList.get(0);
        match.setHomeTeam(teamG);
        match.setAwayTeam(teamS);
        match.setLocation(teamG.getHome());

        // Second Match Fourth Weekend November
        match = matchesList.get(1);
        match.setHomeTeam(teamR);
        match.setAwayTeam(teamH);
        match.setLocation(teamR.getHome());

        // Third Match Fourth Weekend February
        match = matchesList.get(2);
        match.setHomeTeam(teamS);
        match.setAwayTeam(teamR);
        match.setLocation(teamS.getHome());

        // Fourth Match Second Weekend March
        match = matchesList.get(3);
        match.setHomeTeam(teamH);
        match.setAwayTeam(teamG);
        match.setLocation(teamH.getHome());

        // Fifth Match First Weekend May
        match = matchesList.get(4);
        match.setHomeTeam(teamH);
        match.setAwayTeam(teamS);
        match.setLocation(teamH.getHome());

        // Sixth Match Third Weekend May
        match = matchesList.get(5);
        match.setHomeTeam(teamG);
        match.setAwayTeam(teamR);
        match.setLocation(teamG.getHome());

        teamsAssigned.set(true);
        return new TreeSet<>(matches.getValue());
    }

    @Override
    public void assignPoints() {
        if (matches.isEmpty())
            return;

        for(Match match: getMatches()) {
            if (match.getWinner() == null)
                continue; // Tie

            tournamentPoints.merge(match.getHomeTeam().getName(), match.getScoreHome(), Integer::sum);
            tournamentPoints.merge(match.getAwayTeam().getName(), match.getScoreAway(), Integer::sum);
        }

    }

    @Override
    public String getPoints(Match match) {
        return match.getScoreHome() + "—" + match.getScoreAway();
    }
}
