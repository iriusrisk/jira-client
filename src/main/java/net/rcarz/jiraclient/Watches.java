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
 * Represents issue watches.
 */
public class Watches extends Resource {

    private String name = null;
    private int watchCount = 0;
    private boolean isWatching = false;

    /**
     * Creates watches from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Watches(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        watchCount = Field.getInteger(json.get("watchCount"));
        isWatching = Field.getBoolean(json.get("isWatching"));
    }

    /**
     * Retrieves the given watches record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the issue
     *
     * @return a watches instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Watches get(RestClient restclient, String issue)
        throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "issue/" + issue + "/watches");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve watches for issue " + issue, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new Watches(restclient, result);
    }

    @Override
    public String toString() {
        return Integer.toString(getWatchCount());
    }

    public int getWatchCount() {
        return watchCount;
    }

    public boolean isWatching() {
        return isWatching;
    }
}

