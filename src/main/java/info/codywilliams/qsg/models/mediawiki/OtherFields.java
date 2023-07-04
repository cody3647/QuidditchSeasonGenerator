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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public abstract class OtherFields {
    Map<String, Object> otherFields = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getOtherFields() {
        return otherFields;
    }

    @JsonAnySetter
    public void setOtherField(String name, String value) {
        otherFields.put(name, value);
    }

    public String getOtherFieldNames() {
        return String.join(",", otherFields.keySet());
    }
}
