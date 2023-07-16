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
import info.codywilliams.qsg.models.match.Play.BludgerOutcome;
import info.codywilliams.qsg.models.match.Play.InjuryType;
import info.codywilliams.qsg.models.match.PlayChaser.QuaffleOutcome;
import info.codywilliams.qsg.models.match.PlaySeeker.SnitchOutcome;
import info.codywilliams.qsg.models.player.Player;
import info.codywilliams.qsg.output.Element;
import info.codywilliams.qsg.output.MatchInfobox;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.output.elements.*;
import info.codywilliams.qsg.util.Formatters;
import info.codywilliams.qsg.util.ResourceBundleReplacer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Match implements Comparable<Match> {
    private final int number;
    private final int round;
    private final LocalDateTime startDateTime;
    private Team homeTeam;
    private Team awayTeam;
    private Map<String, List<? extends Player>> homeTeamRoster;
    private Map<String, List<? extends Player>> awayTeamRoster;
    private String location;
    private Duration matchLength;
    private Duration snitchReleaseTime;
    private int scoreHome;
    private int scoreAway;
    private int foulsHome;
    private int foulsAway;
    private LinkedList<Play> plays;
    private String title;
    private ResourceBundleReplacer resources;
    private TeamType winner;
    private final Map<String, LocalDate> homeInjuredBefore;
    private final Map<String, LocalDate> homeInjuredDuring;
    private final Map<String, LocalDate> awayInjuredBefore;
    private final Map<String, LocalDate> awayInjuredDuring;

    private final EnumMap<QuaffleOutcome, Integer> homeQuaffleOutcomes;
    private final EnumMap<QuaffleOutcome, Integer> awayQuaffleOutcomes;
    private final EnumMap<BludgerOutcome, Integer> homeBludgerOutcomes;
    private final EnumMap<BludgerOutcome, Integer> awayBludgerOutcomes;
    private final EnumMap<SnitchOutcome, Integer> homeSnitchOutcomes;
    private final EnumMap<SnitchOutcome, Integer> awaySnitchOutcomes;
    private final EnumMap<InjuryType, Integer> homeInjuryTypes;
    private final EnumMap<InjuryType, Integer> awayInjuryTypes;

    public Match(int number, int round, LocalDateTime startDateTime) {
        this.number = number;
        this.round = round;
        this.startDateTime = startDateTime;
        homeInjuredBefore = new TreeMap<>();
        homeInjuredDuring = new TreeMap<>();
        awayInjuredBefore = new TreeMap<>();
        awayInjuredDuring = new TreeMap<>();

        homeQuaffleOutcomes = new EnumMap<>(QuaffleOutcome.class);
        awayQuaffleOutcomes = new EnumMap<>(QuaffleOutcome.class);
        homeBludgerOutcomes = new EnumMap<>(BludgerOutcome.class);
        awayBludgerOutcomes = new EnumMap<>(BludgerOutcome.class);
        homeSnitchOutcomes = new EnumMap<>(SnitchOutcome.class);
        awaySnitchOutcomes = new EnumMap<>(SnitchOutcome.class);
        homeInjuryTypes = new EnumMap<>(InjuryType.class);
        awayInjuryTypes = new EnumMap<>(InjuryType.class);

        clear();
    }

    public void clear() {
        matchLength = Duration.ZERO;
        plays = new LinkedList<>();
        scoreHome = 0;
        scoreAway = 0;
        foulsHome = 0;
        foulsAway = 0;
        winner = null;

        for (QuaffleOutcome outcome : QuaffleOutcome.values()) {
            homeQuaffleOutcomes.put(outcome, 0);
            awayQuaffleOutcomes.put(outcome, 0);
        }
        for (SnitchOutcome outcome : SnitchOutcome.values()) {
            homeSnitchOutcomes.put(outcome, 0);
            awaySnitchOutcomes.put(outcome, 0);
        }
        for (BludgerOutcome outcome : BludgerOutcome.values()) {
            homeBludgerOutcomes.put(outcome, 0);
            awayBludgerOutcomes.put(outcome, 0);
        }
        for (InjuryType type : InjuryType.values()) {
            homeInjuryTypes.put(type, 0);
            awayInjuryTypes.put(type, 0);
        }

    }

    public void setResources(ResourceBundleReplacer resources) {
        this.resources = new ResourceBundleReplacer(resources);
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

    public ResourceBundleReplacer getResources() {
        return resources;
    }

    public Page buildMatchPage() {
        Page matchPage = new Page(getTitle(), getTitle());
        matchPage.addStyle("QuidditchGenerator.css");
        matchPage.addMetadata("keywords", null, resources.getString("meta.match.keywords"), null);
        matchPage.addBodyContent(new MatchInfobox(this));

        matchPage.addBodyContent(new Header(2, "Match Rosters"));
        matchPage.addBodyContent(buildRosters(homeTeam.getName(), getHomeTeamRoster()));
        matchPage.addBodyContent(buildRosters(awayTeam.getName(), getAwayTeamRoster()));

        matchPage.addBodyContent(new Header(2, "Injured Players"));
        matchPage.addBodyContent(buildInjuredPlayersTable(homeTeam.getName(), homeInjuredBefore, homeInjuredDuring));
        matchPage.addBodyContent(buildInjuredPlayersTable(awayTeam.getName(), awayInjuredBefore, awayInjuredDuring));

        matchPage.addBodyContent(new Header(2, "Match"));
        UnorderedList playList = new UnorderedList();
        playList.addClass("quidditch-match");

        int i = 0;
        for (Play play : plays) {
            i++;
            UnorderedList.Item li = new UnorderedList.Item();
            playList.addChildren(li);
            List<Element> liChildren = new ArrayList<>();
            liChildren.add(new Text(play.outputWithDetails(resources, getHomeTeam().getName(), getAwayTeam().getName())));

            if (play instanceof PlayFoul playFoul) {
                li.addClass(
                        "quidditch-foul",
                        "quaffle-" + playFoul.getQuaffleOutcome().name().toLowerCase(),
                        play.attackingTeamType.name().toLowerCase()
                );
                if (playFoul.getQuaffleOutcome() == QuaffleOutcome.SCORED) {
                    liChildren.add(buildScoreDiv(playFoul, false));
                    i = 0;
                }
            } else if (play instanceof PlayChaser playChaser) {
                li.addClass(
                        "quaffle-" + playChaser.getQuaffleOutcome().name().toLowerCase(),
                        play.attackingTeamType.name().toLowerCase()
                );
                if (playChaser.getQuaffleOutcome() == QuaffleOutcome.SCORED) {
                    liChildren.add(buildScoreDiv(playChaser, false));
                    i = 0;
                }
            } else if (play instanceof PlaySeeker playSeeker) {
                li.addClass(
                        "snitch-" + playSeeker.getSnitchOutcome().name().toLowerCase(),
                        play.attackingTeamType.name().toLowerCase()
                );
                if (playSeeker.isSnitchCaught()) {
                    liChildren.add(buildScoreDiv(playSeeker, true));
                    i = 0;
                }
            }
            if (play.getInjuryType() != InjuryType.NONE) {
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
        while(beforeIt.hasNext() || duringIt.hasNext()) {
            row = new Table.Row();
            injuredRowCells(row, beforeIt);
            injuredRowCells(row, duringIt);
            injuredTable.addChildren(row);
        }

        return injuredTable;
    }

    private void injuredRowCells(Table.Row row, Iterator<Map.Entry<String, LocalDate>> it) {
        if(it.hasNext()) {
            Map.Entry<String, LocalDate> entry = it.next();
            row.addChildren(
                    new Table.Cell(new Text(entry.getKey())),
                    new Table.Cell(new Text(Formatters.dateFormatter.format(entry.getValue())))
            );
        } else {
            row.addChildren(new Table.Cell(), new Table.Cell());
        }
    }

    private Div buildScoreDiv(Play play, boolean finalScore) {
        Div div = new Div();

        String text = finalScore ? resources.getString("match.final") : resources.getString("match.score");
        div.addChildren(new Paragraph(text));
        UnorderedList ul = new UnorderedList();
        div.addChildren(ul);
        ul.addChildren(
                new UnorderedList.Item(getHomeTeam().getName() + ": " + play.getScoreHome()),
                new UnorderedList.Item(getAwayTeam().getName() + ": " + play.getScoreAway()),
                new UnorderedList.Item(resources.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength()))
        );

        return div;
    }

    public String getTitle() {
        if (title != null)
            return title;
        title = resources.getString("match.title");

        return title;
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

    public Duration getSnitchReleaseTime() {
        return snitchReleaseTime;
    }

    public void setSnitchReleaseTime(Duration snitchReleaseTime) {
        this.snitchReleaseTime = snitchReleaseTime;
    }

    public LinkedList<Play> getPlays() {
        return plays;
    }

    public void addPlay(Play play) {
        addTime(play.getPlayDurationSeconds());
        play.setScores(getScoreHome(), getScoreAway());
        play.setMatchLength(getMatchLength());
        this.plays.add(play);

        Map<BludgerOutcome, Integer> bludgerOutcome =
                play.getAttackingTeamType() == TeamType.HOME
                        ? homeBludgerOutcomes
                        : awayBludgerOutcomes;
        bludgerOutcome.merge(play.getBludgerOutcome(), 1, Integer::sum);

        if (play.getInjuryType() != InjuryType.NONE) {
            Map<InjuryType, Integer> injuryTypeMap;
            Map<String, LocalDate> injuredDuringMap;

            if (play.getInjuredPlayerTeam() == TeamType.HOME) {
                injuryTypeMap = homeInjuryTypes;
                injuredDuringMap = homeInjuredDuring;
            } else {
                injuryTypeMap = awayInjuryTypes;
                injuredDuringMap = awayInjuredDuring;
            }

            injuryTypeMap.merge(
                    play.getInjuryType(),
                    1,
                    Integer::sum
            );

            injuredDuringMap.merge(
                    play.getInjuredPlayer().getName(),
                    play.getInjuryEndDate(),
                    (prev, next) -> prev.isAfter(next) ? prev : next
            );
        }

        if (play instanceof PlayChaser playChaser) {
            Map<QuaffleOutcome, Integer> quaffleOutcome =
                    play.getAttackingTeamType() == TeamType.HOME
                            ? homeQuaffleOutcomes
                            : awayQuaffleOutcomes;
            quaffleOutcome.merge(playChaser.getQuaffleOutcome(), 1, Integer::sum);
        } else if (play instanceof PlaySeeker playSeeker) {
            Map<SnitchOutcome, Integer> snitchOutcome =
                    play.getAttackingTeamType() == TeamType.HOME
                            ? homeSnitchOutcomes
                            : awaySnitchOutcomes;
            snitchOutcome.merge(playSeeker.getSnitchOutcome(), 1, Integer::sum);
        }
    }

    public void addInjuredBeforePlayer(TeamType teamType, Player player) {
        Map<String, LocalDate> injuredMap = teamType == TeamType.HOME ? homeInjuredBefore : awayInjuredBefore;
        LocalDate endDate = player.findInjuryEndDate(getStartDateTime().toLocalDate());
        injuredMap.put(player.getName(), endDate);
    }

    public int getScoreHome() {
        return scoreHome;
    }

    public int getScoreAway() {
        return scoreAway;
    }

    public int getFoulsHome() {
        return foulsHome;
    }

    public int getFoulsAway() {
        return foulsAway;
    }

    public void incrementFoulsHome() {
        this.foulsHome++;
    }

    public void incrementFoulsAway() {
        this.foulsAway++;
    }

    public int homeScore() {
        scoreHome += 10;
        return scoreHome;
    }

    public int awayScore() {
        scoreAway += 10;
        return scoreAway;
    }

    public int homeCaughtSnitch() {
        scoreHome += 150;
        return scoreHome;
    }

    public int awayCaughtSnitch() {
        scoreAway += 150;
        return scoreAway;
    }

    private void addTime(int seconds) {
        matchLength = matchLength.plusSeconds(seconds);
    }

    public TeamType getWinner() {
        return winner;
    }

    public void setWinner(TeamType winner) {
        this.winner = winner;
    }

    public Map<String, List<? extends Player>> getHomeTeamRoster() {
        return homeTeamRoster;
    }

    public void setHomeTeamRoster(Map<String, List<? extends Player>> homeTeamRoster) {
        this.homeTeamRoster = homeTeamRoster;
    }

    public Map<String, List<? extends Player>> getAwayTeamRoster() {
        return awayTeamRoster;
    }

    public void setAwayTeamRoster(Map<String, List<? extends Player>> awayTeamRoster) {
        this.awayTeamRoster = awayTeamRoster;
    }

    public String outcomesToString() {
        return "Outcomes:\n" +
                "\tHome: " +
                "\n\t\tQuaffle: " + homeQuaffleOutcomes.toString() +
                "\n\t\tBludger: " + homeBludgerOutcomes.toString() +
                "\n\t\tSnitch: " + homeSnitchOutcomes.toString() +
                "\n\t\tInjuries: " + homeInjuryTypes.toString() +
                "\n\tAway:\n\t\tQuaffle: " + awayQuaffleOutcomes.toString() +
                "\n\t\tBludger: " + awayBludgerOutcomes.toString() +
                "\n\t\tSnitch: " + awaySnitchOutcomes.toString() +
                "\n\t\tInjuries: " + awayInjuryTypes.toString();
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
    public int compareTo(@NotNull Match other) {
        if (this == other)
            return 0;
        int dateComp = startDateTime.compareTo(other.startDateTime);
        int roundComp = round - other.round;

        if (roundComp == 0) {
            if (dateComp == 0) {
                return number - other.number;
            }
            return dateComp;
        }
        return roundComp;
    }
}
