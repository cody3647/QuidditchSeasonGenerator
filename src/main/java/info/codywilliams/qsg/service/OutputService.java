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

import info.codywilliams.qsg.output.MatchInfobox;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.util.Formatters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OutputService {
    final ResourceBundle resources;
    final Logger logger = LoggerFactory.getLogger(OutputService.class);

    public OutputService(ResourceBundle resources) {
        this.resources = resources;
    }

    public void writePagesToHtml(String tournamentTitle, List<Page> pages) {
        // Set up an output directory with a subdirectory named after the league and year
        Path outputPath = Paths.get("output", Formatters.sanitizeFileNames(tournamentTitle));

        try {
            Files.createDirectories(outputPath);
            for (Page page : pages) {
                Path pageFile = outputPath.resolve(Formatters.sanitizeFileNames(page.getFileName()) + ".html");
                Files.writeString(pageFile, page.toHtml(0), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            Path pageFile = outputPath.resolve("QuidditchGenerator.css");
            Files.writeString(pageFile, getStylesheet(true), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Problem with HTML files", e);
        }
    }

    public void writePagesToMediawiki(String tournamentTitle, List<Page> pages, Mediawiki mediawiki) throws IOException {

        if(!mediawiki.isLoggedIn())
            return;

        try {

            for (Page page : pages) {
                logger.info("Writing: {}", page.getPageTitle());
                mediawiki.createPage(page.getPageTitle(), page.toWikitext());
                Thread.sleep(250);
            }

            mediawiki.createPage("Template:Quidditch match infobox", MatchInfobox.wikitextTemplate());
            String css = getStylesheet(false);

            mediawiki.createPage("Template:Styles/QuidditchGenerator.css", css);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStylesheet(boolean isHtml) {
        String css = "";

        if (isHtml) {
            InputStreamReader isr = new InputStreamReader(
                    getClass().getResourceAsStream("/info/codywilliams/qsg/stylesheets/Html.css"));
            BufferedReader br = new BufferedReader(isr);
            css += br.lines().collect(Collectors.joining("\n"));
        }

        InputStreamReader isr = new InputStreamReader(
                getClass().getResourceAsStream("/info/codywilliams/qsg/stylesheets/QuidditchGenerator.css"));
        BufferedReader br = new BufferedReader(isr);
        css += br.lines().collect(Collectors.joining("\n"));

        return css;
    }
}
