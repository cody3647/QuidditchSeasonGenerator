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
import info.codywilliams.qsg.util.ResourceBundleReplacer;

import java.time.LocalDateTime;

public class MatchInfobox extends Element implements ElementOutputs {
    public Match match;
    ResourceBundleReplacer resources;
    LocalDateTime endTime;
    String homeTeamName;
    String awayTeamName;

    public MatchInfobox(Match match) {
        this.match = match;
        resources = match.getResources();
        endTime = match.getStartDateTime().plus(match.getMatchLength());
        homeTeamName = match.getHomeTeam().getName();
        awayTeamName = match.getAwayTeam().getName();
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
        Link.ImageLink homeImageImageLink = Link.ImageLink.createTeamLink(homeTeamName);
        Link.ImageLink awayImageImageLink = Link.ImageLink.createTeamLink(awayTeamName);

        Link.TextLink homeLink = Link.TextLink.createTeamLink(homeTeamName);
        Link.TextLink awayLink = Link.TextLink.createTeamLink(awayTeamName);

        Table.Cell imageData = new Table.Cell(homeImageImageLink, awayImageImageLink);
        imageData.addAttribute("colspan", "2");
        Table.Cell vsData = new Table.Cell(homeLink, new Text(resources.getString("match.versus.abbr")), awayLink);
        vsData.addAttribute("colspan", "2");

        // Create the table with the header
        Table table = new Table(new Table.Row(imageData), new Table.Row(vsData));

        // Add all the rows
        table.addChildren(
                addInfoboxHeader(resources.getString("match.ib.title")),
                addInfoboxRow(resources.getString("match.ib.location"), match.getLocation()),
                addInfoboxRow(resources.getString("match.ib.start"), match.getStartDateTime().format(Formatters.dateTimeFormatter)),
                addInfoboxRow(resources.getString("match.ib.end"), endTime.format(Formatters.dateTimeFormatter)),
                addInfoboxRow(resources.getString("match.ib.length"), Formatters.formatDuration(match.getMatchLength())),
                addInfoboxHeader(resources.getString("match.ib.fouls")),
                addInfoboxRow(homeTeamName, String.valueOf(match.getFoulsHome())),
                addInfoboxRow(awayTeamName, String.valueOf(match.getFoulsAway())),
                addInfoboxHeader(resources.getString("match.ib.finalScore")),
                addInfoboxRow(homeTeamName, String.valueOf(match.getScoreHome())),
                addInfoboxRow(awayTeamName, String.valueOf(match.getScoreAway()))
        );

        // Create footer and add table and footer to infobox div
        Div footer = new Div(Link.TextLink.createTournamentLink(resources.getString("match.ib.footerText"), resources.getString("tournamentTitle")));
        Div infobox = new Div(table, footer);
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
                "\n|start=" + match.getStartDateTime().format(Formatters.dateTimeFormatter) +
                "\n|end=" + endTime.format(Formatters.dateTimeFormatter) +
                "\n|length=" + Formatters.formatDuration(match.getMatchLength()) +
                "\n|homeFouls=" + match.getFoulsHome() +
                "\n|awayFouls=" + match.getFoulsAway() +
                "\n|homeScore=" + match.getScoreHome() +
                "\n|awayScore=" + match.getScoreAway() +
                "\n|leagueYear=" + resources.getString("yearRange") +
                "\n|leagueName=" + resources.getString("leagueName") +
                "\n}}\n";
    }

    public String wikitextTemplate() {
        return null;
    }
}
