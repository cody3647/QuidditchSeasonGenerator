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

import info.codywilliams.qsg.models.player.Seeker;

public class PlaySeeker extends Play {
    public enum SnitchOutcome {FEINT, SEEN, MISSED, CAUGHT, STOLEN}
    TeamType teamType;
    Seeker seeker;
    SnitchOutcome snitchOutcome;



    public PlaySeeker(Seeker seeker, TeamType teamType) {
        this.seeker = seeker;
        this.teamType = teamType;
    }

    public void swapTeamType() {
        if(teamType == TeamType.HOME)
            teamType = TeamType.AWAY;
        else
            teamType = TeamType.HOME;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public void setSeeker(Seeker seeker) {
        this.seeker = seeker;
    }

    public Seeker getSeeker() {
        return seeker;
    }

    public SnitchOutcome getSnitchOutcome() {
        return snitchOutcome;
    }
    public void setSnitchOutcome(SnitchOutcome snitchOutcome) {
        this.snitchOutcome = snitchOutcome;
    }

    public boolean isSnitchCaught(){
        return snitchOutcome == SnitchOutcome.CAUGHT || snitchOutcome == SnitchOutcome.STOLEN;
    }

    @Override
    public String toString() {
        return "PlaySeeker{" +
                "teamType=" + teamType +
                ", seeker=" + seeker.getName() +
                ", snitchOutcome=" + snitchOutcome +
                ", bludgerOutcome=" + bludgerOutcome +
                ", beaterHitter=" + ((beaterHitter != null) ? beaterHitter.getName() : "" )+
                ", beaterBlocker=" + ((beaterBlocker != null) ? beaterBlocker.getName() : "" ) +
                ", playDurationSeconds=" + playDurationSeconds +
                '}';
    }
}
