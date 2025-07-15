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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Date;

/**
 * Represents an issue comment.
 */
public class Comment extends Resource {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String issueKey = null;
    private User author = null;
    private String body = null;
    private Date created = null;
    private Date updated = null;
    private User updatedAuthor = null;

    /**
     * Creates a comment from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Comment(RestClient restclient, JsonNode json, String issueKey) {
        super(restclient);

        this.issueKey = issueKey;
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        author = Field.getResource(User.class, json.get("author"), restclient);
        body = Field.getString(json.get("body"));
        created = Field.getDateTime(json.get("created"));
        updated = Field.getDateTime(json.get("updated"));
        updatedAuthor = Field.getResource(User.class, json.get("updatedAuthor"), restclient);
    }

    /**
     * Retrieves the given comment record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the associated issue
     * @param id Internal JIRA ID of the comment
     *
     * @return a comment instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Comment get(RestClient restclient, String issue, String id)
        throws JiraException {

        JsonNode result;

        try {
            result = objectMapper.readTree(restclient.get(getBaseUri() + "issue/" + issue + "/comment/" + id).toString());
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve comment " + id + " on issue " + issue, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new Comment(restclient, result, issue);
    }

    /**
     * Updates the comment body.
     *
     * @param issue associated issue record
     * @param body Comment text
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body) throws JiraException {
        update(body, null, null);
    }

    /**
     * Updates the comment body with limited visibility.
     *
     * @param issue associated issue record
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body, String visType, String visName)
        throws JiraException {

        ObjectNode req = JsonNodeFactory.instance.objectNode();
        req.put("body", body);

        if (visType != null && visName != null) {
            ObjectNode vis = JsonNodeFactory.instance.objectNode();
            vis.put("type", visType);
            vis.put("value", visName);

            req.set("visibility", vis);
        }

        JsonNode result;

        try {
            String issueUri = getBaseUri() + "issue/" + issueKey;
            result = restclient.put(issueUri + "/comment/" + id, req);
        } catch (Exception ex) {
            throw new JiraException("Failed to update comment " + id, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        deserialise(result);
    }

    @Override
    public String toString() {
        return created + " by " + author;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public Date getCreatedDate() {
        return created;
    }

    public User getUpdateAuthor() {
        return updatedAuthor;
    }

    public Date getUpdatedDate() {
        return updated;
    }
}