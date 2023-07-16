/*
 * Copyright (c) 2022. Cody Williams
 *
 * Tournament.java is part of Quidditch Season Generator.
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

package info.codywilliams.qsg.models.tournament;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.codywilliams.qsg.generators.MatchGenerator;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.output.Element;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.output.QsgNote;
import info.codywilliams.qsg.output.TableOfContents;
import info.codywilliams.qsg.output.elements.*;
import info.codywilliams.qsg.util.DependencyInjector;
import info.codywilliams.qsg.util.Formatters;
import info.codywilliams.qsg.util.ResourceBundleReplacer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tournament {
    protected ObjectProperty<TournamentType> type;
    protected IntegerProperty numWeeks;
    protected IntegerProperty numMatches;
    protected IntegerProperty numRounds;
    protected IntegerProperty numMatchesPerRound;
    protected SetProperty<Match> matches;
    protected ObjectProperty<LocalDate> endDate;
    protected StringBinding endDateStringBinding;
    protected TournamentOptions tournamentOptions;
    protected SimpleMapProperty<String, Integer> tournamentPoints;
    protected SimpleBooleanProperty teamsAssigned;
    @JsonIgnore
    private ResourceBundleReplacer resources;
    @JsonIgnore
    private String tournamentTitle;
    @JsonIgnore
    private String yearRange;
    @JsonIgnore
    protected ArrayList<Team> teamList;


    public Tournament(TournamentOptions tournamentOptions, TournamentType type) {

        this.type = new SimpleObjectProperty<>(this, "type", type);
        numWeeks = new SimpleIntegerProperty(this, "numWeeks", 0);
        numMatches = new SimpleIntegerProperty(this, "numMatches", 0);
        numRounds = new SimpleIntegerProperty(this, "numRounds", 0);

        numMatchesPerRound = new SimpleIntegerProperty(this, "numMatchesPerRound", 0);

        matches = new SimpleSetProperty<>(this, "matches", FXCollections.observableSet(new TreeSet<>()));
        endDate = new SimpleObjectProperty<>(this, "endDate");
        this.tournamentOptions = tournamentOptions;
        tournamentPoints = new SimpleMapProperty<>(this, "tournamentPoints", FXCollections.observableMap(new ConcurrentHashMap<>()));


        endDateStringBinding = Bindings.createStringBinding(() -> {
            if (endDateProperty().getValue() == null)
                return "";
            return endDateProperty().getValue().format(Formatters.dateFormatter);
        }, endDateProperty());

        teamsAssigned = new SimpleBooleanProperty(this, "teamsAssigned", false);
    }


    public void recalculateTournament(int numTeams) {
        if (numTeams < 2)
            return;

        calculateNums(numTeams);
        LocalDate lastMatchDate = calculateMatchDates();
        setEndDate(lastMatchDate);
        if (teamList != null)
            teamList.clear();
        teamsAssigned.set(false);
    }

    protected void calculateNums(int numTeams) {
        int totalMatches = calculateTotalMatches(numTeams);
        int totalRounds = calculateTotalRounds(numTeams);

        if (zeroCheckNums(totalMatches, totalRounds)) return;

        setNumMatches(totalMatches);
        setNumRounds(totalRounds);

        int matchesPerRound = calculateMatchesPerRound(numTeams);
        setNumMatchesPerRound(matchesPerRound);

        System.out.printf("Num Teams: %d\nTotal Matches: %d\nTotal Rounds: %d\nMatches Per Round: %d\n",
                numTeams, getNumMatches(), getNumRounds(), getNumMatchesPerRound());
    }

    protected abstract int calculateTotalMatches(int numTeams);

    protected abstract int calculateTotalRounds(int numTeams);

    protected abstract int calculateMatchesPerRound(int numTeams);

    protected boolean zeroCheckNums(int totalMatches, int totalRounds) {
        if (totalMatches <= 0 && totalRounds <= 0) {
            setNumMatches(0);
            setNumRounds(0);
            setNumWeeks(0);
            setNumMatchesPerRound(0);
            return true;
        }
        return false;
    }

    protected abstract LocalDate calculateMatchDates();

    @JsonIgnore
    protected Set<LocalDate> getBlackoutDateSet() {
        Set<LocalDate> dates = new HashSet<>();

        for (BlackoutDates blackoutDates : tournamentOptions.getBlackoutDates()) {
            LocalDate date = blackoutDates.getStart();
            while (date.isBefore(blackoutDates.getEnd()) || date.isEqual(blackoutDates.getEnd())) {
                dates.add(date);
                date = date.plusDays(1);
            }
        }

        return dates;
    }

    public void generateMatches(List<Team> teams, long seed) {
        if (!isTeamsAssigned())
            assignTeamsToMatches(teams, seed);

        for (Team team : teams)
            tournamentPoints.put(team.getName(), 0);

        MatchGenerator matchGenerator = new MatchGenerator(seed);
        long now = System.currentTimeMillis();
        for (Match match : getMatches()) {
            matchGenerator.run(match);
        }
        now = System.currentTimeMillis() - now;
        assignPoints();
        System.out.println(now / 1000.0 + " seconds to generate matches");
    }

    public abstract TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed);

    public List<Page> buildPages(List<Team> teams, long seed) {
        generateMatches(teams, seed);

        long now = System.currentTimeMillis();
        yearRange = getTournamentOptions().getStartDate().getYear() + "-" + getEndDate().getYear();
        resources = new ResourceBundleReplacer(DependencyInjector.getBundle());
        resources.addToken("leagueName", tournamentOptions.getLeagueName());
        resources.addToken("yearRange", yearRange);

        tournamentTitle = resources.getString("tournamentTitle");

        List<Page> pages = new ArrayList<>();

        for (Match match : matches) {
            match.setResources(resources);
            pages.add(match.buildMatchPage());
        }

        pages.add(buildTournamentPage(tournamentTitle));
        now = System.currentTimeMillis() - now;
        System.out.println(now / 1000.0 + " seconds to generate pages");
        return pages;
    }

    public Page buildTournamentPage(String title) {
        Page seasonPage = new Page(title, "index");
        seasonPage.addStyle("QuidditchGenerator.css");
        seasonPage.addMetadata("keywords", null, resources.getString("meta.tournament.keywords"), null);

        Image.Gallery teamGallery = new Image.Gallery("packed");
        teamList.stream()
                .map(Team::getName)
                .map(teamName -> new Image(teamName, teamName + ".png"))
                .forEach(teamGallery::addImages);

        seasonPage.addBodyContent(teamGallery);
        seasonPage.addBodyContent(new TableOfContents());


        LinkedList<Element> descriptionParagraphs = new LinkedList<>();
        for (String text : resources.getString("description." + getType().key).split("\n"))
            descriptionParagraphs.add(new Paragraph(text));
        seasonPage.addBodyContent(descriptionParagraphs);

        seasonPage.addBodyContent(new QsgNote());


        Header scheduleHeader = new Header(2, resources.getString("header.schedule"));
        seasonPage.addBodyContent(scheduleHeader);

        DefinitionList openingDayDef = new DefinitionList(
                new DefinitionList.Term(resources.getString("openingDay")),
                new DefinitionList.Def(getTournamentOptions().getStartDate().format(Formatters.dateFormatter))
        );
        openingDayDef.addClass("opening-day");

        DefinitionList.Def winner = new DefinitionList.Def(resources.getString("tournament.key.winner"));
        winner.addClass("match-winner");
        DefinitionList key = new DefinitionList(
                new DefinitionList.Term(resources.getString("tournament.key.header")),
                winner
        );
        key.addClass("tournament-key");

        Div leagueScheduleTopper = new Div(openingDayDef, key);
        leagueScheduleTopper.addClass("league-schedule-topper");
        seasonPage.addBodyContent(leagueScheduleTopper);

        Table matchTable = new Table();
        matchTable.addClass("league-schedule", "wikitable");

        int round = 0;
        for (Match match : getMatches()) {
            if (round != match.getRound()) {
                round = match.getRound();
                matchTable.addChildren(matchTableRoundHeader(round));
            }

            matchTable.addChildren(matchTableRow(match));
        }

        seasonPage.addBodyContent(matchTable);

        Header rankingsHeader = new Header(2, resources.getString("header.rankings"));
        Paragraph rankingsDesc = new Paragraph(resources.getString("rankings"));

        ArrayList<Map.Entry<String, Integer>> rankings = new ArrayList<>(tournamentPoints.entrySet());
        rankings.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Table rankingTable = new Table();
        rankingTable.addClass("league-rankings", "wikitable");

        rankingTable.addChildren(
                new Table.Row(
                        new Table.HeaderCell(resources.getString("header.rank.team")),
                        new Table.HeaderCell(resources.getString("header.rank.points"))
                )
        );

        int i = 1;
        for (Map.Entry<String, Integer> entry : rankings) {
            rankingTable.addChildren(
                    new Table.Row(
                            new Table.Cell(i + ": " + entry.getKey()),
                            new Table.Cell(String.valueOf(entry.getValue()))
                    )
            );
            i++;
        }

        seasonPage.addBodyContent(rankingsHeader, rankingsDesc, rankingTable);

        return seasonPage;
    }

    public Table.Row[] matchTableRoundHeader(int roundNum) {
        Table.HeaderCell roundHeaderCell = new Table.HeaderCell(resources.getString("header.round") + roundNum);
        roundHeaderCell.addAttribute("colspan", "7");

        Table.HeaderCell[] columnHeaderCells = new Table.HeaderCell[]{
                new Table.HeaderCell(resources.getString("header.date")),
                new Table.HeaderCell(resources.getString("header.home")),
                new Table.HeaderCell(resources.getString("header.away")),
                new Table.HeaderCell(resources.getString("header.location")),
                new Table.HeaderCell(resources.getString("header.length")),
                new Table.HeaderCell(resources.getString("header.score")),
                new Table.HeaderCell(resources.getString("header.points"))
        };

        return new Table.Row[]{new Table.Row(roundHeaderCell), new Table.Row(columnHeaderCells)};
    }

    public Table.Row matchTableRow(Match match) {
        Table.Cell date = new Table.Cell(Link.TextLink.createMatchLink(match.getStartDateTime().format(Formatters.dateFormatter) + " at " + match.getStartDateTime().format(Formatters.timeFormatter), match.getTitle()));
        Table.Cell home = new Table.Cell(Link.TextLink.createTeamLink(match.getHomeTeam().getName()));
        Table.Cell away = new Table.Cell(Link.TextLink.createTeamLink(match.getAwayTeam().getName()));
        Table.Cell location = new Table.Cell(match.getLocation());
        Table.Cell length = new Table.Cell(Formatters.formatDuration(match.getMatchLength()));
        Table.Cell score = new Table.Cell(match.getScoreHome() + " - " + match.getScoreAway());
        Table.Cell points = new Table.Cell(String.valueOf(getPoints(match)));

        date.addClass("match-date");
        home.addClass("match-home");
        away.addClass("match-away");
        location.addClass("match-location");
        length.addClass("match-length");
        score.addClass("match-score");
        points.addClass("match-points");
        if (match.getWinner() != null)
            switch (match.getWinner()) {
                case HOME -> {
                    home.addClass("match-winner");
                    away.addClass("match-loser");
                }
                case AWAY -> {
                    home.addClass("match-loser");
                    away.addClass("match-winner");
                }
            }
        return new Table.Row(date, home, away, location, length, score, points);
    }

    protected abstract void assignPoints();

    public abstract int getPoints(Match match);

    public TournamentType getType() {
        return type.get();
    }

    public ObjectProperty<TournamentType> typeProperty() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type.set(type);
    }

    public int getNumWeeks() {
        return numWeeks.get();
    }

    public IntegerProperty numWeeksProperty() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks.set(numWeeks);
    }

    public int getNumMatches() {
        return numMatches.get();
    }

    public IntegerProperty numMatchesProperty() {
        return numMatches;
    }

    public void setNumMatches(int numMatches) {
        this.numMatches.set(numMatches);
    }

    public int getNumRounds() {
        return numRounds.get();
    }

    public IntegerProperty numRoundsProperty() {
        return numRounds;
    }

    public void setNumRounds(int numRounds) {
        this.numRounds.set(numRounds);
    }

    public int getNumMatchesPerRound() {
        return numMatchesPerRound.get();
    }

    public IntegerProperty numMatchesPerRoundProperty() {
        return numMatchesPerRound;
    }

    public void setNumMatchesPerRound(int numMatchesPerRound) {
        this.numMatchesPerRound.set(numMatchesPerRound);
    }

    public ObservableSet<Match> getMatches() {
        return matches.get();
    }

    public SetProperty<Match> matchesProperty() {
        return matches;
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }

    public StringBinding endDateStringBinding() {
        return endDateStringBinding;
    }

    public boolean isTeamsAssigned() {
        return teamsAssigned.get();
    }

    public SimpleBooleanProperty teamsAssignedProperty() {
        return teamsAssigned;
    }

    public TournamentOptions getTournamentOptions() {
        return tournamentOptions;
    }

    public ObservableMap<String, Integer> getTournamentPoints() {
        return tournamentPoints.get();
    }

    public SimpleMapProperty<String, Integer> tournamentPointsProperty() {
        return tournamentPoints;
    }

    public String getTournamentTitle() {
        return tournamentTitle;
    }
}
