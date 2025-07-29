package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;
import java.util.List;

/**
 * Contains information about an issue change log entry.
 */
public class ChangeLogEntry extends Resource {

    private User author = null;
    private Date created = null;
    private List<ChangeLogItem> items = null;

    protected ChangeLogEntry(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        id = Field.getString(json.get("id"));
        author = Field.getResource(User.class, json.get("author"), restclient);
        created = Field.getDateTime(json.get("created"));
        items = Field.getResourceArray(ChangeLogItem.class, json.get(Field.CHANGE_LOG_ITEMS), restclient);
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreated() {
        return created;
    }

    public List<ChangeLogItem> getItems() {
        return items;
    }
}