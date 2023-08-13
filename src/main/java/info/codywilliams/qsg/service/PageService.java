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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PageService {
    @JsonIgnore
    final private ResourceBundleReplacer outputResourceBundleReplacer;
    @JsonIgnore
    final private ResourceBundle outputResourceBundle;
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
    private boolean createMatchPages = true;
    private boolean includePlayerDetails = true;

    static private final Pattern ballPattern = Pattern.compile("((quaffle|bludger|snitch)\\w*)", Pattern.CASE_INSENSITIVE);
    static private final String ballReplacement = "<span class=\"$2\">$1</span>";
    static private final String QUIDDITCH_STYLE = "QuidditchGenerator.css";

    public PageService(ResourceBundle outputResourceBundle) {
        this.outputResourceBundleReplacer = new ResourceBundleReplacer(outputResourceBundle);
        this.outputResourceBundle = outputResourceBundle;
    }

    public List<Page> buildPages(Tournament tournament, List<Team> teamList, boolean createMatchPages, boolean includePlayerDetails, long seed) {
        this.tournament = tournament;
        this.tournamentOptions = tournament.getTournamentOptions();
        this.teamList = teamList;
        this.createMatchPages = createMatchPages;
        this.includePlayerDetails = includePlayerDetails;
        this.seed = seed;
        MatchGenerator matchGenerator = MatchGenerator.create(this.seed, 1);
        matchGenerator.generateMatches(tournament, teamList);
        generatorVersionUsed = String.valueOf(matchGenerator.getVersion());

        long now = System.currentTimeMillis();
        yearRange = tournamentOptions.getStartDate().getYear()
                + "-" + tournament.getEndDate().getYear();
        outputResourceBundleReplacer.addToken("leagueName", tournamentOptions.getLeagueName());
        outputResourceBundleReplacer.addToken("yearRange", yearRange);

        tournamentTitle = outputResourceBundleReplacer.getString("tournamentTitle");

        List<Page> pages = new ArrayList<>();

        if (this.createMatchPages) {
            for (Match match : tournament.getMatches()) {
                pages.add(buildMatchPage(match));
            }
        }

        for (Team team: teamList) {
            pages.add(buildTeamPage(team));
        }

        pages.add(0, buildTournamentPage(tournamentTitle, seed));

        now = System.currentTimeMillis() - now;
        logger.info("{} seconds to generate pages", now / 1000.0);
        return pages;
    }

    private Page buildTeamPage(Team team) {
        Page teamPage = new Page(team.getName(), outputResourceBundleReplacer.getString("directory.teams"), Page.Type.TEAM);
        teamPage.addStyle(QUIDDITCH_STYLE);
        teamPage.addMetadata(
                "keywords",
                null,
                outputResourceBundleReplacer.getString("meta.tournament.keywords") + ", " + team.getName(),
                null
        );

        Map<String, List<? extends Player>> playerMap = new TreeMap<>(Map.of(
                outputResourceBundleReplacer.getString("team.beaters"), team.getBeaters(),
                outputResourceBundleReplacer.getString("team.chasers"), team.getChasers(),
                outputResourceBundleReplacer.getString("team.keepers"), team.getKeepers(),
                outputResourceBundleReplacer.getString("team.seekers"), team.getSeekers()
        ));

        Table teamRoster = buildRosterTable(outputResourceBundleReplacer.getString("team.roster"), playerMap);
        Table injuryTable = buildTeamInjuredPlayerTable(playerMap);

        Div seasonDiv = new Div(
                Link.TextLink.createIndexLink(
                        outputResourceBundleReplacer.getString("tournamentTitle"),
                        "../" + outputResourceBundleReplacer.getString("tournamentTitle"),
                        outputResourceBundleReplacer.getString("tournamentTitle")
                ),
                new Div(teamRoster, injuryTable)
        );
        seasonDiv.addClass("team-season");
        seasonDiv.setId("team-season-" + yearRange);

        teamPage.addBodyContent(
                new Header(2, outputResourceBundleReplacer.getString("team.seasons")),
                new Header(3, yearRange),
                seasonDiv
        );

        return teamPage;
    }

    private Page buildTournamentPage(String title, long seed) {
        Page seasonPage = new Page(title, outputResourceBundleReplacer.getString("directory.tournament"), Page.Type.TOURNAMENT);
        seasonPage.addStyle(QUIDDITCH_STYLE);
        seasonPage.addMetadata(
                "keywords",
                null,
                outputResourceBundleReplacer.getString("meta.tournament.keywords"),
                null
        );

        Image.Gallery teamGallery = new Image.Gallery("packed");
        teamList.stream()
                .map(Team::getName)
                .map(teamName -> new Image(teamName, teamName + ".png"))
                .forEach(teamGallery::addImages);

        seasonPage.addBodyContent(teamGallery);
        seasonPage.addBodyContent(new TableOfContents());


        LinkedList<Element> descriptionParagraphs = new LinkedList<>();
        for (String text : outputResourceBundleReplacer.getString("description." + tournament.getType().key).split("\n"))
            descriptionParagraphs.add(new Paragraph(text));
        seasonPage.addBodyContent(descriptionParagraphs);

        seasonPage.addBodyContent(new QsgNote());


        Header scheduleHeader = new Header(2, outputResourceBundleReplacer.getString("header.schedule"));
        seasonPage.addBodyContent(scheduleHeader);

        DefinitionList openingDayDef = new DefinitionList(
                new DefinitionList.Term(outputResourceBundleReplacer.getString("openingDay")),
                new DefinitionList.Def(tournamentOptions.getStartDate().format(
                        DateTimeFormatter.ofPattern("EEEE',' d LLLL yyyy").withZone(ZoneId.systemDefault())
                ))
        );
        openingDayDef.addClass("tournament-opening-day");

        DefinitionList.Def winner = new DefinitionList.Def(outputResourceBundleReplacer.getString("tournament.key.winner"));
        winner.addClass("tournament-match-winner");
        DefinitionList key = new DefinitionList(
                new DefinitionList.Term(outputResourceBundleReplacer.getString("tournament.key.header")),
                winner
        );
        key.addClass("tournament-key");

        Div leagueScheduleTopper = new Div(openingDayDef, key);
        leagueScheduleTopper.addClass("tournament-schedule-info");
        seasonPage.addBodyContent(leagueScheduleTopper);

        Table matchTable = buildMatchTable(tournament.getMatches());

        seasonPage.addBodyContent(matchTable);

        Header rankingsHeader = new Header(2, outputResourceBundleReplacer.getString("header.rankings"));
        Paragraph rankingsDesc = new Paragraph(outputResourceBundleReplacer.getString("rankings"));

        ArrayList<Map.Entry<String, Integer>> rankings = new ArrayList<>(tournament.getTournamentPoints().entrySet());
        rankings.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        Table rankingTable = new Table();
        rankingTable.addClass("tournament-rankings", "wikitable");

        rankingTable.addChildren(
                new Table.Row(
                        new Table.HeaderCell(outputResourceBundleReplacer.getString("header.rank.team")),
                        new Table.HeaderCell(outputResourceBundleReplacer.getString("header.rank.points"))
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
                new DefinitionList.Term(outputResourceBundleReplacer.getString("generator.version")),
                new DefinitionList.Def(generatorVersionUsed)
        );
        version.addClass("tournament-version");


        DefinitionList seedDl = new DefinitionList(
                new DefinitionList.Term(outputResourceBundleReplacer.getString("generator.seed")),
                new DefinitionList.Def(HexFormat.of().withUpperCase().toHexDigits(seed))
        );
        seedDl.addClass("tournament-seed");

        Div footer = new Div(version, seedDl);
        footer.addClass("tournament-footer");
        seasonPage.addBodyContent(footer);

        return seasonPage;
    }

    private Table buildMatchTable(Set<Match> matches) {
        Table matchTable = new Table();
        matchTable.addClass("tournament-schedule", "wikitable");

        int round = 0;
        for (Match match : matches) {
            if (round != match.getRound()) {
                round = match.getRound();
                matchTable.addChildren(matchTableRoundHeader(round), matchTableHeaders());
            }
            matchTable.addChildren(matchTableRow(match));
        }

        return matchTable;
    }

    private Table.Row matchTableRoundHeader(int roundNum) {
        Table.HeaderCell roundHeaderCell = new Table.HeaderCell(outputResourceBundleReplacer.getString("header.round") + roundNum);
        roundHeaderCell.addAttribute("colspan", "7");
        return new Table.Row(roundHeaderCell);
    }

    private Table.Row matchTableHeaders() {
                Table.HeaderCell[] columnHeaderCells = new Table.HeaderCell[]{
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.date")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.home")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.away")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.location")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.length")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.score")),
                new Table.HeaderCell(outputResourceBundleReplacer.getString("header.points"))
        };

        return new Table.Row(columnHeaderCells);
    }

    private Table.Row matchTableRow(Match match) {
        Table.Cell date;
        String matchDirectory  = "";
        String teamDirectory = "../" + outputResourceBundleReplacer.getString("directory.teams");

        if (createMatchPages) {
            date = new Table.Cell(
                    new Link.TextLink(
                            match.getStartDateTime().format(Formatters.dateTimeFormatter),
                            matchDirectory,
                            match.getTitle()
                    )
            );
        }
        else {
            date = new Table.Cell(match.getStartDateTime().format(Formatters.dateTimeFormatter));
        }


        Table.Cell home = new Table.Cell(
                new Link.TextLink(
                        match.getHomeTeam().getName(),
                        teamDirectory,
                        match.getHomeTeam().getName()
                )
        );
        Table.Cell away = new Table.Cell(
                new Link.TextLink(
                        match.getAwayTeam().getName(),
                        teamDirectory,
                        match.getAwayTeam().getName()
                )
        );
        Table.Cell location = new Table.Cell(match.getLocation());
        Table.Cell length = new Table.Cell(Formatters.formatDuration(match.getMatchLength()));
        Table.Cell score = new Table.Cell(match.getScoreHome() + " - " + match.getScoreAway());
        Table.Cell points = new Table.Cell(String.valueOf(tournament.getPoints(match)));

        date.addClass("tournament-match-date");
        home.addClass("tournament-match-home");
        away.addClass("tournament-match-away");
        location.addClass("tournament-match-location");
        length.addClass("tournament-match-length");
        score.addClass("tournament-match-score");
        points.addClass("tournament-match-points");

        switch (match.getSnitchCaughtBy()) {
            case HOME -> home.addClass("tournament-match-caught-snitch");
            case AWAY -> away.addClass("tournament-match-caught-snitch");
        }
        if (match.getWinner() != null) {
            switch (match.getWinner()) {
                case HOME -> {
                    home.addClass("tournament-match-winner");
                    away.addClass("tournament-match-loser");
                }
                case AWAY -> {
                    home.addClass("tournament-match-loser");
                    away.addClass("tournament-match-winner");
                }
            }
        }
        else {
            home.addClass("tournament-match-tie");
            away.addClass("tournament-match-tie");
        }
        return new Table.Row(date, home, away, location, length, score, points);
    }

    private void setResourceMatchTokens(Team homeTeam, Team awayTeam, LocalDateTime startDateTime) {
        this.outputResourceBundleReplacer.addToken("date", startDateTime.toLocalDate().format(Formatters.dateFormatter));
        this.outputResourceBundleReplacer.addToken("homeTeam", homeTeam.getName());
        if (homeTeam.getShortName().isEmpty())
            this.outputResourceBundleReplacer.addToken("homeTeamShort", homeTeam.getName());
        else
            this.outputResourceBundleReplacer.addToken("homeTeamShort", homeTeam.getShortName());
        this.outputResourceBundleReplacer.addToken("awayTeam", awayTeam.getName());
        if (awayTeam.getShortName().isEmpty())
            this.outputResourceBundleReplacer.addToken("awayTeamShort", awayTeam.getName());
        else
            this.outputResourceBundleReplacer.addToken("awayTeamShort", awayTeam.getShortName());
    }

    private Page buildMatchPage(Match match) {
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();

        setResourceMatchTokens(homeTeam, awayTeam, match.getStartDateTime());
        String title = outputResourceBundleReplacer.getString("match.title");
        match.setTitle(title);

        Page matchPage = new Page(title, outputResourceBundleReplacer.getString("directory.tournament"), Page.Type.MATCH);
        matchPage.addStyle(QUIDDITCH_STYLE);
        matchPage.addMetadata("keywords", null, outputResourceBundleReplacer.getString("meta.match.keywords"), null);
        matchPage.addBodyContent(new MatchInfobox(match, tournamentTitle, outputResourceBundleReplacer.getString("leagueName"), outputResourceBundleReplacer.getString("yearRange"), outputResourceBundle));

        if (includePlayerDetails) {
            Link.ImageLink homeImage = new Link.ImageLink(
                    homeTeam.getName() + " Logo",
                    homeTeam.getName() + ".png",
                    "../" + outputResourceBundle.getString("directory.teams"),
                    homeTeam.getName()
            );
            homeImage.addImageAttribute("height", "200px");
            homeImage.addImageAttribute("width", "200px");
            Link.ImageLink awayImage = new Link.ImageLink(
                    awayTeam.getName() + " Logo",
                    awayTeam.getName() + ".png",
                    "../" + outputResourceBundle.getString("directory.teams"),
                    awayTeam.getName()
            );
            awayImage.addAttribute("height", "200px");
            awayImage.addAttribute("width", "200px");
            Div rostersDiv = new Div(
                    new Div(
                            homeImage,
                            buildRosterTable(homeTeam.getName(), match.getHomeTeamRoster()),
                            buildInjuredPlayersTable(homeTeam.getName(), match.getHomeInjuredBefore(), match.getHomeInjuredDuring())
                    ),
                    new Div(
                            awayImage,
                            buildRosterTable(awayTeam.getName(), match.getAwayTeamRoster()),
                            buildInjuredPlayersTable(awayTeam.getName(), match.getAwayInjuredBefore(), match.getAwayInjuredDuring())
                    )
            );
            rostersDiv.setId("match-rosters");
            matchPage.addBodyContent(new Header(2, outputResourceBundleReplacer.getString("match.rosters.header")), rostersDiv);
        }

        matchPage.addBodyContent(new Header(2, "Match"));
        UnorderedList playList = new UnorderedList();
        playList.addClass("match-play");

        int i = 0;
        for (Play play : match.getPlays()) {
            i++;
            String playResourceKey = buildPlayResourceKey(play);
            Map<String, String> playTokenMap = buildPlayResourceTempTokenMap(play, match);

            UnorderedList.Item li = new UnorderedList.Item();
            playList.addChildren(li);
            List<Element> liChildren = new ArrayList<>();

            String playText = outputResourceBundleReplacer.getStringWithTempTokens(playResourceKey, playTokenMap);
            Matcher matcher = ballPattern.matcher(playText);
            playText = matcher.replaceAll(ballReplacement);

            liChildren.add(new Text(playText));


            li.addClass(buildClassList(play));
            Div scoreDiv = buildScoreDiv(homeTeam, awayTeam, play);
            if (scoreDiv != null) {
                liChildren.add(scoreDiv);
                i = 0;
            }

            if (play.getInjuryType() != Injury.NONE) {
                String injuryResourceKey = buildInjuryResourceKey(play);
                String injuryText = outputResourceBundleReplacer.getStringWithTempTokens(injuryResourceKey, playTokenMap);
                Div injuryDiv = new Div(new Text(injuryText));
                injuryDiv.addClass("match-injury");
                liChildren.add(injuryDiv);
            }
            if (i == 5) {
                Div time = new Div(new Text(outputResourceBundleReplacer.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength())));
                time.addClass("match-time");
                liChildren.add(time);
                i = 0;
            }
            li.addChildren(liChildren);
        }

        matchPage.addBodyContent(playList);
        return matchPage;
    }

    private Table buildRosterTable(String caption, Map<String, List<? extends Player>> rosterMap) {
        Table rosterTable = new Table(
                new Table.Row(
                        new Table.HeaderCell(),
                        new Table.HeaderCell(outputResourceBundle.getString("team.roster.offense")),
                        new Table.HeaderCell(outputResourceBundle.getString("team.roster.defense")),
                        new Table.HeaderCell(outputResourceBundle.getString("team.roster.teamwork")),
                        new Table.HeaderCell(outputResourceBundle.getString("team.roster.foul"))
                )
        );

        rosterTable.setCaption(caption);
        rosterTable.addClass("quidditch-roster", "wikitable");
        for (Map.Entry<String, List<? extends Player>> entry: rosterMap.entrySet()) {
            Table.HeaderCell positionHeader = new Table.HeaderCell(entry.getKey());
            positionHeader.addAttribute("colspan", "5");

            rosterTable.addChildren(new Table.Row(positionHeader));
            for  (Player player: entry.getValue()) {
                Table.Cell playerCell = new Table.Cell(player.getName());
                playerCell.addClass("quidditch-name");
                rosterTable.addChildren(
                        new Table.Row(
                                playerCell,
                                new Table.Cell(String.valueOf(player.getSkillOffense())),
                                new Table.Cell(String.valueOf(player.getSkillDefense())),
                                new Table.Cell(String.valueOf(player.getSkillTeamwork())),
                                new Table.Cell(String.valueOf(player.getFoulLikelihood()))
                        )
                );
            }
        }
        return rosterTable;
    }

    private Table buildInjuredPlayersTable(String teamName, Map<String, LocalDate> injuredBefore, Map<String, LocalDate> injuredDuring) {
        if (injuredBefore.isEmpty() && injuredDuring.isEmpty())
            return null;

        Table injuredTable = new Table();
        injuredTable.setCaption(teamName + " " + outputResourceBundleReplacer.getString("match.injured.header"));
        injuredTable.addClass("match-injuries", "wikitable");

        injuredTable.addChildren(
                new Table.Row(
                        new Table.HeaderCell(new Text(outputResourceBundleReplacer.getString("match.injured.player"))),
                        new Table.HeaderCell(new Text(outputResourceBundleReplacer.getString("match.injured.until")))
                )
        );

        if (!injuredBefore.isEmpty()) {
            Table.HeaderCell headerCell = new Table.HeaderCell(new Text(outputResourceBundleReplacer.getString("match.injured.before")));
            headerCell.addAttribute("colspan", "2");
            injuredTable.addChildren(
                    new Table.Row(headerCell)
            );
            for (Map.Entry<String, LocalDate> entry : injuredBefore.entrySet()) {
                Table.Cell playerCell = new Table.Cell(new Text(entry.getKey()));
                playerCell.addClass("quidditch-name");
                injuredTable.addChildren(
                        new Table.Row(
                                playerCell,
                                new Table.Cell(new Text(Formatters.yearlessDateFormatter.format(entry.getValue())))
                        )
                );
            }
        }

        if (!injuredDuring.isEmpty()) {
            Table.HeaderCell headerCell = new Table.HeaderCell(new Text(outputResourceBundleReplacer.getString("match.injured.during")));
            headerCell.addAttribute("colspan", "2");
            injuredTable.addChildren(
                    new Table.Row(headerCell)
            );

            for (Map.Entry<String, LocalDate> entry : injuredDuring.entrySet()) {
                Table.Cell playerCell = new Table.Cell(new Text(entry.getKey()));
                playerCell.addClass("quidditch-name");
                injuredTable.addChildren(
                        new Table.Row(
                                playerCell,
                                new Table.Cell(new Text(Formatters.yearlessDateFormatter.format(entry.getValue())))
                        )
                );
            }
        }

        return injuredTable;
    }

    private Table buildTeamInjuredPlayerTable(Map<String, List<? extends Player>> team) {
        Table injuryTable = new Table();
        injuryTable.setCaption(outputResourceBundle.getString("team.injured.caption"));
        injuryTable.addClass("team-injuries", "wikitable");
        for (Map.Entry<String, List<? extends Player>> entry : team.entrySet()) {
            for (Player player : entry.getValue()) {
                if (player.getInjuryHistory().isEmpty())
                    continue;
                Table.HeaderCell playerCell = new Table.HeaderCell(player.getName());
                playerCell.addClass("quidditch-name");
                Table.Row playerRow = new Table.Row(
                        playerCell,
                        new Table.Cell(player.getInjuryDateRanges().stream()
                                .map(injuryRangeStringMapper)
                                .collect(Collectors.joining(", ")))
                );
                injuryTable.addChildren(playerRow);
            }
        }

        return injuryTable;
    }

    private Function<Player.InjuryRange, String> injuryRangeStringMapper = injuryRange -> {
        if (injuryRange.start().isEqual(injuryRange.end()))
            return Formatters.yearlessDateFormatter.format(injuryRange.start());
        else
            return Formatters.yearlessDateFormatter.format(injuryRange.start()) + "–" + Formatters.yearlessDateFormatter.format(injuryRange.end());
    };

    private Div buildScoreDiv(Team homeTeam, Team awayTeam, Play play) {
        boolean finalScore;
        if (play instanceof PlayFoul playFoul && playFoul.getQuaffleOutcome() == Quaffle.SCORED) {
            finalScore = false;
        } else if (play instanceof PlayChaser playChaser && playChaser.getQuaffleOutcome() == Quaffle.SCORED) {
            finalScore = false;
        } else if (play instanceof PlaySeeker playSeeker && playSeeker.isSnitchCaught()) {
            finalScore = true;
        } else {
            return null;
        }
        Div div = new Div();

        String text = finalScore ? outputResourceBundleReplacer.getString("match.final") : outputResourceBundleReplacer.getString("match.score");
        div.addChildren(new Paragraph(text));
        UnorderedList ul = new UnorderedList();
        div.addChildren(ul);
        ul.addChildren(
                new UnorderedList.Item(homeTeam.getName() + ": " + play.getScoreHome()),
                new UnorderedList.Item(awayTeam.getName() + ": " + play.getScoreAway()),
                new UnorderedList.Item(outputResourceBundleReplacer.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength()))
        );

        return div;
    }

    private String buildPlayResourceKey(Play play) {
        String resourceKey = "";
        if (play instanceof PlayFoul playFoul) {
            resourceKey = "foul." + playFoul.getQuaffleOutcome().name().toLowerCase();
        } else if (play instanceof PlayChaser playChaser) {
            resourceKey = "chaser." + playChaser.getQuaffleOutcome().name().toLowerCase() + "." + play.getBludgerOutcome().name().toLowerCase();
        } else if (play instanceof PlaySeeker playSeeker) {
            resourceKey = "seeker." + playSeeker.getSnitchOutcome().name().toLowerCase() + "." + play.getBludgerOutcome().name().toLowerCase();
        } else {
            logger.error("Unknown play type, {}", play);
        }

        if (includePlayerDetails) {
            resourceKey += ".player";
        }
        return resourceKey;
    }

    private String buildInjuryResourceKey(Play play) {
        if (play.getInjuryType() == Injury.NONE)
            return "";

        String resourceKey = "injury." + play.getInjuryType().name().toLowerCase();
        if (play.getInjuryType() == Injury.KEEPER && play instanceof PlayChaser playChaser) {
            if (playChaser.getQuaffleOutcome() == Quaffle.MISSED || playChaser.getQuaffleOutcome() == Quaffle.SCORED)
                resourceKey += ".missed";
            if (playChaser.getQuaffleOutcome() == Quaffle.BLOCKED)
                resourceKey += ".blocked";
        }

        if (includePlayerDetails) {
            resourceKey += ".player";
        }

        return resourceKey;
    }


    private Map<String, String> buildPlayResourceTempTokenMap(Play play, Match match) {
        Map<String, String> tokenMap = new HashMap<>();
        if (play.getBeaterHitter() != null)
            tokenMap.put("beaterHitter", play.getBeaterHitter().getShortName());
        if (play.getBeaterBlocker() != null)
            tokenMap.put("beaterBlocker", play.getBeaterBlocker().getShortName());

        switch (play.getAttackingTeamType()) {
            case HOME -> {
                tokenMap.put("attackingTeam", match.getHomeTeam().getName());
                tokenMap.put("attackingTeamShort", match.getHomeTeam().getShortName());
                tokenMap.put("defendingTeam", match.getAwayTeam().getName());
                tokenMap.put("defendingTeamShort", match.getAwayTeam().getShortName());
            }
            case AWAY -> {
                tokenMap.put("attackingTeam", match.getAwayTeam().getName());
                tokenMap.put("attackingTeamShort", match.getAwayTeam().getShortName());
                tokenMap.put("defendingTeam", match.getHomeTeam().getName());
                tokenMap.put("defendingTeamShort", match.getHomeTeam().getShortName());
            }
        }

        if (play.getInjuryType() != Injury.NONE) {
            tokenMap.put("injuredPlayer", play.getInjuredPlayer().getShortName());
            switch (play.getInjuredPlayerTeam()) {
                case HOME -> {
                    tokenMap.put("injuredPlayerTeam", match.getHomeTeam().getName());
                    tokenMap.put("injuredPlayerTeamShort", match.getHomeTeam().getName());
                }
                case AWAY -> {
                    tokenMap.put("injuredPlayerTeam", match.getAwayTeam().getName());
                    tokenMap.put("injuredPlayerTeamShort", match.getAwayTeam().getShortName());
                }
            }
        }

        if (play instanceof PlayChaser playChaser) {
            tokenMap.put("attacker", playChaser.getAttacker().getShortName());
            tokenMap.put("defender", playChaser.getDefender().getShortName());

            if (playChaser.getDefendingKeeper() != null)
                tokenMap.put("keeper", playChaser.getDefendingKeeper().getShortName());

            if (play instanceof PlayFoul playFoul) {
                tokenMap.put("fouler", playFoul.getFouler().getShortName());

                switch (playFoul.getFoulerTeamType()) {
                    case HOME -> {
                        tokenMap.put("foulerTeam", match.getHomeTeam().getName());
                        tokenMap.put("foulerTeamShort", match.getHomeTeam().getShortName());
                    }
                    case AWAY -> {
                        tokenMap.put("foulerTeam", match.getAwayTeam().getName());
                        tokenMap.put("foulerTeamShort", match.getAwayTeam().getShortName());
                    }
                }
            }
        } else if (play instanceof PlaySeeker playSeeker) {
            tokenMap.put("seeker", playSeeker.getSeeker().getShortName());
            tokenMap.put("otherSeeker", playSeeker.getOtherSeeker().getShortName());
        }

        return tokenMap;
    }

    private List<String> buildClassList(Play play) {

        if (play instanceof PlayFoul playFoul) {
            return List.of(
                    "foul",
                    "quaffle-" + playFoul.getQuaffleOutcome().name().toLowerCase(),
                    play.getAttackingTeamType().name().toLowerCase()
            );
        } else if (play instanceof PlayChaser playChaser) {
            return List.of(
                    "quaffle-" + playChaser.getQuaffleOutcome().name().toLowerCase(),
                    play.getAttackingTeamType().name().toLowerCase()
            );
        } else if (play instanceof PlaySeeker playSeeker) {
            return List.of(
                    "snitch-" + playSeeker.getSnitchOutcome().name().toLowerCase(),
                    play.getAttackingTeamType().name().toLowerCase()
            );
        }
        return List.of();
    }

    public String getTournamentTitle() {
        return tournamentTitle;
    }

    public String getYearRange() {
        return yearRange;
    }
}
