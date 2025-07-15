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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents issue time tracking data.
 */
public class TimeTracking {

    private String originalEstimate = null;
    private String remainingEstimate = null;
    private String timeSpent = null;
    private Integer originalEstimateSeconds = null;
    private Integer remainingEstimateSeconds = null;
    private Integer timeSpentSeconds = null;

    /**
     * Creates a time tracking structure from a JSON payload.
     *
     * @param json JSON payload
     */
    protected TimeTracking(JsonNode json) {
        originalEstimate = Field.getString(json.get("originalEstimate"));
        remainingEstimate = Field.getString(json.get("remainingEstimate"));
        timeSpent = Field.getString(json.get("timeSpent"));
        originalEstimateSeconds = Field.getInteger(json.get("originalEstimateSeconds"));
        remainingEstimateSeconds = Field.getInteger(json.get("remainingEstimateSeconds"));
        timeSpentSeconds = Field.getInteger(json.get("timeSpentSeconds"));
    }

    public TimeTracking() {
    }

    public TimeTracking(TimeTracking tt) {
        this.originalEstimate = tt.originalEstimate;
        this.remainingEstimate = tt.remainingEstimate;
        this.originalEstimateSeconds = tt.originalEstimateSeconds;
        this.remainingEstimateSeconds = tt.remainingEstimateSeconds;
        this.timeSpent = tt.timeSpent;
        this.timeSpentSeconds =tt.timeSpentSeconds;
    }

    protected ObjectNode toJsonObject() {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        if (originalEstimate != null)
            jsonObject.put("originalEstimate", originalEstimate);

        if (remainingEstimate != null)
            jsonObject.put("remainingEstimate", remainingEstimate);

        if (originalEstimateSeconds >= 0)
            jsonObject.put("originalEstimateSeconds", originalEstimateSeconds);

        if (remainingEstimateSeconds >= 0)
            jsonObject.put("remainingEstimateSeconds", remainingEstimateSeconds);

        return jsonObject;
    }

    public String getOriginalEstimate() {
        return originalEstimate;
    }

    public String getRemainingEstimate() {
        return remainingEstimate;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public int getOriginalEstimateSeconds() {
        return originalEstimateSeconds;
    }

    public int getRemainingEstimateSeconds() {
        return remainingEstimateSeconds;
    }

    public void setOriginalEstimate(String originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public void setRemainingEstimate(String remainingEstimate) {
        this.remainingEstimate = remainingEstimate;
    }

    public void setOrignalEstimateSeconds(int originalEstimateSeconds) {
        this.originalEstimateSeconds = originalEstimateSeconds;
    }

    public void setRemainingEstimateSeconds(int remainingEstimateSeconds) {
        this.remainingEstimateSeconds = remainingEstimateSeconds;
    }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }
}
