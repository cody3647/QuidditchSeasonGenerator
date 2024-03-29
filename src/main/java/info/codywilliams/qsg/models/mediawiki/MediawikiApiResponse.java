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

package info.codywilliams.qsg.models.mediawiki;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public abstract class MediawikiApiResponse extends OtherFields {
    @JsonIgnore
    String batchComplete = "";
    @JsonIgnore
    LocalDateTime currentTimestamp;

    Error error;

    public String getBatchComplete() {
        return batchComplete;
    }

    public LocalDateTime getCurrentTimestamp() {
        return currentTimestamp;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void setFields(String batchComplete, LocalDateTime currentTimestamp) {
        this.batchComplete = batchComplete;
        this.currentTimestamp = currentTimestamp;
    }
}
