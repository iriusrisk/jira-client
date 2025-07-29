package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class UserTest {

    private static final String USERNAME_VALUE = "joseph";
    private static final String DISPLAYNAME_VALUE = "Joseph McCarthy";
    private static final String EMAIL_VALUE = "joseph.b.mccarthy2012@googlemail.com";
    private static final String USERID_VALUE = "10";
    private static final boolean ACTIVE_VALUE = true;
    private static final String SELF_VALUE = "https://brainbubble.atlassian.net/rest/api/2/user?username=joseph";

    @Test
    public void testJSONDeserializer() throws URISyntaxException {
        User user = new User(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(user.getName(), USERNAME_VALUE);
        assertEquals(user.getDisplayName(), DISPLAYNAME_VALUE);
        assertEquals(user.getEmail(), EMAIL_VALUE);
        assertEquals(user.getId(), USERID_VALUE);

        Map<String, String> avatars = user.getAvatarUrls();

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));

        assertTrue(user.isActive());
    }

    private ObjectNode getTestJSON() {
        ObjectNode json = JsonNodeFactory.instance.objectNode();

        json.put("name", USERNAME_VALUE);
        json.put("email", EMAIL_VALUE);
        json.put("active", ACTIVE_VALUE);
        json.put("displayName", DISPLAYNAME_VALUE);
        json.put("self", SELF_VALUE);

        ObjectNode images = JsonNodeFactory.instance.objectNode();
        images.put("16x16", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16");
        images.put("24x24", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24");
        images.put("32x32", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32");
        images.put("48x48", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48");

        json.set("avatarUrls", images);
        json.put("id", "10");

        return json;
    }

    @Test
    public void testStatusToString() throws URISyntaxException {
        User user = new User(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(USERNAME_VALUE, user.toString());
    }

    @Test(expected = JiraException.class)
    public void testGetUserJSONError() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenReturn(null);
         User.get(restClient, "username");

    }

    @Test(expected = JiraException.class)
    public void testGetUserRestError() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenThrow(Exception.class);
       User.get(restClient, "username");
    }

    @Test
    public void testGetUser() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenReturn(getTestJSON());
        final User user = User.get(restClient, "username");

        assertEquals(user.getName(), USERNAME_VALUE);
        assertEquals(user.getDisplayName(), DISPLAYNAME_VALUE);
        assertEquals(user.getEmail(), EMAIL_VALUE);
        assertEquals(user.getId(), USERID_VALUE);

        Map<String, String> avatars = user.getAvatarUrls();

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));

        assertTrue(user.isActive());
    }
}
