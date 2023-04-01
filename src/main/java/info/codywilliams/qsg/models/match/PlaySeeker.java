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
import info.codywilliams.qsg.util.ResourceBundleReplacer;

public class PlaySeeker extends Play {
    public enum SnitchOutcome {FEINT, SEEN, MISSED, CAUGHT, STOLEN}
    Seeker seeker;
    Seeker otherSeeker;
    SnitchOutcome snitchOutcome;



    public PlaySeeker(Seeker seeker, Seeker otherSeeker, TeamType attackingTeamType) {
        this.seeker = seeker;
        this.otherSeeker = otherSeeker;
        this.attackingTeamType = attackingTeamType;
    }

    public void swapTeam() {
        attackingTeamType = switch(attackingTeamType) {
            case HOME -> TeamType.AWAY;
            case AWAY -> TeamType.HOME;
        };

        Seeker temp = seeker;
        seeker = otherSeeker;
        otherSeeker = temp;
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
    protected String getOutcomeString(){
        return snitchOutcome.name().toLowerCase() + "." + bludgerOutcome.name().toLowerCase();
    }

    @Override
    public String outputWithDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName) {
        addCommonTokens(resources);
        resources.addToken("seeker", seeker.getName());
        resources.addToken("otherSeeker", otherSeeker.getName());

        return resources.getString("seeker." + getOutcomeString()  + ".player");
    }

    @Override
    public String outputWithoutDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName) {
        addCommonTokens(resources);
        return resources.getString("seeker." + getOutcomeString());
    }

    @Override
    public String toString() {
        return "PlaySeeker{" +
                "teamType=" + attackingTeamType +
                ", seeker=" + seeker.getName() +
                ", snitchOutcome=" + snitchOutcome +
                ", bludgerOutcome=" + bludgerOutcome +
                ", beaterHitter=" + ((beaterHitter != null) ? beaterHitter.getName() : "" )+
                ", beaterBlocker=" + ((beaterBlocker != null) ? beaterBlocker.getName() : "" ) +
                ", playDurationSeconds=" + playDurationSeconds +
                '}';
    }
}
