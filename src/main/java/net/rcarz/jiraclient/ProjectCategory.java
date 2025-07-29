/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a project category.
 */
public class ProjectCategory extends Resource {

    private String name = null;
    private String description = null;

    /**
     * Creates a category from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    protected ProjectCategory(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null) {
            deserialise(json);
        }
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        description = Field.getString(json.get("description"));
        name = Field.getString(json.get("name"));
    }

    /**
     * Retrieves the given status record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the status
     *
     * @return a status instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static ProjectCategory get(RestClient restclient, String id)
            throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "projectCategory/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve status " + id, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new ProjectCategory(restclient, result);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}

