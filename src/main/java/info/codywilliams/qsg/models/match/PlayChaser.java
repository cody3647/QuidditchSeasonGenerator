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

import info.codywilliams.qsg.models.player.Chaser;
import info.codywilliams.qsg.models.player.Keeper;
import info.codywilliams.qsg.util.ResourceBundleReplacer;

public class PlayChaser extends Play {
    TeamType defendingTeamType;
    Chaser attacker;
    Chaser defender;
    Keeper defendingKeeper;
    QuaffleOutcome quaffleOutcome;
    PlayChaser() {
    }

    public PlayChaser(TeamType attackingTeamType, TeamType defendingTeamType, Chaser attacker, Chaser defender, Keeper defendingKeeper) {
        this.attackingTeamType = attackingTeamType;
        this.defendingTeamType = defendingTeamType;
        this.attacker = attacker;
        this.defender = defender;
        this.defendingKeeper = defendingKeeper;
    }

    public Chaser getAttacker() {
        return attacker;
    }

    public void setAttacker(Chaser attacker) {
        this.attacker = attacker;
    }

    public Chaser getDefender() {
        return defender;
    }

    public void setDefender(Chaser defender) {
        this.defender = defender;
    }

    public Keeper getDefendingKeeper() {
        return defendingKeeper;
    }

    public void setDefendingKeeper(Keeper defendingKeeper) {
        this.defendingKeeper = defendingKeeper;
    }

    public QuaffleOutcome getQuaffleOutcome() {
        return quaffleOutcome;
    }

    public void setQuaffleOutcome(QuaffleOutcome quaffleOutcome) {
        this.quaffleOutcome = quaffleOutcome;
    }

    @Override
    protected String getOutcomeString() {
        return quaffleOutcome.name().toLowerCase() + "." + bludgerOutcome.name().toLowerCase();
    }

    @Override
    public String outputWithDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName) {
        addCommonTokens(resources);
        resources.addToken("attacker", attacker.getShortName());
        resources.addToken("defender", defender.getShortName());
        if (defendingKeeper != null)
            resources.addToken("keeper", defendingKeeper.getShortName());

        return resources.getString("chaser." + getOutcomeString() + ".player");
    }

    @Override
    public String outputWithoutDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName) {
        addCommonTokens(resources);
        return resources.getString("chaser." + getOutcomeString());
    }

    @Override
    public String toString() {
        return "PlayChaser{" +
                "attackingTeamType=" + attackingTeamType +
                ", defendingTeamType=" + defendingTeamType +
                ", attacker=" + attacker.getName() +
                ", defender=" + defender.getName() +
                ", quaffleOutcome=" + quaffleOutcome +
                ", bludgerOutcome=" + bludgerOutcome +
                ", beaterHitter=" + ((beaterHitter != null) ? beaterHitter.getName() : "") +
                ", beaterBlocker=" + ((beaterBlocker != null) ? beaterBlocker.getName() : "") +
                ", playDurationSeconds=" + playDurationSeconds +
                '}';
    }

    public enum QuaffleOutcome {TURNOVER, MISSED, BLOCKED, SCORED}
}
