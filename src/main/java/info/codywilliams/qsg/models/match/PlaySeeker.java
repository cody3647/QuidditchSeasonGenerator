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
    Seeker seeker;
    Seeker otherSeeker;
    Snitch snitch;
    public PlaySeeker(Seeker seeker, Seeker otherSeeker, TeamType attackingTeamType) {
        this.seeker = seeker;
        this.otherSeeker = otherSeeker;
        this.attackingTeamType = attackingTeamType;
    }

    public void swapTeam() {
        attackingTeamType = switch (attackingTeamType) {
            case HOME -> TeamType.AWAY;
            case AWAY -> TeamType.HOME;
        };

        Seeker temp = seeker;
        seeker = otherSeeker;
        otherSeeker = temp;
    }

    public Seeker getSeeker() {
        return seeker;
    }

    public void setSeeker(Seeker seeker) {
        this.seeker = seeker;
    }

    public Seeker getOtherSeeker() {
        return otherSeeker;
    }

    public void setOtherSeeker(Seeker otherSeeker) {
        this.otherSeeker = otherSeeker;
    }

    public Snitch getSnitchOutcome() {
        return snitch;
    }

    public void setSnitchOutcome(Snitch snitch) {
        this.snitch = snitch;
    }

    public boolean isSnitchCaught() {
        return snitch == Snitch.CAUGHT || snitch == Snitch.STOLEN;
    }

    @Override
    public String toString() {
        return "PlaySeeker{" +
                "teamType=" + attackingTeamType +
                ", seeker=" + seeker.getName() +
                ", snitchOutcome=" + snitch +
                ", bludgerOutcome=" + bludger +
                ", beaterHitter=" + ((beaterHitter != null) ? beaterHitter.getName() : "") +
                ", beaterBlocker=" + ((beaterBlocker != null) ? beaterBlocker.getName() : "") +
                ", playDurationSeconds=" + playDurationSeconds +
                '}';
    }

}
