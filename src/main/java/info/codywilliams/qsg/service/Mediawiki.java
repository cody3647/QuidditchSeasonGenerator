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
import java.util.*;
import java.util.stream.Collectors;

import static info.codywilliams.qsg.App.mapper;

public class Mediawiki {
    final String apiUrlString;
    final private HttpClient client;
    final private Logger logger = LoggerFactory.getLogger(Mediawiki.class);
    private boolean loggedIn = false;

    public Mediawiki(String apiUrlString) {
        this.apiUrlString = apiUrlString;

        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
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

    private static HttpRequest.BodyPublisher convertMultipartDataToBodyPublisher(Map<String, String> formData, String boundary) {
        ArrayList<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition:form-data; name=").getBytes(StandardCharsets.UTF_8);

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            byteArrays.add(separator);

            byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private static String randomString(int numChars) {
        Random random = new Random();
        return random.ints(numChars, 'a', 'z').mapToObj(i -> Character.toString((char) i)).collect(Collectors.joining());
    }

    public Response login(String username, String password) throws IOException {
        URI uri;
        HttpRequest request;

        logger.info("Login - Attempting to login user: {}", username);
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
        logger.info("Login: {}, {}", clientLogin.getStatus(), clientLogin.getMessageCode());

        return loggedIn
                ? new Response(true, null, null)
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
        logger.info("PageExists - {}, exists: {}", query.getPages().get(0).getTitle(), !query.getPages().get(0).isMissing());

        if (errorReturned(query)) return false;

        return !query.getPages().get(0).isMissing();
    }

    public Response createPage(String pageName, String content) throws IOException {
        if (!loggedIn) {
            String message = "Must be logged in to edit pages with this tool.";
            logger.error(message);
            return new Response(false, "nologgedin", message);
        }
        logger.info("CreatePage - Attempting to edit/create {}", pageName);
        URI uri = buildUri("query",
                Map.of("meta", "tokens",
                        "prop", "info|revisions",
                        "titles", pageName
                )
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();
        Query query = apiCall(request, Query.field, Query.class);
        if (errorReturned(query)) {
            return new Response(false, query.getError().getCode(), query.getError().getInfo());
        }

        String csrfToken = query.getToken("csrftoken");
        Page firstPage = query.getPages().get(0);

        Map<String, String> params = new HashMap<>();
        params.put("title", pageName);
        params.put("starttimestamp", query.getCurrentTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));

        if (firstPage != null) {
            List<Revision> revisions = firstPage.getRevisions();
            if (revisions != null && revisions.get(0) != null)
                params.put("basetimestamp", revisions.get(0).getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        uri = buildUri(Edit.field, params);

        String boundary = randomString(50);
        HttpRequest.BodyPublisher formBody = convertMultipartDataToBodyPublisher(
                Map.of("text", content,
                        "token", csrfToken),
                boundary
        );

        request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(formBody)
                .build();

        Edit edit = apiCall(request, Edit.field, Edit.class);

        if (errorReturned(edit)) {
            return new Response(false, edit.getError().getCode(), edit.getError().getInfo());
        }
        logger.info("CreatePage - Edit of {} {}, new page: {}, no change: {}", edit.getTitle(), edit.getResult(), edit.isNewPage(), edit.isNoChange());
        String messageCode = null;
        if (edit.isNewPage())
            messageCode = "newpage";
        else if (edit.isNoChange())
            messageCode = "nochange";
        return new Response(true, messageCode, null);
    }

    private <T extends MediaikiApiResponse> T apiCall(HttpRequest request, String field, Class<T> type) throws IOException {
        try {
            logger.debug("{}: {}", request.method(), request.uri());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("Response Code: {}, Content-Type: {}", response.statusCode(), response.headers().map().get("content-type"));
            if (response.statusCode() != 200) {
                logger.error("Unexpected response: {}", response.statusCode());
                throw new IOException("Unexpected response: " + response.statusCode());
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

    public boolean errorReturned(MediaikiApiResponse mediaikiApiResponse) {
        if (mediaikiApiResponse.getError() != null) {
            logger.error("{}: {}", mediaikiApiResponse.getError().getCode(), mediaikiApiResponse.getError().getInfo());
            return true;
        }
        return false;
    }

    private URI buildUri(String action, @NotNull Map<String, String> queryParams) {
        String base = apiUrlString + "?action=" + action + "&format=json&curtimestamp=true&formatversion=2&";
        String url = queryParams.entrySet().stream()
                .map(entry -> entry.getKey() + '=' + encodeValue(entry.getValue()))
                .collect(Collectors.joining("&", base, ""));

        return URI.create(url);
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
