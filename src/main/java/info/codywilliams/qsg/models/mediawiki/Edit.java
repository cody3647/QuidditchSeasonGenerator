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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;

import static info.codywilliams.qsg.App.mapper;

public class Edit extends MediaikiApiResponse {
    public static final String field = "edit";
    String result;
    @JsonProperty("pageid")
    int pageId;
    String title;
    @JsonProperty("contentmodel")
    String contentModel;
    @JsonProperty("oldrevid")
    int oldRevisionId;
    @JsonProperty("newrevid")
    int newRevisionId;
    @JsonProperty("newtimestamp")
    LocalDateTime newTimestamp;
    String watched;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentModel() {
        return contentModel;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public int getOldRevisionId() {
        return oldRevisionId;
    }

    public void setOldRevisionId(int oldRevisionId) {
        this.oldRevisionId = oldRevisionId;
    }

    public int getNewRevisionId() {
        return newRevisionId;
    }

    public void setNewRevisionId(int newRevisionId) {
        this.newRevisionId = newRevisionId;
    }

    public LocalDateTime getNewTimestamp() {
        return newTimestamp;
    }

    public void setNewTimestamp(LocalDateTime newTimestamp) {
        this.newTimestamp = newTimestamp;
    }

    public String getWatched() {
        return watched;
    }

    public void setWatched(String watched) {
        this.watched = watched;
    }

    @Override
    public String toString() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Unable to convert to JSON string";
        }
    }
}
