package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class StatusTest {

    private static final String STATUS_ID = "10004";
    private static final String DESCRIPTION = "Issue is currently in progress.";
    private static final String ICON_URL = "https://site/images/icons/statuses/open.png";

    @Test
    public void testJSONDeserializer() throws IOException, URISyntaxException {
        Status status = new Status(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(status.getDescription(), DESCRIPTION);
        assertEquals(status.getIconUrl(), ICON_URL);
        assertEquals(status.getName(), "Open");
        assertEquals(status.getId(), STATUS_ID);
    }

    @Test
    public void testGetStatus() throws Exception {
        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString())).thenReturn(getTestJSON());
        Status status = Status.get(restClient,"someID");
        assertEquals(status.getDescription(), DESCRIPTION);
        assertEquals(status.getIconUrl(), ICON_URL);
        assertEquals(status.getName(), "Open");
        assertEquals(status.getId(), STATUS_ID);
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromRestException() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        when(mockRestClient.get(anyString())).thenThrow(RestException.class);
        Status.get(mockRestClient, "issueNumber");
    }

    @Test(expected = JiraException.class)
    public void testJiraExceptionFromNonJSON() throws Exception {
        final RestClient mockRestClient = PowerMockito.mock(RestClient.class);
        Status.get(mockRestClient,"issueNumber");
    }

    private ObjectNode getTestJSON() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("description", DESCRIPTION);
        json.put("name", "Open");
        json.put("iconUrl", ICON_URL);
        json.put("id", STATUS_ID);

        return json;
    }

    @Test
    public void testStatusToString() throws URISyntaxException {
        Status status = new Status(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals("Open",status.toString());
    }
}
