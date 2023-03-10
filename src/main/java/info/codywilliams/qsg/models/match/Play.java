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

import info.codywilliams.qsg.models.player.Beater;

public abstract class Play {
    public enum BludgerOutcome {BLOCKED, HIT, MISS}
    BludgerOutcome bludgerOutcome;
    Beater beaterHitter;
    Beater beaterBlocker;

    int playDurationSeconds;

    public BludgerOutcome getBludgerOutcome() {
        return bludgerOutcome;
    }

    public void setBludgerOutcome(BludgerOutcome bludgerOutcome) {
        this.bludgerOutcome = bludgerOutcome;
    }

    public Beater getBeaterHitter() {
        return beaterHitter;
    }

    public void setBeaterHitter(Beater beaterHitter) {
        this.beaterHitter = beaterHitter;
    }

    public Beater getBeaterBlocker() {
        return beaterBlocker;
    }

    public void setBeaterBlocker(Beater beaterBlocker) {
        this.beaterBlocker = beaterBlocker;
    }

    public int getPlayDurationSeconds() {
        return playDurationSeconds;
    }

    public void setPlayDurationSeconds(int playDurationSeconds) {
        this.playDurationSeconds = playDurationSeconds;
    }

    public enum TeamType {HOME, AWAY}
}
