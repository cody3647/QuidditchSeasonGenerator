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

import java.util.List;

import static info.codywilliams.qsg.App.mapper;

public class Page extends OtherFields {
    @JsonProperty("pageid")
    int pageId;
    int ns;
    String title;
    boolean missing;
    @JsonProperty("contentmodel")
    String contentModel;
    @JsonProperty("pagelanguage")
    String pageLanguage;
    @JsonProperty("pagelanguagehtmlcode")
    String pageLanguageHtmlCode;
    @JsonProperty("pagelanguagedir")
    String pageLanguageDirection;
    String touched;
    @JsonProperty("lastrevid")
    int lastRevisionId;
    int length;
    @JsonProperty("new")
    boolean newPage;
    List<Revision> revisions;

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getNs() {
        return ns;
    }

    public void setNs(int ns) {
        this.ns = ns;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

    public String getContentModel() {
        return contentModel;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public String getPageLanguage() {
        return pageLanguage;
    }

    public void setPageLanguage(String pageLanguage) {
        this.pageLanguage = pageLanguage;
    }

    public String getPageLanguageHtmlCode() {
        return pageLanguageHtmlCode;
    }

    public void setPageLanguageHtmlCode(String pageLanguageHtmlCode) {
        this.pageLanguageHtmlCode = pageLanguageHtmlCode;
    }

    public String getPageLanguageDirection() {
        return pageLanguageDirection;
    }

    public void setPageLanguageDirection(String pageLanguageDirection) {
        this.pageLanguageDirection = pageLanguageDirection;
    }

    public String getTouched() {
        return touched;
    }

    public void setTouched(String touched) {
        this.touched = touched;
    }

    public int getLastRevisionId() {
        return lastRevisionId;
    }

    public void setLastRevisionId(int lastRevisionId) {
        this.lastRevisionId = lastRevisionId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isNewPage() {
        return newPage;
    }

    public void setNewPage(boolean newPage) {
        this.newPage = newPage;
    }

    public List<Revision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<Revision> revisions) {
        this.revisions = revisions;
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
