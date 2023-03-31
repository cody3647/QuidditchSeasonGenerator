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
import info.codywilliams.qsg.util.DependencyInjector;
import info.codywilliams.qsg.util.Formatters;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MatchInfobox extends Element implements Outputs{
    public Match match;
    ResourceBundle resources;
    LocalDateTime endTime;
    String homeTeamName;
    String awayTeamName;

    public MatchInfobox(Match match) {
        super("infobox");
        this.match = match;
        resources = DependencyInjector.getBundle();
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
        table.addChildren(addInfoboxHeader(resources.getString("match.ib.title")));
        table.addChildren(addInfoboxRow(resources.getString("match.ib.location"), match.getLocation()));
        table.addChildren(addInfoboxRow(resources.getString("match.ib.start"), match.getStartDateTime().format(Formatters.dateTimeFormatter)));
        table.addChildren(addInfoboxRow(resources.getString("match.ib.end"), endTime.format(Formatters.dateTimeFormatter)));
        table.addChildren(addInfoboxRow(resources.getString("match.ib.length"), match.getMatchLength().toString()));
        table.addChildren(addInfoboxHeader("match.ib.fouls"));
        table.addChildren(addInfoboxRow(homeTeamName, String.valueOf(match.getFoulsHome())));
        table.addChildren(addInfoboxRow(awayTeamName, String.valueOf(match.getFoulsAway())));
        table.addChildren(addInfoboxHeader("match.ib.finalScore"));
        table.addChildren(addInfoboxRow(homeTeamName, String.valueOf(match.getScoreHome())));
        table.addChildren(addInfoboxRow(awayTeamName, String.valueOf(match.getScoreAway())));

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
}
