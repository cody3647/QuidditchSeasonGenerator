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

package info.codywilliams.qsg.models.tournament.type;

import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.tournament.MatchDayTime;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class StraightRoundRobin extends Tournament {
    public StraightRoundRobin(TournamentOptions tournamentOptions) {
        super(tournamentOptions, TournamentType.STRAIGHT_ROUND_ROBIN);
    }

    public StraightRoundRobin(TournamentOptions tournamentOptions, TournamentType type) {
        super(tournamentOptions, type);

    }

    @Override
    protected int calculateTotalMatches(int numTeams) {
        return (int) ((numTeams / 2.0) * (numTeams - 1));
    }

    @Override
    protected int calculateTotalRounds(int numTeams) {
        return numTeams % 2 == 0 ? numTeams - 1 : numTeams;
    }

    @Override
    protected int calculateMatchesPerRound(int numTeams) {
        return (numTeams % 2 == 0) ? (numTeams / 2) : ((numTeams - 1) / 2);
    }

    @Override
    protected LocalDate calculateMatchDates() {
        List<MatchDayTime> matchDayTimeList = tournamentOptions.getSortedMatchDayTimeList();

        if (matchDayTimeList.isEmpty()) return null;

        // Move template to first acceptable day of the week selected in get start date
        LocalDate date = tournamentOptions.getStartDate();

        // Setup iterators
        Iterator<MatchDayTime> matchDayIterator = matchDayTimeList.iterator();
        Set<LocalDate> blackoutDates = getBlackoutDateSet();

        // Setup All Round Set<Dates>
        matches.clear();

        int totalMatches = getNumMatches();
        int totalRoundMatches = getNumMatchesPerRound();
        int totalMatchCount = 0;
        int roundMatchCount = 0;
        int round = 1;

        while (totalMatchCount < totalMatches) {
            if (roundMatchCount == totalRoundMatches) {
                // Round is over, New Round
                roundMatchCount = 0;
                round++;
            }

            if (!matchDayIterator.hasNext()) {
                matchDayIterator = matchDayTimeList.iterator();
                date = date.plusWeeks(1);
            }

            MatchDayTime matchDayTime = matchDayIterator.next();
            date = nextMatchDate(date, matchDayTime.getDayOfWeek());
            if (blackoutDates.contains(date))
                continue;

            int count = matchDayTime.getCount();
            LocalDateTime localDateTime = matchDayTime.getLocalTime().atDate(date);
            while (count > 0) {
                count--;
                matches.add(new Match(roundMatchCount + 1, round, localDateTime));
                roundMatchCount++;
                totalMatchCount++;

                if (roundMatchCount == getNumMatchesPerRound())
                    break;
            }
        }

        return matches.stream()
                .map(match -> match.getStartDateTime().toLocalDate())
                .max(Comparator.naturalOrder())
                .orElse(tournamentOptions.getStartDate());
    }

    @Override
    public TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed) {
        teamList = new ArrayList<>(teams);
        ArrayList<Team> teamArrayList = new ArrayList<>(teams);
        Random rand = new Random(seed + "TeamShuffle".hashCode());

        Collections.shuffle(teamArrayList, rand);

        // Round Robin Circle Method
        if (teamArrayList.size() % 2 != 0)
            teamArrayList.add(null);

        int round = 1;
        int aIdx = 0;
        int bIdx = teamArrayList.size() - 1;
        int half = numRounds.get() / 2;
        HashMap<String, Integer> homeTeamTimes = new HashMap<>();
        HashSet<String> matcheSet = new HashSet<>();
        boolean reset = false;
        for (Match match : getMatches()) {
            if (match.getRound() != round) {
                round++;
                aIdx = 0;
                bIdx = teamArrayList.size() - 1;
                Collections.rotate(teamArrayList.subList(1, teamArrayList.size()), 1);
            }
            if (teamArrayList.get(aIdx) == null || teamArrayList.get(bIdx) == null) {
                aIdx++;
                bIdx--;
            }

            // Balance out the home and away matches.  Reverse for the second half if home and away
            if (getType() == TournamentType.STRAIGHT_ROUND_ROBIN || (round <= half && getType() == TournamentType.STRAIGHT_ROUND_ROBIN_HOME_AWAY)) {
                int aCount = homeTeamTimes.getOrDefault(teamArrayList.get(aIdx).getName(), 0);
                int bCount = homeTeamTimes.getOrDefault(teamArrayList.get(bIdx).getName(), 0);

                assignTeams(aCount < bCount, match, teamArrayList.get(aIdx), teamArrayList.get(bIdx), homeTeamTimes);
            } else {
                if (!reset && round == half + 1) {
                    homeTeamTimes.clear();
                    reset = true;
                }
                int aCount = homeTeamTimes.getOrDefault(teamArrayList.get(aIdx).getName(), 0);
                int bCount = homeTeamTimes.getOrDefault(teamArrayList.get(bIdx).getName(), 0);

                // Reverse comparison and teamA and teamB so that when counts are equal or different behavior is reversed from above
                assignTeams(aCount > bCount, match, teamArrayList.get(bIdx), teamArrayList.get(aIdx), homeTeamTimes);
            }

            boolean exists = !matcheSet.add(match.getHomeTeam().getName() + match.getAwayTeam().getName());
            if(exists) {
                Team temp = match.getHomeTeam();
                match.setHomeTeam(match.getAwayTeam());
                match.setAwayTeam(temp);
                exists = matcheSet.add(match.getHomeTeam().getName() + match.getAwayTeam().getName());
                if (exists)
                    logger.error("Swapped Teams also already exists, {} {}", match.getHomeTeam().getName(), match.getAwayTeam().getName());
            }

            aIdx++;
            bIdx--;
        }

        // The fixed team in the schedule will always play first match, randomize this some.
        int randInt = 0;
        Match firstMatch = null;
        Match otherMatch;
        for (Match match : getMatches()) {
            // Swap first match with random other match in round
            if (match.getNumber() == 1) {
                firstMatch = match;
                randInt = rand.nextInt(getNumMatchesPerRound()) + 1;
            }
            if (match.getNumber() == randInt) {
                otherMatch = match;
                swapMatchTeams(firstMatch, otherMatch);
                randInt = 0;
                firstMatch = null;
            }
        }

        teamsAssigned.set(true);
        return new TreeSet<>(matches.getValue());
    }

    @Override
    public void assignPoints() {
        if (matches.isEmpty())
            return;

        for (Match match : getMatches()) {
            if (match.getWinner() == null)
                continue; // Tie

            switch (match.getWinner()) {
                case HOME -> tournamentPoints.merge(match.getHomeTeam().getName(), getPoints(match), Integer::sum);
                case AWAY -> tournamentPoints.merge(match.getAwayTeam().getName(), getPoints(match), Integer::sum);
            }
        }
    }

    @Override
    public int getPoints(Match match) {
        int diff = match.getScoreHome() - match.getScoreAway();
        diff = diff < 0 ? -diff : diff;
        /*
            Score differences:
            0 -> 0 points
            10-40 -> 2 points
            50-90 -> 3 points
            100-140 -> 5 points
            >=150 -> 7 points
         */
        if (diff == 0)
            return 0;
        else if (diff < 50)
            return 2;
        else if (diff < 100)
            return 3;
        else if (diff < 150)
            return 5;
        else
            return 7;
    }

    private void assignTeams(boolean flag, Match match, Team aTeam, Team bTeam, Map<String, Integer> homeTeamTimes) {
        if (flag) {
            match.setHomeTeam(aTeam);
            match.setLocation(aTeam.getHome());
            match.setAwayTeam(bTeam);
            homeTeamTimes.merge(aTeam.getName(), 1, Integer::sum);
        } else {
            match.setHomeTeam(bTeam);
            match.setLocation(bTeam.getHome());
            match.setAwayTeam(aTeam);
            homeTeamTimes.merge(bTeam.getName(), 1, Integer::sum);
        }
    }

    private void swapMatchTeams(Match first, Match other) {
        if (first == other)
            return;
        Team tempHome = first.getHomeTeam();
        Team tempAway = first.getAwayTeam();
        first.setHomeTeam(other.getHomeTeam());
        first.setLocation(other.getHomeTeam().getHome());
        first.setAwayTeam(other.getAwayTeam());
        other.setHomeTeam(tempHome);
        other.setLocation(tempHome.getHome());
        other.setAwayTeam(tempAway);
    }


    private LocalDate nextMatchDate(LocalDate currentDate, DayOfWeek nextDay) {
        DayOfWeek currentDay = currentDate.getDayOfWeek();

        int difference = nextDay.getValue() - currentDay.getValue();
        if (difference == 0)
            return currentDate;

        return currentDate.plusDays(difference);

    }
}
