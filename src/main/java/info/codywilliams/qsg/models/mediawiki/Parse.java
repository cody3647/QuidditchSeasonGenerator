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

import java.util.List;

public class Parse extends MediawikiApiResponse{
    public static final String field = "parse";
    String title;
    @JsonProperty("pageid")
    int pageId;
    List<Section> sections;
    @JsonProperty("showtoc")
    boolean showToc;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public boolean isShowToc() {
        return showToc;
    }

    public void setShowToc(boolean showToc) {
        this.showToc = showToc;
    }

    public static class Section {
        @JsonProperty("toclevel")
        int tocLevel;
        String level;
        String line;
        String number;
        String index;
        @JsonProperty("fromtitle")
        String fromTitle;
        @JsonProperty("byteoffset")
        int byteOffset;
        String anchor;
        String linkAnchor;

        public int getTocLevel() {
            return tocLevel;
        }

        public void setTocLevel(int tocLevel) {
            this.tocLevel = tocLevel;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getFromTitle() {
            return fromTitle;
        }

        public void setFromTitle(String fromTitle) {
            this.fromTitle = fromTitle;
        }

        public int getByteOffset() {
            return byteOffset;
        }

        public void setByteOffset(int byteOffset) {
            this.byteOffset = byteOffset;
        }

        public String getAnchor() {
            return anchor;
        }

        public void setAnchor(String anchor) {
            this.anchor = anchor;
        }

        public String getLinkAnchor() {
            return linkAnchor;
        }

        public void setLinkAnchor(String linkAnchor) {
            this.linkAnchor = linkAnchor;
        }
    }
}
