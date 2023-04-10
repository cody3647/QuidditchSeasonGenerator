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
import info.codywilliams.qsg.output.elements.*;
import info.codywilliams.qsg.util.Formatters;
import info.codywilliams.qsg.util.ResourceBundleReplacer;

import java.time.LocalDateTime;

public class MatchInfobox extends Element implements Outputs{
    public Match match;
    ResourceBundleReplacer resources;
    LocalDateTime endTime;
    String homeTeamName;
    String awayTeamName;

    public MatchInfobox(Match match) {
        super("infobox");
        this.match = match;
        resources = match.getResources();
        endTime = match.getStartDateTime().plus(match.getMatchLength());
        homeTeamName = match.getHomeTeam().getName();
        awayTeamName = match.getAwayTeam().getName();
    }

    @Override
    public String toHtml() {
        // Build the infobox header
        Link homeImageLink = new Link.Team(new Image(homeTeamName, homeTeamName + ".png"), homeTeamName, homeTeamName);
        Link awayImageLink = new Link.Team(new Image(awayTeamName, awayTeamName + ".png"), awayTeamName, awayTeamName);

        Link homeLink = new Link.Team(homeTeamName, homeTeamName);
        Link awayLink = new Link.Team(awayTeamName, awayTeamName);

        TableData imageData = new TableData(homeImageLink, awayImageLink);
        imageData.addAttribute("colspan", "2");
        TableData vsData = new TableData(homeLink, new Text(resources.getString("match.versus.abbr")), awayLink);
        vsData.addAttribute("colspan", "2");
        
        // Create the table with the header
        Table table = new Table(new TableRow(imageData), new TableRow(vsData));

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
        Div footer = new Div(new Link.Tournament(resources.getString("match.ib.footerLink")));
        Div infobox = new Div(table, footer);
        infobox.addClass("ib ib-quidditch-match");

        return infobox.toHtml();
    }

    private TableRow addInfoboxRow(String label, String value) {
        TableData dataLabel = new TableData(label);
        dataLabel.addClass("ib-label");
        TableData dataValue = new TableData(value);
        dataValue.addClass("ib-value");

        return new TableRow(dataLabel, dataValue);
    }

    private TableRow addInfoboxHeader(String header) {
        TableData.Header dataHeader = new TableData.Header(header);
        dataHeader.addClass("ib-subheader");
        dataHeader.addAttribute("colspan", "2");

        return new TableRow(dataHeader);
    }

    @Override
    public String toWikitext() {
        StringBuilder stringBuilder = new StringBuilder("{{Quidditch match infobox");
        stringBuilder
                .append("\n|homeTeam=").append(match.getHomeTeam().getName())
                .append("\n|awayTeam=").append(match.getAwayTeam().getName())
                .append("\n|location=").append(match.getLocation())
                .append("\n|start=").append(match.getStartDateTime().format(Formatters.dateTimeFormatter))
                .append("\n|end=").append(endTime.format(Formatters.dateTimeFormatter))
                .append("\n|length=").append(Formatters.formatDuration(match.getMatchLength()))
                .append("\n|homeFouls=").append(match.getFoulsHome())
                .append("\n|awayFouls=").append(match.getFoulsAway())
                .append("\n|homeScore=").append(match.getScoreHome())
                .append("\n|awayScore=").append(match.getScoreAway())
                .append("\n|leagueYear=").append(resources.getString("yearRange"))
                .append("\n|leagueName=").append(resources.getString("leagueName"));

        stringBuilder.append("\n}}\n");
        return stringBuilder.toString();
    }

    public String wikitextTemplate() {
        return null;
    }
}
