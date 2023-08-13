/*
 * Quidditch Season Generator
 * Copyright (C) 2022.  Cody Williams
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.player.Beater;
import info.codywilliams.qsg.models.player.Chaser;
import info.codywilliams.qsg.models.player.Keeper;
import info.codywilliams.qsg.models.player.Seeker;

import java.io.IOException;
import java.util.List;

public class TeamDeserializer extends StdDeserializer<Team> {
    public TeamDeserializer() {
        this(null);
    }

    public TeamDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     * <p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker or a FIELD_NAME.
     * </p>
     * <p>
     * The two possible input conditions for structured types result
     * from polymorphism via fields. In the ordinary case, Jackson
     * calls this method when it has encountered an OBJECT_START,
     * and the method implementation must advance to the next token to
     * see the first field name. If the application configures
     * polymorphism via a field, then the object looks like the following.
     * <pre>
     *      {
     *          "@class": "class name",
     *          ...
     *      }
     *  </pre>
     * Jackson consumes the two tokens (the <tt>@class</tt> field name
     * and its value) in order to learn the class and select the deserializer.
     * Thus, the stream is pointing to the FIELD_NAME for the first field
     * after the @class. Thus, if you want your method to work correctly
     * both with and without polymorphism, you must begin your method with:
     * <pre>
     *       if (p.currentToken() == JsonToken.START_OBJECT) {
     *         p.nextToken();
     *       }
     *  </pre>
     * This results in the stream pointing to the field name, so that
     * the two conditions align.
     * <p>
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     * <p>
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param p    Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about
     *             this deserialization activity.
     * @return Deserialized value
     */
    @Override
    public Team deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Team team = new Team();

        team.setName(node.get("name").asText(""));
        team.setShortName(node.get("shortName").asText(""));
        team.setHome(node.get("home").asText(""));

        deserializePlayerList(node.get("beaters"), ctxt, team.getBeaters(), Beater.class);
        deserializePlayerList(node.get("chasers"), ctxt, team.getChasers(), Chaser.class);
        deserializePlayerList(node.get("keepers"), ctxt, team.getKeepers(), Keeper.class);
        deserializePlayerList(node.get("seekers"), ctxt, team.getSeekers(), Seeker.class);
        return team;
    }

    private <T> void deserializePlayerList(JsonNode listNode, DeserializationContext ctxt, List<T> playerList, Class<T> playerClass) throws IOException {
        for (JsonNode n : listNode)
            playerList.add(ctxt.readTreeAsValue(n, playerClass));
    }
}
