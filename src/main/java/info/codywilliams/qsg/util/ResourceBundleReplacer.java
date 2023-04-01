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

package info.codywilliams.qsg.util;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceBundleReplacer {
    private ResourceBundle resources;
    private HashMap<String, String> tokenMap;
    private Replacer replacer;

    static private Pattern pattern = Pattern.compile("\\$\\{(\\w*)\\}");

    public ResourceBundleReplacer(ResourceBundle resources) {
        this.resources = resources;
        this.tokenMap = new HashMap<>();
        replacer = new Replacer();
    }

    public ResourceBundleReplacer(ResourceBundleReplacer resourceBundleReplacer) {
        this.resources = resourceBundleReplacer.resources;
        this.tokenMap = new HashMap<>(resourceBundleReplacer.tokenMap);
        replacer = new Replacer();
    }

    public String getString(String key) {
        String text = resources.getString(key);

        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll(replacer);
    }

    public void addToken(String key, String value) {
        if(value == null) {
            tokenMap.remove(key);
            return;
        }
        tokenMap.put(key, value);
    }

    public void addTeamToken(String key, String team) {
        tokenMap.put(key, tokenMap.getOrDefault(team, team));
    }

    private class Replacer implements Function<MatchResult, String> {
        @Override
        public String apply(MatchResult matchResult) {
            return Matcher.quoteReplacement(tokenMap.getOrDefault(matchResult.group(1), matchResult.group()));
        }
    }
}
