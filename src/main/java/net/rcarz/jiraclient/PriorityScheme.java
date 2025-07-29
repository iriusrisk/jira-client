/*
 * Copyright (c) 2012-2019 Continuum Security SLNE.  All rights reserved
 */
package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a priority scheme.
 */
public class PriorityScheme extends Resource {

    private List<String> prioritiesIds;
    private String name;
    private String description;
    private boolean defaultScheme;

    /**
     * Creates a priority scheme from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    public PriorityScheme(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    public void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        prioritiesIds = Field.getStringArray(json.get("optionIds"));
        description = Field.getString(json.get("description"));
        name = Field.getString(json.get("name"));
        defaultScheme = Field.getBoolean(json.get("defaultScheme"));
    }

    /**
     * Retrieves the given priority scheme record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the priorities scheme
     *
     * @return a priority scheme instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static PriorityScheme get(RestClient restclient, String id)
            throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "priorityschemes/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve priority " + id, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new PriorityScheme(restclient, result);
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<String> getPrioritiesIds() {
        return prioritiesIds;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultScheme() {
        return defaultScheme;
    }

    public List<Priority> getPriorities() throws JiraException {

        List<Priority> priorities = new ArrayList<Priority>();

        try {
            for (String priorityId: prioritiesIds) {
                priorities.add(Priority.get(restclient, priorityId));
            }
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve priorities", ex);
        }

        return priorities;
    }
}
