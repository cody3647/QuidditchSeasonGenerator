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

import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.tournament.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class StraightRoundRobin extends Tournament {
    int numMatchesPerRound;
    int numMatchesPerWeek;
    int numMatchesPerDay;

    public StraightRoundRobin(TournamentOptions tournamentOptions) {
        super(tournamentOptions, TournamentType.STRAIGHT_ROUND_ROBIN);
    }

    public StraightRoundRobin(TournamentOptions tournamentOptions, TournamentType type) {
        super(tournamentOptions, type);

    }

    @Override
    protected void calculateNums(Integer numTeams) {
        int totalMatches = (int) ((numTeams / 2.0) * (numTeams - 1));
        int totalRounds = numTeams % 2 == 0 ? numTeams - 1 : numTeams;

        if (zeroCheckNums(totalMatches, totalRounds)) {
            numMatchesPerRound = 0;
            numMatchesPerWeek = 0;
            numMatchesPerDay = 0;
            return;
        }

        setNumMatches(totalMatches);
        setNumRounds(totalRounds);

        calculateRoundRobinNumbers(numTeams);

        System.out.printf("Num Teams: %d\nTotal Matches: %d\nTotal Rounds: %d\nMatches Per Round: %d\nMatches Per Week: %d\nMatches Per Day: %d\n\n",
                numTeams, totalMatches, totalRounds, numMatchesPerRound, numMatchesPerWeek, numMatchesPerDay);

    }

    protected void calculateRoundRobinNumbers(int numTeams) {
        int validDays = (int) tournamentOptions.getValidStartTimes().stream().filter(ValidStartTime::getEnableDay).count();
        numMatchesPerRound = (numTeams % 2 == 0) ? (numTeams / 2) : ((numTeams - 1) / 2);
        numMatchesPerWeek = (int) Math.ceil(tournamentOptions.getRoundsPerWeek() * numMatchesPerRound);
        numMatchesPerDay = (int) Math.ceil(numMatchesPerWeek / (double) validDays);
    }

    @Override
    protected LocalDateTime calculateMatchDates() {

        template = makeMatchTemplate();

        if (template == null)
            return null;

        // Move template to first acceptable day of the week selected in get start date
        LocalDate date = tournamentOptions.getStartDate();
        DayOfWeek dayMatchesStart = tournamentOptions.getValidStartDay();

        // Setup iterators
        List<BlackoutDates> blackoutDatesList = tournamentOptions.getBlackoutDates();
        blackoutDatesList.sort(null);
        Iterator<BlackoutDates> blackoutDatesIterator = blackoutDatesList.listIterator();
        Iterator<TimeEntry> timeEntryIterator = template.iterator();

        // Setup All Round Set<Dates>
        matches.clear();

        BlackoutDates blackoutDates = null;
        if (blackoutDatesIterator.hasNext())
            blackoutDates = blackoutDatesIterator.next();

        int totalMatchCount = 0;
        int roundMatchCount = 0;
        int round = 1;
        int compare;
        LocalDateTime localDateTime = null;

        while (totalMatchCount < numMatches.get()) {
            // Have we finished a round?
            if (roundMatchCount == numMatchesPerRound) {
                // Reset round counter, start the week template over again
                roundMatchCount = 0;
                timeEntryIterator = template.iterator();
                round++;

                if(totalMatchCount % numMatchesPerWeek == 0)
                    // Adjust date to the next week
                    date = date.with(TemporalAdjusters.next(dayMatchesStart));

            }
            // Have we split a round across weeks?
            else if (roundMatchCount != 0 && roundMatchCount % numMatchesPerWeek == 0) {
                timeEntryIterator = template.iterator();
                // Adjust date to the next week
                date = date.with(TemporalAdjusters.next(dayMatchesStart));
            }

            if(!timeEntryIterator.hasNext())
                throw new RuntimeException("Ran out of time entries before the end of a week or round");
            TimeEntry timeEntry = timeEntryIterator.next();

            while ((compare = isDateInBlackout(date, blackoutDates)) > 0) {
                date = date.plusDays(1);

                if (compare == 2) {
                    if (blackoutDatesIterator.hasNext()) {
                        blackoutDates = blackoutDatesIterator.next();
                    } else
                        blackoutDates = null;
                }

            }

            while (!date.getDayOfWeek().equals(timeEntry.getDayOfWeek())) {
                date = date.plusDays(1);
            }
            localDateTime = timeEntry.getLocalTime().atDate(date);

            int matchDayCount = 0;
            for(int i = 0; i < timeEntry.getCount(); i++) {
                matches.add(new Match(roundMatchCount + 1, round, localDateTime));
                roundMatchCount++;
                totalMatchCount++;
                matchDayCount++;

                if(matchDayCount == numMatchesPerDay)
                    break;
            }

            if (!timeEntryIterator.hasNext() && totalMatchCount < getNumMatches()) {
                timeEntryIterator = template.iterator();
            }
        }

        return localDateTime;
    }


    TreeSet<TimeEntry> makeMatchTemplate() {
        TreeMap<String, TimeEntry> template = new TreeMap<>();

        Iterator<ValidStartTime> iterator = tournamentOptions.getValidStartTimes().iterator();
        int hoursBetweenMatches = tournamentOptions.getHoursBetweenMatches();

        int matches = 0;
        LocalTime midnight = LocalTime.MAX;

        while (iterator.hasNext()) {


            ValidStartTime validStartTime = iterator.next();
            if (!validStartTime.getEnableDay())
                continue;

            // Add the matches to try to schedule for the day to the number left over from previous day
            matches += numMatchesPerDay;
            LocalTime time;
            // When hours between matches is 0, matches will be equally divided between valid days starting at the latest time available
            if (hoursBetweenMatches == 0)
                time = validStartTime.getLatest();
            else
                time = validStartTime.getEarliest();
            // Add a minute to the end so that adding hours will not land on latest time and be ineligible
            LocalTime end = validStartTime.getLatest().plusMinutes(1);

            // Loop through the acceptable time adding a match every hoursBetweenMatches
            while (matches > 0 && time.isBefore(end)) {
                String key = Integer.toString(validStartTime.getDayOfWeek().getValue()) + time;
                if (template.containsKey(key))
                    template.get(key).incrementCount();
                else
                    template.put(key, new TimeEntry(validStartTime.getDayOfWeek(), time));
                matches--;
                // If we are going to wrap around midnight, don't.
                if (midnight.minusHours(hoursBetweenMatches).isBefore(time))
                    break;
                time = time.plusHours(hoursBetweenMatches);
            }

            // If all matches aren't at the same time, move the last match for each day to the last valid start time
            if (hoursBetweenMatches != 0) {
                Map.Entry<String, TimeEntry> last = template.lastEntry();
                TimeEntry newEntry = new TimeEntry(last.getValue().getDayOfWeek(), end.minusMinutes(1));
                newEntry.setCount(last.getValue().getCount());
                template.put(last.getKey(), newEntry);
            }
        }

        if (template.isEmpty())
            return null;

        // Fill in the remaining matches
        if (matches > 0) {
            // Order by fewest matches on the day, then by Fri-Sun-Mon-Thu then by descending time
            Comparator<TimeEntry> comparator = Comparator.comparingInt(TimeEntry::getCount).thenComparingInt(TimeEntry::fridayZero).thenComparing(TimeEntry::getLocalTime, Comparator.reverseOrder());
            TreeSet<TimeEntry> templateByCount = new TreeSet<>(comparator);
            templateByCount.addAll(template.values());

            while (matches > 0) {
                TimeEntry timeEntry = templateByCount.pollFirst();

                timeEntry.incrementCount();

                templateByCount.add(timeEntry);
                matches--;
            }
            System.out.println(matches);
        }
        return new TreeSet<>(template.values());
    }

    @Override
    public TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed) {
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
            if(getType() == TournamentType.STRAIGHT_ROUND_ROBIN || (round <= half && getType() == TournamentType.STRAIGHT_ROUND_ROBIN_HOME_AWAY)){
                int aCount = homeTeamTimes.getOrDefault(teamArrayList.get(aIdx).getName(), 0);
                int bCount = homeTeamTimes.getOrDefault(teamArrayList.get(bIdx).getName(), 0);

                assignTeams(aCount < bCount, match, teamArrayList.get(aIdx), teamArrayList.get(bIdx), homeTeamTimes);
            }
            else {
                if(!reset && round == half + 1){
                    homeTeamTimes.clear();
                    reset = true;
                }
                int aCount = homeTeamTimes.getOrDefault(teamArrayList.get(aIdx).getName(), 0);
                int bCount = homeTeamTimes.getOrDefault(teamArrayList.get(bIdx).getName(), 0);

                // Reverse comparison and teamA and teamB so that when counts are equal or different behavior is reversed from above
                assignTeams(aCount > bCount, match, teamArrayList.get(bIdx), teamArrayList.get(aIdx), homeTeamTimes);
            }

            aIdx++;
            bIdx--;
        }

        // The fixed team in the schedule will always play first match, randomize this some.
        int randInt = 0;
        Match firstMatch = null;
        Match otherMatch;
        for(Match match: getMatches()){
            // Swap first match with random other match in round
            if(match.getNumber() == 1){
                firstMatch = match;
                randInt = rand.nextInt(numMatchesPerRound) + 1;
            }
            if(match.getNumber() == randInt){
                otherMatch = match;
                swapMatchTeams(firstMatch, otherMatch);
                randInt = 0;
                firstMatch = null;
            }
        }
        return new TreeSet<>(matches.getValue());
    }

    private void assignTeams(boolean flag, Match match, Team aTeam, Team bTeam, Map<String, Integer> homeTeamTimes){
        if(flag){
            match.setHomeTeam(aTeam);
            match.setLocation(aTeam.getHome());
            match.setAwayTeam(bTeam);
            homeTeamTimes.merge(aTeam.getName(), 1, Integer::sum);
        }
        else {
            match.setHomeTeam(bTeam);
            match.setLocation(bTeam.getHome());
            match.setAwayTeam(aTeam);
            homeTeamTimes.merge(bTeam.getName(), 1, Integer::sum);
        }
    }

    private void swapMatchTeams(Match first, Match other){
        if(first == other)
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


}
