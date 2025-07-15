package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

public class IssueHistoryItem extends Resource {

    private String field;
    private String from;
    private String to;
    private String fromStr;
    private String toStr;

    public IssueHistoryItem(RestClient restclient) {
        super(restclient);
    }

    public IssueHistoryItem(RestClient restclient, JsonNode json) {
        this(restclient);
        if (json != null) {
            deserialise(json);
        }
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        field = Field.getString(json.get("field"));
        from = Field.getString(json.get("from"));
        to = Field.getString(json.get("to"));
        fromStr = Field.getString(json.get("fromString"));
        toStr = Field.getString(json.get("toString"));
    }

    public String getField() {
        return field;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFromStr() {
        return fromStr;
    }

    public String getToStr() {
        return toStr;
    }
    
}
