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

/**
 * Represents an issue type.
 */
public class IssueType extends Resource {

    private String description = null;
    private String iconUrl = null;
    private String name = null;
    private boolean subtask = false;
    private JsonNode fields = null;

    /**
     * Creates an issue type from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json       JSON payload
     */
    protected IssueType(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        description = Field.getString(json.get("description"));
        iconUrl = Field.getString(json.get("iconUrl"));
        name = Field.getString(json.get("name"));
        subtask = Field.getBoolean(json.get("subtask"));

        if (json.has("fields") && json.get("fields").isObject()) {
            fields = json.get("fields");
        }
    }

    /**
     * Retrieves the given issue type record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the issue type
     *
     * @return an issue type instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static IssueType get(RestClient restclient, String id)
        throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "issuetype/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue type " + id, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new IssueType(restclient, result);
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

    public String getIconUrl() {
        return iconUrl;
    }

    public boolean isSubtask() {
        return subtask;
    }

    public JsonNode getFields() {
        return fields;
    }
}

