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
    static private final Pattern pattern = Pattern.compile("\\$\\{(\\w*)\\}");
    private final ResourceBundle resourceBundle;
    private final HashMap<String, String> tokenMap;
    private final Replacer replacer;

    public ResourceBundleReplacer(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        this.tokenMap = new HashMap<>();
        replacer = new Replacer();
    }

    public String getString(String key) {
        String text = resourceBundle.getString(key);
        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll(replacer);
    }

    public String getStringWithTempTokens(String key, Map<String, String> tempTokenMap) {
        String text = resourceBundle.getString(key);
        Matcher matcher = pattern.matcher(text);

        replacer.setTempTokenMap(tempTokenMap);
        text = matcher.replaceAll(replacer);
        replacer.clearTempTokenMap();
        return text;
    }

    public void addToken(String key, String value) {
        if (value == null) {
            tokenMap.remove(key);
            return;
        }
        tokenMap.put(key, value);
    }

    private class Replacer implements Function<MatchResult, String> {
        Map<String, String> tempTokenMap = Map.of();
        @Override
        public String apply(MatchResult matchResult) {
            if (tempTokenMap.containsKey(matchResult.group(1))) {
                return Matcher.quoteReplacement(tempTokenMap.get(matchResult.group(1)));
            }
            return Matcher.quoteReplacement(tokenMap.getOrDefault(matchResult.group(1), matchResult.group()));
        }

        public void clearTempTokenMap() {
            tempTokenMap = Map.of();
        }

        public void setTempTokenMap(Map<String, String> tempTokenMap) {
            this.tempTokenMap = tempTokenMap;
        }
    }
}
