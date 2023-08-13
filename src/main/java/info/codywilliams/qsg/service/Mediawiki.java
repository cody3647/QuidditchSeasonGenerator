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
import info.codywilliams.qsg.models.mediawiki.*;
import info.codywilliams.qsg.util.multipart.MultipartFormDataBodyPublisher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static info.codywilliams.qsg.App.mapper;

public class Mediawiki {
    final private HttpClient client;
    final private Logger logger = LoggerFactory.getLogger(Mediawiki.class);
    String apiUrlString;
    private String username;
    private boolean loggedIn = false;
    private String token;

    public Mediawiki() {
        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        if (client.cookieHandler().isEmpty()) {
            logger.error("No cookie manager available.");
            throw new RuntimeException("No cookie manager available.");
        }
    }

    private static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static HttpRequest.BodyPublisher convertFormDataToBodyPublisher(Map<String, String> formData) {
        String formDataString = formData.entrySet().stream()
                .map(entry -> encodeValue(entry.getKey()) + '=' + encodeValue(entry.getValue()))
                .collect(Collectors.joining("&"));
        return HttpRequest.BodyPublishers.ofString(formDataString);
    }

    synchronized public Response login(String apiUrlString, String username, String password) throws IOException {
        setApiUrlString(apiUrlString);
        return login(username, password);
    }

    synchronized public Response login(String username, String password) throws IOException {
        token = null;
        String message;
        if (apiUrlString == null || apiUrlString.isEmpty()) {
            message = "Must provide API URL before logging in to mediawiki instance";
            logger.error(message);
            return new Response(false, "missingApiUrl", message);
        }
        if ((username == null || username.isEmpty()) && (this.username == null || this.username.isEmpty())) {
            message = "Must provide username to login.";
            logger.error(message);
            return new Response(false, "missingUsername", message);
        }
        if (password == null || password.isEmpty()) {
            message = "Must provide password to login.";
            logger.error(message);
            return new Response(false, "missingPassword", message);
        }
        URI uri;
        HttpRequest request;

        if (username != null)
            this.username = username;
        else
            username = this.username;

        logger.debug("Login - Attempting to login user: {}", username);
        uri = buildUri(Query.field, Map.of("meta", "tokens", "type", "login"));
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        Query query = apiCall(request, Query.field, Query.class);
        if (errorReturned(query)) {
            return new Response(false, query.getError().getCode(), query.getError().getInfo());
        }
        String loginToken = query.getToken("logintoken");

        uri = buildUri(ClientLogin.field, Map.of());

        HttpRequest.BodyPublisher formBody = convertFormDataToBodyPublisher(
                Map.of("username", username,
                        "password", password,
                        "logintoken", loginToken,
                        "loginreturnurl", apiUrlString
                )
        );

        request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(formBody)
                .build();

        ClientLogin clientLogin = apiCall(request, ClientLogin.field, ClientLogin.class);

        if (errorReturned(clientLogin)) {
            return new Response(false, query.getError().getCode(), query.getError().getInfo());
        }

        loggedIn = clientLogin.getStatus().equalsIgnoreCase("pass");
        logger.debug("Login: {}, {}", clientLogin.getStatus(), clientLogin.getMessageCode());

        return loggedIn
                ? new Response(true, "loggedIn", "Successfully logged in to " + apiUrlString)
                : new Response(false, clientLogin.getMessageCode(), clientLogin.getMessage());
    }

    public boolean pageExists(String pageName) throws IOException {
        URI url = buildUri(Query.field,
                Map.of("prop", "info",
                        "titles", pageName
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .build();

        Query query = apiCall(request, Query.field, Query.class);
        logger.debug("PageExists - {}, exists: {}", query.getPages().get(0).getTitle(), !query.getPages().get(0).isMissing());

        if (errorReturned(query)) return false;

        return !query.getPages().get(0).isMissing();
    }

    public boolean sectionExists(String pageName, String sectionName) throws IOException {
        String sectionId = getSectionId(pageName, sectionName);
        return !sectionId.equals("new");
    }

    public Response createPage(String pageName, String content) throws IOException {
        Response response = checkLoggedIn();
        if (response.isFailure())
            return response;

        logger.debug("CreatePage - Attempting to edit/create {}", pageName);
        return editPage(pageName, "text", content);
    }

    public Response appendToPage(String pageName, String content) throws IOException {
        Response response = checkLoggedIn();
        if (response.isFailure())
            return response;

        logger.debug("Appending to Page - Attempting to edit {}", pageName);
        return editPage(pageName, "appendtext", content);
    }

    public Response editPage(String pageName, String contentField, String content) throws IOException {
        Response response = getCsrfToken();
        if (response.isFailure())
            return response;

        Query query = queryInfoRevisions(pageName);
        if (errorReturned(query)) {
            return new Response(false, query.getError().getCode(), query.getError().getInfo());
        }

        Map<String, String> params = getEditRequestParams(pageName, query);

        return edit(contentField, content, params);
    }

    public Response appendToSection(String pageName, String sectionTitle, String content) throws IOException {
        return sectionEdit(pageName, sectionTitle, content, "appendtext");
    }

    public Response replaceSection(String pageName, String sectionTitle, String content) throws IOException {
        return sectionEdit(pageName, sectionTitle, content, "text");
    }

    private Response sectionEdit(String pageName, String sectionTitle, String content, String contentField) throws IOException {
        Response response = checkLoggedIn();
        if (response.isFailure())
            return response;

        response = getCsrfToken();
        if (response.isFailure())
            return response;

        String sectionId = getSectionId(pageName, sectionTitle);

        Query query = queryInfoRevisions(pageName);
        if (errorReturned(query)) {
            return new Response(false, query.getError().getCode(), query.getError().getInfo());
        }

        Map<String, String> params = getEditRequestParams(pageName, query);
        params.put("section", sectionId);
        if (sectionId.equals("new")) {
            params.put("sectiontitle", sectionTitle);
            contentField = "text";
        }

        return edit(contentField, content, params);
    }

    private Response checkLoggedIn() {
        if (!loggedIn) {
            String message = "Must be logged in to edit pages with this tool.";
            logger.error(message);
            return new Response(false, "nologgedin", message);
        }

        return new Response(true, null, null);
    }

    private Response getCsrfToken() throws IOException {
        if (token == null) {
            URI uri = buildUri("query",
                    Map.of("meta", "tokens")
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .build();
            Query query = apiCall(request, Query.field, Query.class);
            if (errorReturned(query)) {
                return new Response(false, query.getError().getCode(), query.getError().getInfo());
            }
            token = query.getToken("csrftoken");
        }

        return new Response(true, null, null);
    }

    private Query queryInfoRevisions(String pageName) throws IOException {
        URI uri = buildUri("query",
                Map.of("prop", "info|revisions",
                        "titles", pageName
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        return apiCall(request, Query.field, Query.class);
    }

    private Map<String, String> getEditRequestParams(String pageName, Query query) {
        Page firstPage = query.getPages().get(0);

        Map<String, String> params = new HashMap<>();
        params.put("title", pageName);
        params.put("starttimestamp", query.getCurrentTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));

        if (firstPage != null) {
            List<Revision> revisions = firstPage.getRevisions();
            if (revisions != null && revisions.get(0) != null)
                params.put("basetimestamp", revisions.get(0).getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        return params;
    }

    private Response edit(String contentField, String content, Map<String,String> params) throws IOException {
        URI uri = buildUri(Edit.field, params);

        MultipartFormDataBodyPublisher formBody = new MultipartFormDataBodyPublisher()
                .add(contentField, content)
                .add("summary", "Edit made by QuidditchSeasonGenerator")
                .add("token", token);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", formBody.contentType())
                .POST(formBody)
                .build();

        Edit edit = apiCall(request, Edit.field, Edit.class);

        if (edit.getError() != null && edit.getError().getCode().equals("missingparam")) {
            int i = 5;
            while (edit.getError() != null && edit.getError().getCode().equals("missingparam") && i > 0) {
                logger.warn("missingparam: Trying again {}", i);
                edit = apiCall(request, Edit.field, Edit.class);
                i--;
            }
        }

        if (errorReturned(edit)) {
            return new Response(false, edit.getError().getCode(), edit.getError().getInfo());
        }
        logger.debug("Edit - Edit of {} {}, new page: {}, no change: {}", edit.getTitle(), edit.getResult(), edit.isNewPage(), edit.isNoChange());
        String messageCode = null;
        if (edit.isNewPage())
            messageCode = "newpage";
        else if (edit.isNoChange())
            messageCode = "nochange";
        return new Response(true, messageCode, null);
    }

    private String getSectionId(String pageName, String sectionTitle) throws IOException {
        URI uri = buildUri("parse",
                Map.of("prop", "sections",
                        "page", pageName
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        Parse parseResponse = apiCall(request, Parse.field, Parse.class);

        List<Parse.Section> sections = parseResponse.getSections();
        for (Parse.Section section: sections) {
            if (section.getLine().equals(sectionTitle))
                return section.getIndex();
        }

        return "new";
    }

    public boolean errorReturned(MediawikiApiResponse mediawikiApiResponse) {
        if (mediawikiApiResponse.getError() != null) {
            logger.error("{}: {}", mediawikiApiResponse.getError().getCode(), mediawikiApiResponse.getError().getInfo());
            return true;
        }
        return false;
    }

    private <T extends MediawikiApiResponse> T apiCall(HttpRequest request, String field, Class<T> type) throws IOException {
        try {
            logger.trace("{}: {}", request.method(), request.uri());

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.trace("Response Code: {}, Content-Type: {}", response.statusCode(), response.headers().map().get("content-type"));
            if (response.statusCode() != 200) {
                logger.error("Unexpected response code: {}", response.statusCode());
                throw new IOException("Unexpected response code: " + response.statusCode());
            }

            String responseBody = response.body();
            if (responseBody == null) {
                logger.error("Response body expected but missing, status code: {}, headers: {}", response.statusCode(), response.headers());
                throw new IOException("Mediawiki did not return a body in response");
            }

            JsonNode rootNode = mapper.readTree(responseBody);

            LocalDateTime currentTimestamp = null;
            String batchComplete = "";

            JsonNode batchCompleteNode = rootNode.get("batchcomplete");
            if (batchCompleteNode != null)
                batchComplete = batchCompleteNode.asText();

            JsonNode currentTimestampNode = rootNode.get("curtimestamp");

            if (currentTimestampNode != null) {
                currentTimestamp = LocalDateTime.parse(currentTimestampNode.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
            }

            T mediawikiApiResponse = mapper.treeToValue(rootNode.get(field), type);

            if (mediawikiApiResponse == null) {
                mediawikiApiResponse = mapper.treeToValue(rootNode, type);

                if (mediawikiApiResponse.getError() == null) {
                    Object json = mapper.readValue(rootNode.toString(), Object.class);
                    logger.error("Unknown mediawiki API response type: {}", mapper.writeValueAsString(json));
                    throw new IOException("Unknown Mediawiki API response type.");
                }
            }

            mediawikiApiResponse.setFields(batchComplete, currentTimestamp);

            return mediawikiApiResponse;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI buildUri(String action, @NotNull Map<String, String> queryParams) {
        String base = apiUrlString + "?action=" + action + "&format=json&curtimestamp=true&formatversion=2&";
        String url = queryParams.entrySet().stream()
                .map(entry -> entry.getKey() + '=' + encodeValue(entry.getValue()))
                .collect(Collectors.joining("&", base, ""));

        return URI.create(url);
    }

    public String getApiUrlString() {
        return apiUrlString;
    }

    synchronized public void setApiUrlString(String apiUrlString) {
        if (!apiUrlString.equals(this.apiUrlString)) {
            loggedIn = false;
            token = null;
        }
        this.apiUrlString = apiUrlString;
    }


    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public static class Response {
        public final boolean success;
        public final String messageCode;
        public final String message;

        public Response(boolean success, @Nullable String messageCode, @Nullable String message) {
            this.success = success;
            this.messageCode = messageCode;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailure() {
            return !success;
        }

        public @Nullable String getMessageCode() {
            return messageCode;
        }

        public @Nullable String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "success=" + success +
                    ", messageCode='" + messageCode + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
