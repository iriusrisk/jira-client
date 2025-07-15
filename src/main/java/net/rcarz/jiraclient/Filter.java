package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;

/**
 * Represens a Jira filter.
 */
public class Filter extends Resource {

	private String name;
	private String jql;
	private boolean favourite;

	public Filter(RestClient restclient, JsonNode json) {
		super(restclient);

		if (json != null)
			deserialise(json);
	}

	private void deserialise(JsonNode json) {
		id = Field.getString(json.get("id"));
		self = Field.getString(json.get("self"));
		name = Field.getString(json.get("name"));
		jql = Field.getString(json.get("jql"));
		favourite = Field.getBoolean(json.get("favourite"));
	}

	public boolean isFavourite() {
		return favourite;
	}

	public String getJql() {
		return jql;
	}

	public String getName() {
		return name;
	}

	public static Filter get(final RestClient restclient, final String id) throws JiraException {
		JsonNode result = null;

		try {
			URI uri = restclient.buildURI(getBaseUri() + "filter/" + id);
			result = restclient.get(uri);
		} catch (Exception ex) {
			throw new JiraException("Failed to retrieve filter with id " + id, ex);
		}

		if (result == null || !result.isObject()) {
			throw new JiraException("JSON payload is malformed");
		}

		return new Filter(restclient, result);
	}

	@Override
	public String toString() {
		return "Filter{" +
				"favourite=" + favourite +
				", name='" + name + '\'' +
				", jql='" + jql + '\'' +
				'}';
	}


}
