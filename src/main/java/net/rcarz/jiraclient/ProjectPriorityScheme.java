/*
 * Copyright (c) 2012-2019 Continuum Security SLNE.  All rights reserved
 */
package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

public class ProjectPriorityScheme extends PriorityScheme {

    /**
     * Creates a priority scheme from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    public ProjectPriorityScheme(RestClient restclient, JsonNode json) {
        super(restclient, json);

        if (json != null)
            deserialise(json);
    }

    /**
     * Retrieves the given priority scheme record.
     *
     * @param restclient REST client instance
     * @param project Internal JIRA ID of the associated project
     *
     * @return a priority scheme instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static PriorityScheme get(RestClient restclient, String project)
            throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "project/" + project + "/priorityscheme");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve property scheme on project " + project, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new PriorityScheme(restclient, result);
    }
}
