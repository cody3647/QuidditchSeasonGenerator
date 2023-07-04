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

package info.codywilliams.qsg.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import info.codywilliams.qsg.models.mediawiki.*;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Mediawiki {
    final public static ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    final OkHttpClient client;
    HttpUrl apiBaseUrl;

    String loginToken;

    public Mediawiki() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar())
                .addInterceptor(logging)
                .build();
    }

    public boolean login(String username, String password) throws IOException {
        HttpUrl url;
        Request request;

        url = buildUrl(Query.field, Map.of("meta", "tokens", "type", "login"));
        request = new Request.Builder()
                .url(url)
                .build();

        Query query = apiCall(request, Query.field, Query.class);
        if (errorReturned(query)) return false;
        loginToken = query.getToken("logintoken");

        url = buildUrl(ClientLogin.field, null);
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("logintoken", loginToken)
                .add("loginreturnurl", apiBaseUrl.toString())
                .build();

        request = new Request.Builder().url(url).post(formBody).build();

        ClientLogin clientLogin = apiCall(request, ClientLogin.field, ClientLogin.class);
        if (errorReturned(clientLogin)) return false;

        if (clientLogin.getStatus().equals("PASS")) {
            System.out.println("Login success");
            return true;
        }

        if (clientLogin.getStatus().equals("FAIL")) {
            System.out.println("Unable to login: " + clientLogin.getMessageCode());
            return false;
        }

        System.out.println("Login Status: " + clientLogin.getStatus() + " - " + clientLogin.getMessageCode());
        return false;
    }

    public boolean pageExists(String pageName) throws IOException {
        HttpUrl url = buildUrl(Query.field, Map.of("prop", "info", "titles", pageName));
        Request request = new Request.Builder()
                .url(url)
                .build();

        Query query = apiCall(request, Query.field, Query.class);

        if (errorReturned(query)) return false;

        return !query.getPages().get(0).isMissing();
    }

    public boolean createPage(String pageName, String content) throws IOException {
        HttpUrl url = buildUrl("query", Map.of("meta", "tokens", "prop", "info|revisions", "titles", pageName));
        Request request = new Request.Builder()
                .url(url)
                .build();
        Query query = apiCall(request, Query.field, Query.class);
        if (errorReturned(query)) return false;

        String csrfToken = query.getToken("csrftoken");
        Page firstPage = query.getPages().get(0);

        Map<String, String> params = new HashMap<>();
        params.put("title", pageName);
        params.put("starttimestamp", query.getCurrentTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));

        if (firstPage != null) {
            Revision revision = firstPage.getRevisions().get(0);
            if (revision != null)
                params.put("basetimestamp", revision.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        url = buildUrl(Edit.field, params);
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", content)
                .addFormDataPart("token", csrfToken)
                .build();
        request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Edit edit = apiCall(request, Edit.field, Edit.class);

        return !errorReturned(edit);
    }

    private <T extends MediaikiApiResponse> T apiCall(Request request, String field, Class<T> type) throws IOException {
        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code: " + response);

            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Mediawiki did not return a body in response");

            JsonNode rootNode = objectMapper.readTree(responseBody.byteStream());

            LocalDateTime currentTimestamp = null;
            String batchComplete = "";

            JsonNode batchCompleteNode = rootNode.get("batchcomplete");
            if (batchCompleteNode != null)
                batchComplete = batchCompleteNode.asText();

            JsonNode currentTimestampNode = rootNode.get("curtimestamp");

            if (currentTimestampNode != null) {
                currentTimestamp = LocalDateTime.parse(currentTimestampNode.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
            }

            T mediawikiApiResponse = objectMapper.treeToValue(rootNode.get(field), type);

            if (mediawikiApiResponse == null) {
                System.out.println("check");
                mediawikiApiResponse = objectMapper.treeToValue(rootNode, type);

                if (mediawikiApiResponse.getError() == null) {
                    Object json = objectMapper.readValue(rootNode.toString(), Object.class);

                    throw new IOException("Unknown Mediawiki API response type: \n" +
                            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
                    );
                }
            }

            mediawikiApiResponse.setFields(batchComplete, currentTimestamp);

            return mediawikiApiResponse;
        }
    }

    private boolean errorReturned(MediaikiApiResponse mediaikiApiResponse) {
        if (mediaikiApiResponse.getError() == null) return false;

        System.out.println("Mediawiki API Error: \n" + mediaikiApiResponse.getError().toString());
        return true;
    }

    public void setApiBaseUrl(String apiAddress) {
        this.apiBaseUrl = HttpUrl.parse(apiAddress);
    }

    public HttpUrl buildUrl(String action, @Nullable Map<String, String> queryParams) {
        HttpUrl.Builder builder = apiBaseUrl.newBuilder()
                .addQueryParameter("action", action)
                .addQueryParameter("format", "json")
                .addQueryParameter("curtimestamp", "true")
                .addQueryParameter("formatversion", "2");

        if (queryParams != null) {
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                builder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        return builder.build();
    }

    static class CookieJar implements okhttp3.CookieJar {
        private final Map<String, Map<String, Cookie>> siteCookieMap = new ConcurrentHashMap<>();

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
            Map<String, Cookie> cookieMap = siteCookieMap.getOrDefault(httpUrl.host(), Map.of());
            return new ArrayList<>(cookieMap.values());
        }

        @Override
        public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
            Map<String, Cookie> cookieMap = siteCookieMap.computeIfAbsent(httpUrl.host(), k -> new ConcurrentHashMap<>());
            for (Cookie cookie : list) {
                cookieMap.put(cookie.name(), cookie);
            }
        }

    }
}
