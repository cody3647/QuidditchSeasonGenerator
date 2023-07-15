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
import info.codywilliams.qsg.models.match.PlayChaser.QuaffleOutcome;
import info.codywilliams.qsg.models.match.PlaySeeker.SnitchOutcome;
import info.codywilliams.qsg.output.Element;
import info.codywilliams.qsg.output.MatchInfobox;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.output.elements.Div;
import info.codywilliams.qsg.output.elements.Paragraph;
import info.codywilliams.qsg.output.elements.Text;
import info.codywilliams.qsg.output.elements.UnorderedList;
import info.codywilliams.qsg.util.Formatters;
import info.codywilliams.qsg.util.ResourceBundleReplacer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Match implements Comparable<Match> {
    private final int number;
    private final int round;
    private final LocalDateTime startDateTime;
    private Team homeTeam;
    private Team awayTeam;
    private String location;
    private Duration matchLength;
    private int scoreHome;
    private int scoreAway;
    private int foulsHome;
    private int foulsAway;
    private LinkedList<Play> plays;
    private String title;
    private ResourceBundleReplacer resources;
    private TeamType winner;
    private int homePlays;
    private int awayPlays;

    private final EnumMap<QuaffleOutcome, Integer> homeQuaffleOutcomes;
    private final EnumMap<QuaffleOutcome, Integer> awayQuaffleOutcomes;
    private final EnumMap<BludgerOutcome, Integer> homeBludgerOutcomes;
    private final EnumMap<BludgerOutcome, Integer> awayBludgerOutcomes;
    private final EnumMap<SnitchOutcome, Integer> homeSnitchOutcomes;
    private final EnumMap<SnitchOutcome, Integer> awaySnitchOutcomes;

    public Match(int number, int round, LocalDateTime startDateTime) {
        this.number = number;
        this.round = round;
        this.startDateTime = startDateTime;
        homeQuaffleOutcomes = new EnumMap<>(QuaffleOutcome.class);
        awayQuaffleOutcomes = new EnumMap<>(QuaffleOutcome.class);
        homeBludgerOutcomes = new EnumMap<>(BludgerOutcome.class);
        awayBludgerOutcomes = new EnumMap<>(BludgerOutcome.class);
        homeSnitchOutcomes = new EnumMap<>(SnitchOutcome.class);
        awaySnitchOutcomes = new EnumMap<>(SnitchOutcome.class);
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
            if (i == 5) {
                liChildren.add(new Div(new Text(resources.getString("match.time") + ": " + Formatters.formatDuration(play.getMatchLength()))));
                i = 0;
            }
            li.addChildren(liChildren);
        }

        matchPage.addBodyContent(playList);
        return matchPage;
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

    public LinkedList<Play> getPlays() {
        return plays;
    }

    public void addPlay(Play play) {
        addTime(play.getPlayDurationSeconds());
        play.setScores(getScoreHome(), getScoreAway());
        play.setMatchLength(getMatchLength());
        this.plays.add(play);

        if (play.attackingTeamType == TeamType.HOME)
            homePlays++;
        else
            awayPlays++;

        Map<BludgerOutcome, Integer> bludgerOutcome =
                play.getAttackingTeamType() == TeamType.HOME
                ? homeBludgerOutcomes
                : awayBludgerOutcomes;
        bludgerOutcome.merge(play.getBludgerOutcome(), 1, Integer::sum);

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

    public String outcomesToString() {
        return "Outcomes:\n" +
                "\tHome: " +
                "\n\t\tQuaffle: " + homeQuaffleOutcomes.toString() +
                "\n\t\tBludger: " + homeBludgerOutcomes.toString() +
                "\n\t\tSnitch: " + homeSnitchOutcomes.toString() +
                "\n\tAway:\n\t\tQuaffle: " + awayQuaffleOutcomes.toString() +
                "\n\t\tBludger: " + awayBludgerOutcomes.toString() +
                "\n\t\tSnitch: " + awaySnitchOutcomes.toString();
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
