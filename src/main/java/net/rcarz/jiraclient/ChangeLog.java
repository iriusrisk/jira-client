package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Issue change log.
 */
public class ChangeLog extends Resource {

    private List<ChangeLogEntry> entries = null;

    protected ChangeLog(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        entries = Field.getResourceArray(ChangeLogEntry.class, json.get(Field.CHANGE_LOG_ENTRIES), restclient);
    }

    public List<ChangeLogEntry> getEntries() {
        return entries;
    }
}