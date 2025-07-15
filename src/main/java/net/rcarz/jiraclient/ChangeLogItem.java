package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Item in a {@link ChangeLogEntry}.
 */
public class ChangeLogItem extends Resource {

    private String field = null;
    private String fieldType = null;
    private String from = null;
    private String fromString = null;
    private String to = null;
    private String toString = null;

    protected ChangeLogItem(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        field = Field.getString(json.get("field"));
        fieldType = Field.getString(json.get("fieldtype"));
        from = Field.getString(json.get("from"));
        fromString = Field.getString(json.get("fromString"));
        to = Field.getString(json.get("to"));
        toString = Field.getString(json.get("toString"));
    }

    public String getField() {
        return field;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFrom() {
        return from;
    }

    public String getFromString() {
        return fromString;
    }

    public String getTo() {
        return to;
    }

    public String getToString() {
        return toString;
    }
}