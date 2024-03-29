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

import info.codywilliams.qsg.output.Element;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OutputService {
    final private ResourceBundle outputResourceBundle;
    final Logger logger;
    private final SaveSettingsService saveSettingsService;

    public static String sanitizeDirectories(String dir) {
        if (dir.contains("/")) {
            ArrayList<String> dirList = new ArrayList<>();
            String[] directories = dir.split("/");
            for (String directory: directories) {
                dirList.add(Formatters.sanitizeFileNames(directory));
            }
            dir = String.join("/", dirList);
        }
        else {
            dir = Formatters.sanitizeFileNames(dir);
        }

        return dir;
    }

    public OutputService(ResourceBundle outputResoureBundle, SaveSettingsService saveSettingsService) {
        this.outputResourceBundle = outputResoureBundle;
        this.logger = LoggerFactory.getLogger(OutputService.class);
        this.saveSettingsService = saveSettingsService;
    }

    public void writePagesToHtml(List<Page> pages, String yearRange) {
        // Set up an output directory with a subdirectory named after the league and year
        Path outputPath = Paths.get("output");

        final String teamSeasonDivRegex = "<h3>" + yearRange + "</h3>.*?<div class=\"team-season\" id=\"team-season-" + yearRange + "\">.*?<\\/div>.*?<\\/div>";
        final Pattern teamSeasonDivPattern = Pattern.compile(teamSeasonDivRegex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        try {
            Files.createDirectories(outputPath);
            for (Page page : pages) {
                Path pageDir = outputPath.resolve(page.getDirectory());
                Files.createDirectories(pageDir);
                String fileName = page.getPageType() == Page.Type.TOURNAMENT ? "index.html" : Formatters.sanitizeFileNames(page.getPageTitle()) + ".html";
                Path pageFile = pageDir.resolve(fileName);

                switch (page.getPageType()) {
                    case TOURNAMENT -> {
                        Files.writeString(pageFile, page.toHtml(0), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        Path settingsFile = pageDir.resolve("settings.json");
                        Files.writeString(settingsFile, saveSettingsService.getSettingsJsonAsString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                    case MATCH, PLAYER ->  Files.writeString(pageFile, page.toHtml(0), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    case TEAM -> {
                        if (!Files.exists(pageFile))
                            Files.writeString(pageFile, page.toHtml(0), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        else {
                            List<Element> elements = page.getSubsetPageElements(outputResourceBundle.getString("team.seasons"), null);
                            String content = elements.stream().map(element -> element.toHtml(2)).collect(Collectors.joining("\n"));
                            String originalContent = Files.readString(pageFile);
                            Matcher matcher = teamSeasonDivPattern.matcher(originalContent);
                            if (matcher.find()) {
                                content = matcher.replaceAll(content);
                            }
                            else {
                                content = originalContent.replace("\t</div>\n</body>", content + "\n\t</div>\n</body>");
                            }
                            Files.writeString(pageFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                }
            }

            Path cssDir = outputPath.resolve("css");
            Path imagesDir = outputPath.resolve("images");

            Files.createDirectories(imagesDir);
            Files.createDirectories(cssDir);
            Path cssFile = cssDir.resolve("QuidditchGenerator.css");
            Files.writeString(cssFile, getStylesheet(true), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("Problem with HTML files", e);
        }
    }

    public void writePagesToMediawiki(List<Page> pages, Mediawiki mediawiki, String yearRange, Boolean writeTeamPages) throws IOException {
        if(!mediawiki.isLoggedIn())
            return;
        try {
            for (Page page : pages) {
                logger.info("Writing: {}", page.getPageTitle());
                switch (page.getPageType()) {
                    case TOURNAMENT ->  {
                        mediawiki.createPage(page.getPageTitle(), page.toWikitext());
                        mediawiki.createPage(page.getPageTitle() + "/settings.json", saveSettingsService.getSettingsJsonAsString());
                    }
                    case MATCH, PLAYER -> mediawiki.createPage(page.getPageTitle(), page.toWikitext());
                    case TEAM -> {
                        if(writeTeamPages) {
                            if (!mediawiki.pageExists(page.getPageTitle()))
                                mediawiki.createPage(page.getPageTitle(), page.toWikitext());
                            else {
                                List<Element> elements = page.getSubsetPageElements(outputResourceBundle.getString("team.seasons"), null);
                                String content = elements.stream().map(Element::toWikitext).collect(Collectors.joining("\n"));

                                if (mediawiki.sectionExists(page.getPageTitle(), yearRange))
                                    mediawiki.replaceSection(page.getPageTitle(), yearRange, content);
                                else
                                    mediawiki.appendToSection(
                                            page.getPageTitle(),
                                            outputResourceBundle.getString("team.seasons"),
                                            "\n" + content
                                    );
                            }
                        }
                    }
                }

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
