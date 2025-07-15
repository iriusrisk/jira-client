package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

public class RemoteLink extends Resource {
    private String remoteUrl;
    private String title;

    public RemoteLink(RestClient restclient, JsonNode json) {
        super(restclient);
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));

        JsonNode object = json.get("object");

        remoteUrl = Field.getString(object.get("url"));
        title = Field.getString(object.get("title"));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
}
