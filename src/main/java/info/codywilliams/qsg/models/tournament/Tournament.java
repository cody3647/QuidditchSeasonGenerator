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
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.output.Element;
import info.codywilliams.qsg.output.Page;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tournament {
    protected ObjectProperty<TournamentType> type;
    protected IntegerProperty numWeeks;
    protected IntegerProperty numMatches;
    protected IntegerProperty numRounds;
    protected SetProperty<Match> matches;
    protected TreeSet<TimeEntry> template;
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


    public Tournament(TournamentOptions tournamentOptions, TournamentType type) {

        this.type = new SimpleObjectProperty<>(this, "type", type);
        numWeeks = new SimpleIntegerProperty(this, "numWeeks", 0);
        numMatches = new SimpleIntegerProperty(this, "numMatches", 0);
        numRounds = new SimpleIntegerProperty(this, "numRounds", 0);

        matches = new SimpleSetProperty<>(this, "matches", FXCollections.observableSet(new TreeSet<>()));
        endDate = new SimpleObjectProperty<>(this, "endDate");
        this.tournamentOptions = tournamentOptions;
        tournamentPoints = new SimpleMapProperty<>(this, "tournamentPoints", FXCollections.observableMap(new ConcurrentHashMap<>()));


        endDateStringBinding = Bindings.createStringBinding(() -> {
                    if(endDateProperty().getValue() == null)
                        return "";
                    return endDateProperty().getValue().format(Formatters.dateFormatter);
        }, endDateProperty());

        teamsAssigned = new SimpleBooleanProperty(this, "teamsAssigned", false);
    }



    public void recalculateTournament(int numTeams) {
        if(numTeams <2)
            return;

        calculateNums(numTeams);
        LocalDateTime lastMatchDate = calculateMatchDates();
        setEndDate(lastMatchDate.toLocalDate());
        teamsAssigned.set(false);
    }

    protected abstract void calculateNums(Integer numTeams);

    protected boolean zeroCheckNums(int totalMatches, int totalRounds){
        if(totalMatches <= 0 && totalRounds <= 0){
            setNumMatches(0);
            setNumMatches(0);

            return true;
        }
        return false;
    }

    protected abstract LocalDateTime calculateMatchDates();
    protected int isDateInBlackout(LocalDate date, BlackoutDates blackoutDates){
        if(blackoutDates == null)
            return 0;

        if(date.isAfter(blackoutDates.getStart()) && date.isBefore(blackoutDates.getEnd()) || date.isEqual(blackoutDates.getStart()))
            return 1;
        if(date.isEqual(blackoutDates.getEnd()))
            return 2;

        return 0;
    }

    public void generateMatches(List<Team> teams, long seed) {
        if(!isTeamsAssigned())
            assignTeamsToMatches(teams, seed);

        for(Team team: teams)
            tournamentPoints.put(team.getName(), 0);

        MatchGenerator matchGenerator = new MatchGenerator(seed);
        long now = System.currentTimeMillis();
        for (Match match : getMatches()) {
            matchGenerator.setUpMatch(match);
            matchGenerator.generate();
            matchGenerator.cleanUp();
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

        for(Match match: matches) {
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
        seasonPage.addMetadata("keywords", null, resources.getString("meta.tournament.keywords"), null);

        Div key = new Div(
                new Header(2, resources.getString("tournament.key.header")),
                new Paragraph(resources.getString("tournament.key.winner"))
        );
        key.addClass("tournament-key");
        seasonPage.addBodyContent(key);


        LinkedList<Element> descriptionParagraphs = new LinkedList<>();
        for(String text: resources.getString("description." + getType().key).split("\n"))
            descriptionParagraphs.add(new Paragraph(text));
        seasonPage.addBodyContent(descriptionParagraphs);

        Header scheduleHeader = new Header(2, resources.getString("header.schedule"));
        seasonPage.addBodyContent(scheduleHeader);

        DefinitionList openingDayDef = new DefinitionList();
        seasonPage.addBodyContent(openingDayDef);
        openingDayDef.addChildren(new DefinitionList.Term(resources.getString("openingDay")));
        openingDayDef.addChildren(new DefinitionList.Defintion(getTournamentOptions().getStartDate().format(Formatters.dateFormatter)));

        Table matchTable = new Table();
        matchTable.addClass("league-schedule");

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
        seasonPage.addBodyContent(rankingsHeader, rankingsDesc);

        return seasonPage;
    }

    public TableRow[] matchTableRoundHeader(int roundNum) {
        TableData.Header roundHeader = new TableData.Header(resources.getString("header.round") + roundNum);
        roundHeader.addAttribute("colspan", "6");

        TableData.Header[] columnHeaders = new TableData.Header[]{
                new TableData.Header(resources.getString("header.date")),
                new TableData.Header(resources.getString("header.home")),
                new TableData.Header(resources.getString("header.away")),
                new TableData.Header(resources.getString("header.location")),
                new TableData.Header(resources.getString("header.length")),
                new TableData.Header(resources.getString("header.score")),
                new TableData.Header(resources.getString("header.points"))
        };

        return new TableRow[]{new TableRow(roundHeader), new TableRow(columnHeaders)};
    }

    public TableRow matchTableRow(Match match) {
        TableData date = new TableData(new Link.Match(match.getStartDateTime().format(Formatters.dateFormatter) + " at " + match.getStartDateTime().format(Formatters.timeFormatter), match.getTitle()));
        TableData home = new TableData(new Link.Team(match.getHomeTeam().getName(), match.getHomeTeam().getName()));
        TableData away = new TableData(new Link.Team(match.getAwayTeam().getName(), match.getAwayTeam().getName()));
        TableData location = new TableData(match.getLocation());
        TableData length = new TableData(Formatters.formatDuration(match.getMatchLength()));
        TableData score = new TableData(match.getScoreHome() + " - " + match.getScoreAway());
        TableData points = new TableData(String.valueOf(getPoints(match)));

        date.addClass("match-date");
        home.addClass("match-home");
        away.addClass("match-away");
        location.addClass("match-location");
        length.addClass("match-length");
        score.addClass("match-score");
        points.addClass("match-points");
        switch (match.getWinner()) {
            case HOME -> {home.addClass("match-winner"); away.addClass("match-loser");}
            case AWAY -> {home.addClass("match-loser"); away.addClass("match-winner");}
        }
        return new TableRow(date, home, away, location, length, score, points);
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

    public StringBinding endDateStringBinding(){
        return endDateStringBinding;
    }

    public TreeSet<TimeEntry> getTemplate() {
        return template;
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
