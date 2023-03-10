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

import info.codywilliams.qsg.models.player.Player;

public class PlayChaser extends Play {
    public enum QuaffleOutcome {TURNOVER, MISS, BLOCK, SCORE}

    TeamType attackingTeamType;
    TeamType defendingTeamType;
    Player attacker;
    Player defender;
    QuaffleOutcome quaffleOutcome;

    public PlayChaser(TeamType attackingTeamType, TeamType defendingTeamType, Player attacker, Player defender) {
        this.attackingTeamType = attackingTeamType;
        this.defendingTeamType = defendingTeamType;
        this.attacker = attacker;
        this.defender = defender;
    }

    public Player getAttacker() {
        return attacker;
    }

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }

    public Player getDefender() {
        return defender;
    }

    public void setDefender(Player defender) {
        this.defender = defender;
    }

    public QuaffleOutcome getQuaffleOutcome() {
        return quaffleOutcome;
    }

    public void setQuaffleOutcome(QuaffleOutcome quaffleOutcome) {
        this.quaffleOutcome = quaffleOutcome;
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
                ", beaterHitter=" + ((beaterHitter != null) ? beaterHitter.getName() : "" )+
                ", beaterBlocker=" + ((beaterBlocker != null) ? beaterBlocker.getName() : "" ) +
                ", playDurationSeconds=" + playDurationSeconds +
                '}';
    }
}
