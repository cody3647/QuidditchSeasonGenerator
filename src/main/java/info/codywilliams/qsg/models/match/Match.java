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

package info.codywilliams.qsg.models.match;

import info.codywilliams.qsg.models.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class Match implements Comparable<Match>{
    private final int number;
    private final int round;
    private final LocalDateTime startDateTime;
    private Team homeTeam;
    private Team awayTeam;
    private String location;
    private Duration matchLength;
    private boolean timeChanged;

    private int scoreHome;
    private int scoreAway;
    private int foulsHome;
    private int foulsAway;
    private LinkedList<Play> plays;

    public Match(int number, int round, LocalDateTime startDateTime){
        this.number = number;
        this.round = round;
        this.startDateTime = startDateTime;
        this.matchLength = Duration.ZERO;
        plays = new LinkedList<>();

        scoreHome = 0;
        scoreAway = 0;
    }
    public int getNumber() {
        return number;
    }

    public int getRound() {
        return round;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Duration getMatchLength() {
        return matchLength;
    }

    public LinkedList<Play> getPlays() {
        return plays;
    }

    public void addPlay(Play play) {
        addTime(play.getPlayDurationSeconds());
        play.setScores(getScoreHome(), getScoreAway());
        play.setMatchLength(getMatchLength());
        this.plays.add(play);
    }

    public int getScoreHome() {
        return scoreHome;
    }

    public int getScoreAway() {
        return scoreAway;
    }

    public int homeScore(){
        scoreHome += 10;
        return scoreHome;
    }
    public int awayScore(){
        scoreAway += 10;
        return scoreAway;
    }

    public int homeCaughtSnitch(){
        scoreHome += 150;
        return scoreHome;
    }

    public int awayCaughtSnitch(){
        scoreAway += 150;
        return scoreAway;
    }

    private void addTime(int seconds){
        matchLength = matchLength.plusSeconds(seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        if (number != match.number) return false;
        if (round != match.round) return false;
        return startDateTime.equals(match.startDateTime);
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + round;
        result = 31 * result + startDateTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Match{" +
                "\n\tnumber=" + number +
                "\n\tround=" + round +
                "\n\thomeTeam=" + homeTeam.getName() +
                "\n\tawayTeam=" + awayTeam.getName() +
                "\n\tlocation='" + location + '\'' +
                "\n\tstartDateTime=" + startDateTime +
                "\n\tmatchLength=" + matchLength +
                '}';
    }


    @Override
    public int compareTo(Match other) {
        if (this == other)
            return 0;
        int dateComp = startDateTime.compareTo(other.startDateTime);
        int roundComp = round - other.round;

        if(roundComp == 0) {
            if (dateComp == 0) {
                return number - other.number;
            }
            return dateComp;
        }
        return roundComp;
    }
}
