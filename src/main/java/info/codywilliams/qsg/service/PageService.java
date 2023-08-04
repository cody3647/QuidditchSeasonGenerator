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

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.*;
import info.codywilliams.qsg.models.player.Player;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.output.*;
import info.codywilliams.qsg.output.elements.*;
import info.codywilliams.qsg.util.Formatters;
import info.codywilliams.qsg.util.ResourceBundleReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PageService {
    @JsonIgnore
    final private ResourceBundleReplacer resources;
    @JsonIgnore
    private String tournamentTitle;
    @JsonIgnore
    private String yearRange;
    @JsonIgnore
    protected List<Team> teamList;
    protected long seed;
    @JsonIgnore
    private String generatorVersionUsed;

    protected Logger logger = LoggerFactory.getLogger(PageService.class);

    private Tournament tournament;
    private TournamentOptions tournamentOptions;

    public PageService(ResourceBundle resources) {
        this.resources = new ResourceBundleReplacer(resources);
    }

    public List<Page> buildPages(Tournament tournament, List<Team> teamList, long seed) {
        this.tournament = tournament;
        this.tournamentOptions = tournament.getTournamentOptions();
        this.teamList = teamList;
        this.seed = seed;
        MatchGenerator matchGenerator = MatchGenerator.create(this.seed, 1);
        matchGenerator.generateMatches(tournament, teamList);
        generatorVersionUsed = String.valueOf(matchGenerator.getVersion());

        long now = System.currentTimeMillis();
        yearRange = tournamentOptions.getStartDate().getYear()
                + "-" + tournament.getEndDate().getYear();
        resources.addToken("leagueName", tournamentOptions.getLeagueName());
        resources.addToken("yearRange", yearRange);

        tournamentTitle = resources.getString("tournamentTitle");

        List<Page> pages = new ArrayList<>();

        for (Match match : tournament.getMatches()) {
            pages.add(buildMatchPage(match));
        }

        pages.add(0, buildTournamentPage(tournamentTitle, seed));

        now = System.currentTimeMillis() - now;
        logger.info("{} seconds to generate pages", now / 1000.0);
        return pages;
    }

    private Page buildTournamentPage(String title, long seed) {
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
        for (String text : resources.getString("description." + tournament.getType().key).split("\n"))
            descriptionParagraphs.add(new Paragraph(text));
        seasonPage.addBodyContent(descriptionParagraphs);

        seasonPage.addBodyContent(new QsgNote());


        Header scheduleHeader = new Header(2, resources.getString("header.schedule"));
        seasonPage.addBodyContent(scheduleHeader);

        DefinitionList openingDayDef = new DefinitionList(
                new DefinitionList.Term(resources.getString("openingDay")),
                new DefinitionList.Def(tournamentOptions.getStartDate().format(
                        DateTimeFormatter.ofPattern("EEEE',' d LLLL yyyy").withZone(ZoneId.systemDefault())
                ))
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
        for (Match match : tournament.getMatches()) {
            if (round != match.getRound()) {
                round = match.getRound();
                matchTable.addChildren(matchTableRoundHeader(round));
            }

            matchTable.addChildren(matchTableRow(match));
        }

        seasonPage.addBodyContent(matchTable);

        Header rankingsHeader = new Header(2, resources.getString("header.rankings"));
        Paragraph rankingsDesc = new Paragraph(resources.getString("rankings"));

        ArrayList<Map.Entry<String, Integer>> rankings = new ArrayList<>(tournament.getTournamentPoints().entrySet());
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

        DefinitionList version = new DefinitionList(
                new DefinitionList.Term(resources.getString("generator.version")),
                new DefinitionList.Def(generatorVersionUsed)
        );
        version.addClass("generator-version");


        DefinitionList seedDl = new DefinitionList(
                new DefinitionList.Term(resources.getString("generator.seed")),
                new DefinitionList.Def(HexFormat.of().withUpperCase().toHexDigits(seed))
        );
        seedDl.addClass("generator-seed");

        Div footer = new Div(version, seedDl);
        footer.addClass("league-footer");
        seasonPage.addBodyContent(footer);

        return seasonPage;
    }

    private Table.Row[] matchTableRoundHeader(int roundNum) {
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

    private Table.Row matchTableRow(Match match) {
        Table.Cell date = new Table.Cell(Link.TextLink.createMatchLink(match.getStartDateTime().format(Formatters.dateTimeFormatter), match.getTitle()));
        Table.Cell home = new Table.Cell(Link.TextLink.createTeamLink(match.getHomeTeam().getName()));
        Table.Cell away = new Table.Cell(Link.TextLink.createTeamLink(match.getAwayTeam().getName()));
        Table.Cell location = new Table.Cell(match.getLocation());
        Table.Cell length = new Table.Cell(Formatters.formatDuration(match.getMatchLength()));
        Table.Cell score = new Table.Cell(match.getScoreHome() + " - " + match.getScoreAway());
        Table.Cell points = new Table.Cell(String.valueOf(tournament.getPoints(match)));

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

    private void setResourceMatchTokens(Team homeTeam, Team awayTeam, LocalDateTime startDateTime) {
        this.resources.addToken("date", startDateTime.toLocalDate().format(Formatters.dateFormatter));
        this.resources.addToken("homeTeam", homeTeam.getName());
        if (homeTeam.getShortName().isEmpty())
            this.resources.addToken("homeTeamShort", homeTeam.getName());
        else
            this.resources.addToken("homeTeamShort", homeTeam.getShortName());
        this.resources.addToken("awayTeam", awayTeam.getName());
        if (awayTeam.getShortName().isEmpty())
            this.resources.addToken("awayTeamShort", awayTeam.getName());
        else
            this.resources.addToken("awayTeamShort", awayTeam.getShortName());
    }

    private Page buildMatchPage(Match match) {
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        setResourceMatchTokens(homeTeam, awayTeam, match.getStartDateTime());
        String title = resources.getString("match.title");
        match.setTitle(title);

        Page matchPage = new Page(title, title);
        matchPage.addStyle("QuidditchGenerator.css");
        matchPage.addMetadata("keywords", null, resources.getString("meta.match.keywords"), null);
        matchPage.addBodyContent(new MatchInfobox(match, resources));

        matchPage.addBodyContent(new Header(2, "Match Rosters"));
        matchPage.addBodyContent(buildRosters(homeTeam.getName(), match.getHomeTeamRoster()));
        matchPage.addBodyContent(buildRosters(awayTeam.getName(), match.getAwayTeamRoster()));

        matchPage.addBodyContent(new Header(2, "Injured Players"));
        matchPage.addBodyContent(buildInjuredPlayersTable(homeTeam.getName(), match.getHomeInjuredBefore(), match.getHomeInjuredDuring()));
        matchPage.addBodyContent(buildInjuredPlayersTable(awayTeam.getName(), match.getAwayInjuredBefore(), match.getAwayInjuredDuring()));

        matchPage.addBodyContent(new Header(2, "Match"));
        UnorderedList playList = new UnorderedList();
        playList.addClass("quidditch-match");

        int i = 0;
        for (Play play : match.getPlays()) {
            i++;
            UnorderedList.Item li = new UnorderedList.Item();
            playList.addChildren(li);
            List<Element> liChildren = new ArrayList<>();
            liChildren.add(new Text(play.outputWithDetails(resources, homeTeam.getName(), awayTeam.getName())));

            if (play instanceof PlayFoul playFoul) {
                li.addClass(
                        "quidditch-foul",
                        "quaffle-" + playFoul.getQuaffleOutcome().name().toLowerCase(),
                        play.getAttackingTeamType().name().toLowerCase()
                );
                if (playFoul.getQuaffleOutcome() == PlayChaser.QuaffleOutcome.SCORED) {
                    liChildren.add(buildScoreDiv(homeTeam, awayTeam, playFoul, false));
                    i = 0;
                }
            } else if (play instanceof PlayChaser playChaser) {
                li.addClass(
                        "quaffle-" + playChaser.getQuaffleOutcome().name().toLowerCase(),
                        play.getAttackingTeamType().name().toLowerCase()
                );
                if (playChaser.getQuaffleOutcome() == PlayChaser.QuaffleOutcome.SCORED) {
                    liChildren.add(buildScoreDiv(homeTeam, awayTeam, playChaser, false));
                    i = 0;
                }
            } else if (play instanceof PlaySeeker playSeeker) {
                li.addClass(
                        "snitch-" + playSeeker.getSnitchOutcome().name().toLowerCase(),
                        play.getAttackingTeamType().name().toLowerCase()
                );
                if (playSeeker.isSnitchCaught()) {
                    liChildren.add(buildScoreDiv(homeTeam, awayTeam, playSeeker, true));
                    i = 0;
                }
            }
            if (play.getInjuryType() != Play.InjuryType.NONE) {
                Div injuryDiv = new Div(new Text(play.outputInjuryWithDetails(resources)));
                injuryDiv.addClass("quidditch-injury");
                liChildren.add(injuryDiv);
            }
            if (i == 5) {
                Div time = new Div(new Text(resources.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength())));
                time.addClass("quidditch-time");
                liChildren.add(time);
                i = 0;
            }
            li.addChildren(liChildren);
        }

        matchPage.addBodyContent(playList);
        return matchPage;
    }

    private Table buildRosters(String teamName, Map<String, List<? extends Player>> rosterMap) {
        ArrayList<Table.TableCell> headerCells = new ArrayList<>();
        ArrayList<Table.TableCell> playerCells = new ArrayList<>();
        for (Map.Entry<String, List<? extends Player>> entry : rosterMap.entrySet()) {
            headerCells.add(new Table.HeaderCell(entry.getKey()));
            List<UnorderedList.Item> playerItems = entry.getValue().stream()
                    .map(player -> {
                        Text tooltip = new Text(player.playerSkillsOutput());
                        tooltip.addClass("player-tooltip-text");

                        Div playerDiv = new Div(new Text(player.getName()), tooltip);
                        playerDiv.addClass("player-tooltip");

                        return new UnorderedList.Item(playerDiv);
                    }).toList();
            playerCells.add(new Table.Cell(new UnorderedList(playerItems)));
        }

        Table.Row headerRow = new Table.Row(headerCells);
        Table.Row playerRow = new Table.Row(playerCells);
        headerRow.addClass("quidditch-roster-positions");
        playerRow.addClass("quidditch-roster-players");
        Table rosterTable = new Table(headerRow, playerRow);
        rosterTable.addClass("quidditch-roster");
        rosterTable.setCaption(teamName);

        return rosterTable;
    }

    private Table buildInjuredPlayersTable(String teamName, Map<String, LocalDate> injuredBefore, Map<String, LocalDate> injuredDuring) {
        Table injuredTable = new Table();
        injuredTable.setCaption(teamName);
        injuredTable.addClass("quidditch-roster");

        Table.HeaderCell headerCell;
        Table.Row row = new Table.Row();
        headerCell = new Table.HeaderCell(new Text("Before Match"));
        headerCell.addAttribute("colspan", "2");
        row.addChildren(headerCell);
        headerCell = new Table.HeaderCell(new Text("During Match"));
        headerCell.addAttribute("colspan", "2");
        row.addChildren(headerCell);
        injuredTable.addChildren(row);

        row = new Table.Row(
                new Table.HeaderCell(new Text("Player")),
                new Table.HeaderCell(new Text("Injured Through")),
                new Table.HeaderCell(new Text("Player")),
                new Table.HeaderCell(new Text("Injured Through"))
        );
        row.addClass("quiddtion-roster-header-row");
        injuredTable.addChildren(row);

        Iterator<Map.Entry<String, LocalDate>> beforeIt = injuredBefore.entrySet().iterator();
        Iterator<Map.Entry<String, LocalDate>> duringIt = injuredDuring.entrySet().iterator();
        while (beforeIt.hasNext() || duringIt.hasNext()) {
            row = new Table.Row();
            injuredRowCells(row, beforeIt);
            injuredRowCells(row, duringIt);
            injuredTable.addChildren(row);
        }

        return injuredTable;
    }

    private void injuredRowCells(Table.Row row, Iterator<Map.Entry<String, LocalDate>> it) {
        if (it.hasNext()) {
            Map.Entry<String, LocalDate> entry = it.next();
            row.addChildren(
                    new Table.Cell(new Text(entry.getKey())),
                    new Table.Cell(new Text(Formatters.dateFormatter.format(entry.getValue())))
            );
        } else {
            row.addChildren(new Table.Cell(), new Table.Cell());
        }
    }

    private Div buildScoreDiv(Team homeTeam, Team awayTeam, Play play, boolean finalScore) {
        Div div = new Div();

        String text = finalScore ? resources.getString("match.final") : resources.getString("match.score");
        div.addChildren(new Paragraph(text));
        UnorderedList ul = new UnorderedList();
        div.addChildren(ul);
        ul.addChildren(
                new UnorderedList.Item(homeTeam.getName() + ": " + play.getScoreHome()),
                new UnorderedList.Item(awayTeam.getName() + ": " + play.getScoreAway()),
                new UnorderedList.Item(resources.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength()))
        );

        return div;
    }

    public String getTournamentTitle() {
        return tournamentTitle;
    }
}
