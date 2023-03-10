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

package info.codywilliams.qsg.models.match;

import java.net.URL;

public class Tactic {

    /**
     * Name of the tactic
     */
    String name;
    /**
     * Description of the tactic
     */
    String description;
    /**
     * Text used to describe the tactic when the play is output.
     */
    String playText;
    /**
     * URL to webpage describing the tactic
     */
    String url;
    /**
     * QuaffleOutcome the tactic applies to
     */
    PlayChaser.QuaffleOutcome quaffleOutcome;
    /**
     * SnitchOutcome the tactic applies to
     */
    PlaySeeker.SnitchOutcome snitchOutcome;
    /**
     * BludgerOutcome the tactic applies to
     */
    Play.BludgerOutcome bludgerOutcome;
    /**
     * If beaters are used in this tactic, how many.
     */
    int numBeaters;
    /**
     * If Chasers are used in this tactic, how many.
     */
    int numChasers;

    public Tactic() {
    }

    public Tactic(String name, String description, String playText, String url, PlayChaser.QuaffleOutcome quaffleOutcome, PlaySeeker.SnitchOutcome snitchOutcome, Play.BludgerOutcome bludgerOutcome, int numBeaters, int numChasers) {
        this.name = name;
        this.description = description;
        this.playText = playText;
        this.url = url;
        this.quaffleOutcome = quaffleOutcome;
        this.snitchOutcome = snitchOutcome;
        this.bludgerOutcome = bludgerOutcome;
        this.numBeaters = numBeaters;
        this.numChasers = numChasers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlayText() {
        return playText;
    }

    public void setPlayText(String playText) {
        this.playText = playText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PlayChaser.QuaffleOutcome getQuaffleOutcome() {
        return quaffleOutcome;
    }

    public void setQuaffleOutcome(PlayChaser.QuaffleOutcome quaffleOutcome) {
        this.quaffleOutcome = quaffleOutcome;
    }

    public PlaySeeker.SnitchOutcome getSnitchOutcome() {
        return snitchOutcome;
    }

    public void setSnitchOutcome(PlaySeeker.SnitchOutcome snitchOutcome) {
        this.snitchOutcome = snitchOutcome;
    }

    public Play.BludgerOutcome getBludgerOutcome() {
        return bludgerOutcome;
    }

    public void setBludgerOutcome(Play.BludgerOutcome bludgerOutcome) {
        this.bludgerOutcome = bludgerOutcome;
    }

    public int getNumBeaters() {
        return numBeaters;
    }

    public void setNumBeaters(int numBeaters) {
        this.numBeaters = numBeaters;
    }

    public int getNumChasers() {
        return numChasers;
    }

    public void setNumChasers(int numChasers) {
        this.numChasers = numChasers;
    }
}
