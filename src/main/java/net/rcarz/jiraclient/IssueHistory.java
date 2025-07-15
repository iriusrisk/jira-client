package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.Date;

public class IssueHistory extends Resource {

    private static final long serialVersionUID = 1L;
    private User user;
    private ArrayList<IssueHistoryItem> changes;
    private Date created;

    /**
     * Creates an issue history record from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected IssueHistory(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null) {
            deserialise(restclient,json);
        }
    }

    public IssueHistory(IssueHistory record, ArrayList<IssueHistoryItem> changes) {
        super(record.restclient);
        user = record.user;
        id = record.id;
        self = record.self;
        created = record.created;
        this.changes = changes;
    }

    private void deserialise(RestClient restclient, JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        user = new User(restclient, json.get("author"));
        created = Field.getDateTime(json.get("created"));
        ArrayNode items = (ArrayNode) json.get("items");
        changes = new ArrayList<>(items.size());
        for (JsonNode item : items) {
            changes.add(new IssueHistoryItem(restclient, item));
        }
    }

    public User getUser() {
        return user;
    }

    public ArrayList<IssueHistoryItem> getChanges() {
        return changes;
    }

    public Date getCreated() {
        return created;
    }

}
