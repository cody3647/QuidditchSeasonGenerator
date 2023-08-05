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
import info.codywilliams.qsg.models.player.Player;

public class PlayFoul extends PlayChaser {
    Player fouler;
    TeamType foulerTeamType;

    public PlayFoul(Player fouler, TeamType foulerTeamType) {
        super();
        this.fouler = fouler;
        this.foulerTeamType = foulerTeamType;
    }

    public Player getFouler() {
        return fouler;
    }

    public void setFouler(Player fouler) {
        this.fouler = fouler;
    }

    public TeamType getFoulerTeamType() {
        return foulerTeamType;
    }

    public void setFoulerTeamType(TeamType foulerTeamType) {
        this.foulerTeamType = foulerTeamType;
    }

    public void setPlayChaser(TeamType attackingTeamType, TeamType defendingTeamType, Chaser attacker, Chaser defender, Keeper defendingKeeper) {
        this.attackingTeamType = attackingTeamType;
        this.defendingTeamType = defendingTeamType;
        this.attacker = attacker;
        this.defender = defender;
        this.defendingKeeper = defendingKeeper;
    }
}
