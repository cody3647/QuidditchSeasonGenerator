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

package info.codywilliams.qsg.output;

import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.output.elements.Div;
import info.codywilliams.qsg.output.elements.Link;
import info.codywilliams.qsg.output.elements.Table;
import info.codywilliams.qsg.output.elements.Text;
import info.codywilliams.qsg.util.Formatters;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MatchInfobox extends Element implements ElementOutputs {
    public Match match;
    ResourceBundle outputResourceBundle;
    LocalDateTime endTime;
    String homeTeamName;
    String awayTeamName;
    String tournamentTitle;
    String leagueName;
    String yearRange;

    public MatchInfobox(Match match, String tournamentTitle, String leagueName, String yearRange, ResourceBundle outputResourceBundle) {
        this.match = match;
        this.outputResourceBundle = outputResourceBundle;
        endTime = match.getStartDateTime().plus(match.getMatchLength());
        homeTeamName = match.getHomeTeam().getName();
        awayTeamName = match.getAwayTeam().getName();
        this.tournamentTitle = tournamentTitle;
        this.leagueName = leagueName;
        this.yearRange = yearRange;
    }

    @Override
    public String getTagName() {
        return "DIV";
    }

    @Override
    public boolean isTagClosedOnNewLine() {
        return false;
    }

    @Override
    public String toHtml(int tabs) {
        // Build the infobox header
        Div homeImageImageLink = new Div(Link.ImageLink.createTeamLink(homeTeamName));
        Div awayImageImageLink = new Div(Link.ImageLink.createTeamLink(awayTeamName));
        Div images = new Div(homeImageImageLink, awayImageImageLink);
        images.addClass("team-vs-container");

        Div homeLink = new Div(Link.TextLink.createTeamLink(homeTeamName));
        homeLink.addClass("team-vs-link");
        Div awayLink = new Div(Link.TextLink.createTeamLink(awayTeamName));
        awayLink.addClass("team-vs-link");
        Div vs = new Div(new Text(" " + outputResourceBundle.getString("match.versus.abbr")));
        vs.addClass("team-vs");
        Div teamVsTeam = new Div(homeLink, vs , awayLink);
        teamVsTeam.addClass("team-vs-container");

        Table.Cell imageData = new Table.Cell(images);
        imageData.addAttribute("colspan", "2");
        Table.HeaderCell vsData = new Table.HeaderCell(teamVsTeam);
        vsData.addAttribute("colspan", "2");

        // Create the table with the header
        Table table = new Table(new Table.Row(imageData), new Table.Row(vsData));

        Table.Cell footer = new Table.Cell(Link.TextLink.createTournamentLink(tournamentTitle, tournamentTitle));
        footer.addAttribute("colspan", "2");
        Table.Row footerRow = new Table.Row(footer);
        footerRow.addClass("ib-footer");

        // Add all the rows
        table.addChildren(
                addInfoboxHeader(outputResourceBundle.getString("match.ib.title")),
                addInfoboxRow(outputResourceBundle.getString("match.ib.location"), match.getLocation()),
                addInfoboxRow(outputResourceBundle.getString("match.ib.start"), match.getStartDateTime().format(Formatters.shortDateTimeFormatter)),
                addInfoboxRow(outputResourceBundle.getString("match.ib.end"), endTime.format(Formatters.shortDateTimeFormatter)),
                addInfoboxRow(outputResourceBundle.getString("match.ib.length"), Formatters.formatDuration(match.getMatchLength())),
                addInfoboxRow(outputResourceBundle.getString("match.ib.snitchRelease"), Formatters.formatDuration(match.getSnitchReleaseTime())),
                addInfoboxHeader(outputResourceBundle.getString("match.ib.fouls")),
                addInfoboxRow(homeTeamName, String.valueOf(match.getFoulsHome())),
                addInfoboxRow(awayTeamName, String.valueOf(match.getFoulsAway())),
                addInfoboxHeader(outputResourceBundle.getString("match.ib.finalScore")),
                addInfoboxRow(homeTeamName, String.valueOf(match.getScoreHome())),
                addInfoboxRow(awayTeamName, String.valueOf(match.getScoreAway())),
                footerRow
        );

        Div infobox = new Div(table);
        infobox.addClass("ib ib-quidditch-match");

        return infobox.toHtml(tabs);
    }

    private Table.Row addInfoboxRow(String label, String value) {
        Table.Cell dataLabel = new Table.Cell(label);
        dataLabel.addClass("ib-label");
        Table.Cell dataValue = new Table.Cell(value);
        dataValue.addClass("ib-value");

        return new Table.Row(dataLabel, dataValue);
    }

    private Table.Row addInfoboxHeader(String header) {
        Table.HeaderCell dataHeaderCell = new Table.HeaderCell(header);
        dataHeaderCell.addClass("ib-subheader");
        dataHeaderCell.addAttribute("colspan", "2");

        return new Table.Row(dataHeaderCell);
    }

    @Override
    public String toWikitext() {

        return "{{Quidditch match infobox" +
                "\n|homeTeam=" + match.getHomeTeam().getName() +
                "\n|awayTeam=" + match.getAwayTeam().getName() +
                "\n|location=" + match.getLocation() +
                "\n|start=" + match.getStartDateTime().format(Formatters.shortDateTimeFormatter) +
                "\n|end=" + endTime.format(Formatters.shortDateTimeFormatter) +
                "\n|length=" + Formatters.formatDuration(match.getMatchLength()) +
                "\n|snitchReleaseTime=" + Formatters.formatDuration(match.getSnitchReleaseTime()) +
                "\n|homeFouls=" + match.getFoulsHome() +
                "\n|awayFouls=" + match.getFoulsAway() +
                "\n|homeScore=" + match.getScoreHome() +
                "\n|awayScore=" + match.getScoreAway() +
                "\n|leagueYear=" + yearRange +
                "\n|leagueName=" + leagueName +
                "\n}}";
    }

    public static String wikitextTemplate() {
        return """
                <includeonly><div class="ib ib-quidditch-match">
                {| cellspacing="0" cellpadding="4" style="border-width: 0px;"
                {{!}}-
                {{!}} class="ib-image" colspan="2" {{!}}<div style="display:flex;">
                <div>[[File:{{{homeTeam}}}.png|125px|link={{{homeTeam}}}|alt={{{homeTeam}}}]]</div>
                <div>[[File:{{{awayTeam}}}.png|125px|link={{{awayTeam}}}|alt={{{awayTeam}}}]]</div>
                </div>
                {{!}}-
                ! class="plainlinks" colspan="2"{{!}}<div style="display:flex;">
                <div style="flex:2">[[{{{homeTeam}}}]]</div>
                <div style="flex:1">vs</div>
                <div style="flex:2">[[{{{awayTeam}}}]]</div>
                </div>
                {{!}}-
                ! colspan="2" {{!}} Quidditch Match
                {{!}}-
                {{!}} class="ib-label" {{!}} Match Location
                {{!}} class="ib-value" {{!}} {{{location|}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} Start of Game
                {{!}} class="ib-value" {{!}} {{{start|}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} End of Game
                {{!}} class="ib-value" {{!}} {{{end|}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} Length
                {{!}} class="ib-value" {{!}} {{{length|}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} Snitch Release
                {{!}} class="ib-value" {{!}} {{{snitchReleaseTime|}}}
                {{!}}-
                ! " colspan="2" {{!}} Fouls
                {{!}}-
                {{!}} class="ib-label" {{!}} {{{homeTeam}}}
                {{!}} class="ib-value" {{!}} {{{homeFouls}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} {{{awayTeam}}}
                {{!}} class="ib-value" {{!}} {{{awayFouls}}}
                {{!}}-
                ! colspan="2" {{!}} Final Score
                {{!}}-
                {{!}} class="ib-label" {{!}} {{{homeTeam}}}
                {{!}} class="ib-value" {{!}} {{{homeScore}}}
                {{!}}-
                {{!}} class="ib-label" {{!}} {{{awayTeam}}}
                {{!}} class="ib-value" {{!}} {{{awayScore}}}
                {{!}}- class="ib-footer"
                {{!}} colspan="2" {{!}} [[{{{leagueName}}} Cup ({{{leagueYear}}}) | {{{leagueYear}}} Season]]
                |}
                [[Category:{{{leagueYear}}} {{{leagueName}}} Matches]]
                </div></includeonly>
                <noinclude>
                {{Quidditch match infobox/doc}}
                                
                [[Category: Infoboxes]]
                </noinclude>
                """;
    }
}
