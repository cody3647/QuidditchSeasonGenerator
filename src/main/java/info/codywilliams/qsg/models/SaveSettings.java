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

import info.codywilliams.qsg.models.tournament.BlackoutDates;
import info.codywilliams.qsg.models.tournament.MatchDayTime;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.type.TournamentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaveSettings {
    private List<Team> teams;
    private TournamentType tournamentType;
    // Tournament Options
    private String leagueName;
    private List<MatchDayTime> matchDayTimeList;
    private List<BlackoutDates> blackoutDates;
    private LocalDate startDate;
    private long seed;

    public SaveSettings() {
    }

    public SaveSettings(List<Team> teams, TournamentType tournamentType, TournamentOptions tournamentOptions, long seed) {
        this.teams = teams;
        this.tournamentType = tournamentType;
        leagueName = tournamentOptions.getLeagueName();

        matchDayTimeList = new ArrayList<>(tournamentOptions.getMatchDayTimeList());
        blackoutDates = new ArrayList<>(tournamentOptions.getBlackoutDates());

        startDate = tournamentOptions.getStartDate();
        this.seed = seed;
    }

    public SaveSettings(Context context) {
        this(new ArrayList<>(context.getTeams()), (context.getCurrentTournament() != null) ? context.getCurrentTournament().getType() : null, context.getTournamentOptions(), context.getSeed());
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public List<MatchDayTime> getMatchDayTimeList() {
        return matchDayTimeList;
    }

    public void setMatchDayTimeList(List<MatchDayTime> matchDayTimeList) {
        this.matchDayTimeList = matchDayTimeList;
    }

    public List<BlackoutDates> getBlackoutDates() {
        return blackoutDates;
    }

    public void setBlackoutDates(List<BlackoutDates> blackoutDates) {
        this.blackoutDates = blackoutDates;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
