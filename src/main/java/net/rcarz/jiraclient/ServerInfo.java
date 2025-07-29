/*
 * Copyright (c) 2012-2019 Continuum Security SLNE.  All rights reserved
 */
package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Represents the server info.
 */
public class ServerInfo extends Resource {

    private String baseUrl;
    private String version;
    private List<Integer> versionNumbers;
    private String deploymentType;
    private int buildNumber;
    private String buildDate;
    private String serverTime;
    private String scmInfo;
    private String serverTitle;

    public static final String CLOUD = "Cloud";
    public static final String SERVER = "Server";

    /**
     * Creates a server info from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    public ServerInfo(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null) {
            deserialise(json);
        }
    }

    public void deserialise(JsonNode json) {
        baseUrl = Field.getString(json.get("baseUrl"));
        version = Field.getString(json.get("version"));
        versionNumbers = Field.getIntegerArray(json.get("versionNumbers"));
        deploymentType = Field.getString(json.get("deploymentType"));
        buildNumber = Field.getInteger(json.get("buildNumber"));
        buildDate = Field.getString(json.get("buildDate"));
        serverTime = Field.getString(json.get("serverTime"));
        scmInfo = Field.getString(json.get("scmInfo"));
        serverTitle = Field.getString(json.get("serverTitle"));
    }

    /**
     * Retrieves the server info.
     *
     * @param restclient REST client instance
     *
     * @return a server info instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static ServerInfo get(RestClient restclient)
            throws JiraException {

        JsonNode result = null;

        try {
            result = restclient.get(getBaseUri() + "serverInfo");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve ServerInfo", ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return new ServerInfo(restclient, result);
    }

    @Override
    public String toString() {
        return getServerTitle();
    }


    public String getBaseUrl() {
        return baseUrl;
    }

    public String getVersion() {
        return version;
    }

    public List<Integer> getVersionNumbers() {
        return versionNumbers;
    }

    public String getDeploymentType() {
        return deploymentType;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public String getServerTime() {
        return serverTime;
    }

    public String getScmInfo() {
        return scmInfo;
    }

    public String getServerTitle() {
        return serverTitle;
    }
}
