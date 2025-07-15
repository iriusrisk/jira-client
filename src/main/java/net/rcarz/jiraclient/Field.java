/**
 * jira-client - a simple JIRA REST client Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility functions for translating between JSON and fields.
 */
public final class Field {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Field metadata structure.
     */
    public static final class Meta {
        public boolean required;
        public String type;
        public String items;
        public String name;
        public String system;
        public String custom;
        public int customId;
    }

    /**
     * Field update operation.
     */
    public static final class Operation {
        public String name;
        public Object value;

        /**
         * Initialises a new update operation.
         *
         * @param name Operation name
         * @param value Field value
         */
        public Operation(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Allowed value types.
     */
    public enum ValueType {
        KEY("key"), NAME("name"), ID_NUMBER("id"), VALUE("value");
        private String typeName;

        private ValueType(String typeName) {
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }
    }

    ;

    /**
     * Value and value type pair.
     */
    public static final class ValueTuple {
        public final String type;
        public final Object value;

        /**
         * Initialises the value tuple.
         *
         * @param type
         * @param value
         */
        public ValueTuple(String type, Object value) {
            this.type = type;
            this.value = (value != null ? value : NullNode.getInstance());
        }

        /**
         * Initialises the value tuple.
         *
         * @param type
         * @param value
         */
        public ValueTuple(ValueType type, Object value) {
            this(type.toString(), value);
        }
    }

    public static final String ASSIGNEE = "assignee";
    public static final String ATTACHMENT = "attachment";
    public static final String CHANGE_LOG = "changelog";
    public static final String CHANGE_LOG_ENTRIES = "histories";
    public static final String CHANGE_LOG_ITEMS = "items";
    public static final String COMMENT = "comment";
    public static final String COMPONENTS = "components";
    public static final String DESCRIPTION = "description";
    public static final String DUE_DATE = "duedate";
    public static final String FIX_VERSIONS = "fixVersions";
    public static final String ISSUE_LINKS = "issuelinks";
    public static final String ISSUE_TYPE = "issuetype";
    public static final String LABELS = "labels";
    public static final String PARENT = "parent";
    public static final String PRIORITY = "priority";
    public static final String PROJECT = "project";
    public static final String REPORTER = "reporter";
    public static final String RESOLUTION = "resolution";
    public static final String RESOLUTION_DATE = "resolutiondate";
    public static final String STATUS = "status";
    public static final String SUBTASKS = "subtasks";
    public static final String SUMMARY = "summary";
    public static final String TIME_TRACKING = "timetracking";
    public static final String VERSIONS = "versions";
    public static final String VOTES = "votes";
    public static final String WATCHES = "watches";
    public static final String WORKLOG = "worklog";
    public static final String TIME_ESTIMATE = "timeestimate";
    public static final String TIME_SPENT = "timespent";
    public static final String CREATED_DATE = "created";
    public static final String UPDATED_DATE = "updated";
    public static final String TRANSITION_TO_STATUS = "to";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private Field() {
    }

    /**
     * Gets a boolean value from the given object.
     *
     * @param b a Boolean instance
     *
     * @return a boolean primitive or false if b isn't a Boolean instance
     */
    public static boolean getBoolean(Object b) {
        boolean result = false;

        if (b instanceof Boolean)
            result = ((Boolean) b).booleanValue();

        return result;
    }

    /**
     * Gets a list of comments from the given object.
     *
     * @param c a JsonNode instance
     * @param restclient REST client instance
     * @param issueKey key of the parent issue
     *
     * @return a list of comments found in c
     */
    public static List<Comment> getComments(JsonNode c, RestClient restclient,
                                            String issueKey) {
        List<Comment> results = new ArrayList<>();

        if (c != null && !c.isNull()) {
            results = getResourceArray(
                    Comment.class,
                    c.get("comments"),
                    restclient,
                    issueKey
            );
        }

        return results;
    }

    /**
     * Gets a list of work logs from the given object.
     *
     * @param c a JsonNode instance
     * @param restclient REST client instance
     *
     * @return a list of work logs found in c
     */
    public static List<WorkLog> getWorkLogs(JsonNode c, RestClient restclient) {
        List<WorkLog> results = new ArrayList<>();

        if (c != null && !c.isNull()) {
            results = getResourceArray(WorkLog.class, c.get("worklogs"), restclient);
        }

        return results;
    }

    /**
     * Gets a list of remote links from the given object.
     *
     * @param c a JsonNode instance
     * @param restclient REST client instance
     *
     * @return a list of remote links found in c
     */
    public static List<RemoteLink> getRemoteLinks(JsonNode c, RestClient restclient) {
        List<RemoteLink> results = new ArrayList<>();

        if (c != null && c.isArray()) {
            results = getResourceArray(RemoteLink.class, c, restclient);
        }

        return results;
    }

    /**
     * Gets a date from the given object.
     *
     * @param d a string representation of a date
     *
     * @return a Date instance or null if d isn't a string
     */
    public static Date getDate(JsonNode d) {
        Date result = null;

        if (d != null && d.isTextual()) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            result = df.parse(d.asText(), new ParsePosition(0));
        }

        return result;
    }

    /**
     * Gets a date with a time from the given object.
     *
     * @param d a string representation of a date
     *
     * @return a Date instance or null if d isn't a string
     */
    public static Date getDateTime(JsonNode d) {
        Date result = null;

        if (d != null && d.isTextual()) {
            SimpleDateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
            result = df.parse(d.asText(), new ParsePosition(0));
        }

        return result;
    }

    /**
     * Gets an floating-point number from the given object.
     *
     * @param i an Double instance
     *
     * @return an floating-point number or null if i isn't a Double instance
     */
    public static Double getDouble(JsonNode i) {
        Double result = null;

        if (i != null && i.isDouble()) {
            result = i.asDouble();
        }

        return result;
    }

    /**
     * Gets an integer from the given object.
     *
     * @param i an Integer instance
     *
     * @return an integer primitive or 0 if i isn't an Integer instance
     */
    public static int getInteger(JsonNode i) {
        int result = 0;

        if (i != null && i.isInt()) {
            result = i.asInt();
        }

        return result;
    }

    /**
     +     * Gets a long from the given object.
     +     *
     +     * @param i a Long or an Integer instance
     +     *
     +     * @return a long primitive or 0 if i isn't a Long or an Integer instance
     +     */
    public static long getLong(JsonNode i) {
        long result = 0;
        if (i != null && i.isLong()) {
            result = i.asLong();
        } else if (i != null && i.isInt()) {
            result = i.asInt();
        }
        return result;
    }

    /**
     * Gets a generic map from the given object.
     *
     * @param keytype Map key data type
     * @param valtype Map value data type
     * @param m a JsonNode instance
     *
     * @return a Map instance with all entries found in m
     */
    public static <TK, TV> Map<TK, TV> getMap(
            Class<TK> keytype, Class<TV> valtype, JsonNode m) {

        Map<TK, TV> result = new HashMap<>();

        if (m != null && m.isObject() && !m.isNull()) {
            result = mapper.convertValue(m, new TypeReference<Map<TK, TV>>() {});
        }

        return result;
    }

    /**
     * Gets a JIRA resource from the given object.
     *
     * @param type Resource data type
     * @param r a JsonNode instance
     * @param restclient REST client instance
     *
     * @return a Resource instance or null if r isn't a JSONObject instance
     */
    public static <T extends Resource> T getResource(
            Class<T> type, JsonNode r, RestClient restclient) {

        return getResource(type, r, restclient, null);
    }

    /**
     * Gets a JIRA resource from the given object.
     *
     * @param type Resource data type
     * @param r a JsonNode instance
     * @param restclient REST client instance
     * @param parentId id/key of the parent resource
     *
     * @return a Resource instance or null if r isn't a JsonNode instance
     */
    public static <T extends Resource> T getResource(
            Class<T> type, JsonNode r, RestClient restclient, String parentId) {

        T result = null;

        if (r != null && r.isObject() && !r.isNull()) {
            if (type == Attachment.class) {
                result = (T) new Attachment(restclient, r);
            } else if (type == ChangeLog.class) {
                result = (T) new ChangeLog(restclient, r);
            } else if (type == ChangeLogEntry.class) {
                result = (T) new ChangeLogEntry(restclient, r);
            } else if (type == ChangeLogItem.class) {
                result = (T) new ChangeLogItem(restclient, r);
            } else if (type == Comment.class) {
                result = (T) new Comment(restclient, r, parentId);
            } else if (type == Component.class) {
                result = (T) new Component(restclient, r);
            } else if (type == CustomFieldOption.class) {
                result = (T) new CustomFieldOption(restclient, r);
            } else if (type == Issue.class) {
                // TODO: result = (T) new Issue(restclient, r);
            } else if (type == IssueLink.class) {
                result = (T) new IssueLink(restclient, r);
            } else if (type == IssueType.class) {
                result = (T) new IssueType(restclient, r);
            } else if (type == LinkType.class) {
                result = (T) new LinkType(restclient, r);
            } else if (type == Priority.class) {
                result = (T) new Priority(restclient, r);
            } else if (type == Project.class) {
                result = (T) new Project(restclient, r);
            } else if (type == ProjectCategory.class) {
                result = (T) new ProjectCategory(restclient, r);
            } else if (type == RemoteLink.class) {
                result = (T) new RemoteLink(restclient, r);
            } else if (type == Resolution.class) {
                result = (T) new Resolution(restclient, r);
            } else if (type == Status.class) {
                result = (T) new Status(restclient, r);
            } else if (type == Transition.class) {
                result = (T) new Transition(restclient, r);
            } else if (type == User.class) {
                result = (T) new User(restclient, r);
            } else if (type == Version.class) {
                result = (T) new Version(restclient, r);
            } else if (type == Votes.class) {
                result = (T) new Votes(restclient, r);
            } else if (type == Watches.class) {
                result = (T) new Watches(restclient, r);
            } else if (type == WorkLog.class) {
                result = (T) new WorkLog(restclient, r);
            }
        }

        return result;
    }

    /**
     * Gets a string from the given object.
     *
     * @param s a String instance
     *
     * @return a String or null if s isn't a String instance
     */
    public static String getString(JsonNode s) {
        String result = null;

        if (s != null && s.isTextual()) {
            result = s.asText();
        }

        return result;
    }

    /**
     * Gets a list of strings from the given object.
     *
     * @param sa a JSONArray instance
     *
     * @return a list of strings found in sa
     */
    public static List<String> getStringArray(JsonNode sa) {
        List<String> results = new ArrayList<>();

        if (sa instanceof ArrayNode) {
            for (JsonNode s : sa) {
                if (s.isTextual()) {
                    results.add(s.asText());
                }
            }
        }

        return results;
    }

    /**
     * Gets a list of integers from the given object.
     *
     * @param sa a JSONArray instance
     *
     * @return a list of integers found in sa
     */
    public static List<Integer> getIntegerArray(JsonNode sa) {
        List<Integer> results = new ArrayList<>();

        if (sa instanceof ArrayNode) {
            for (JsonNode s : sa) {
                if (s.isInt()) {
                    results.add(s.asInt());
                }
            }
        }

        return results;
    }

    /**
     * Gets a list of JIRA resources from the given object.
     *
     * @param type Resource data type
     * @param ra a JSONArray instance
     * @param restclient REST client instance
     *
     * @return a list of Resources found in ra
     */
    public static <T extends Resource> List<T> getResourceArray(
            Class<T> type, JsonNode ra, RestClient restclient) {

        return getResourceArray(type, ra, restclient, null);
    }

    /**
     * Gets a list of JIRA resources from the given object.
     *
     * @param type Resource data type
     * @param ra a JSONArray instance
     * @param restclient REST client instance
     * @param parentId id/key of the parent resource
     *
     * @return a list of Resources found in ra
     */
    public static <T extends Resource> List<T> getResourceArray(
            Class<T> type, JsonNode ra, RestClient restclient, String parentId) {

        List<T> results = new ArrayList<T>();

        if (ra != null && ra.isArray()) {
            for (JsonNode v : ra) {
                T item = null;

                if (parentId != null) {
                    item = getResource(type, v, restclient, parentId);
                } else {
                    item = getResource(type, v, restclient);
                }

                if (item != null)
                    results.add(item);
            }
        }

        return results;
    }

    /**
     * Gets a time tracking object from the given object.
     *
     * @param tt a JSONObject instance
     *
     * @return a TimeTracking instance or null if tt isn't a JSONObject instance
     */
    public static TimeTracking getTimeTracking(JsonNode tt) {
        TimeTracking result = null;

        if (tt != null && tt.isObject() && !tt.isNull()) {
            result = new TimeTracking(tt);
        }

        return result;
    }

    /**
     * Extracts field metadata from an editmeta JSON object.
     *
     * @param name Field name
     * @param editmeta Edit metadata JSON object
     *
     * @return a Meta instance with field metadata
     *
     * @throws JiraException when the field is missing or metadata is bad
     */
    public static Meta getFieldMetadata(String name, JsonNode editmeta)
            throws JiraException {

        if (editmeta == null || editmeta.isNull() || !editmeta.has(name)) {
            throw new JiraException("Field '" + name + "' does not exist or read-only");
        }

        JsonNode f = editmeta.get(name);
        Meta m = new Meta();

        m.required = Field.getBoolean(f.get("required"));
        m.name = Field.getString(f.get("name"));

        if (!f.has("schema")) {
            throw new JiraException("Field '" + name + "' is missing schema metadata");
        }

        JsonNode schema = f.get("schema");

        m.type = Field.getString(schema.get("type"));
        m.items = Field.getString(schema.get("items"));
        m.system = Field.getString(schema.get("system"));
        m.custom = Field.getString(schema.get("custom"));
        m.customId = Field.getInteger(schema.get("customId"));

        return m;
    }

    /**
     * Converts the given value to a date.
     *
     * @param value New field value
     *
     * @return a Date instance or null
     */
    public static Date toDate(Object value) {
        if (value instanceof Date || value == null)
            return (Date) value;

        String dateStr = value.toString();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        if (dateStr.length() > DATE_FORMAT.length()) {
            df = new SimpleDateFormat(DATETIME_FORMAT);
        }
        return df.parse(dateStr, new ParsePosition(0));
    }

    /**
     * Converts an iterable type to a JSON array.
     *
     * @param iter Iterable type containing field values
     * @param type Name of the item type
     * @param custom Name of the custom type
     *
     * @return a JSON-encoded array of items
     */
    public static ArrayNode toArray(Iterable iter, String type, String custom) throws JiraException {
        ArrayNode results = JsonNodeFactory.instance.arrayNode();

        if (type == null)
            throw new JiraException("Array field metadata is missing item type");

        for (Object val : iter) {
            Operation oper = null;
            Object realValue = null;
            JsonNode realResult = null;

            if (val instanceof Operation) {
                oper = (Operation) val;
                realValue = oper.value;
            } else
                realValue = val;

            if (type.equals("component") || type.equals("group") ||
                    type.equals("user") || type.equals("version")) {

                ObjectNode itemMap = JsonNodeFactory.instance.objectNode();

                if (realValue instanceof ValueTuple) {
                    ValueTuple tuple = (ValueTuple) realValue;
                    itemMap.put(tuple.type, tuple.value.toString());
                } else
                    itemMap.put(ValueType.NAME.toString(), realValue.toString());

                realResult = itemMap;
            } else if (type.equals("option") ||
                    (
                            type.equals("string") && custom != null
                                    && (custom.equals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes") ||
                                    custom.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect")))) {

                realResult = JsonNodeFactory.instance.objectNode();
                ((ObjectNode) realResult).put(ValueType.VALUE.toString(), realValue.toString());
            } else if (type.equals("string"))
                realResult = JsonNodeFactory.instance.textNode(realValue.toString());

            if (oper != null) {
                ObjectNode operMap = JsonNodeFactory.instance.objectNode();
                operMap.put(oper.name, realResult);
                results.add(operMap);
            } else
                results.add(realResult);
        }

        return results;
    }

    /**
     * Converts the given value to a JSON object.
     *
     * @param name Field name
     * @param value New field value
     * @param editmeta Edit metadata JSON object
     *
     * @return a JSON-encoded field value
     *
     * @throws JiraException when a value is bad or field has invalid metadata
     * @throws UnsupportedOperationException when a field type isn't supported
     */
    public static JsonNode toJson(String name, Object value, JsonNode editmeta)
            throws JiraException, UnsupportedOperationException {
        return toJson(name, value, editmeta, null);
    }

    /**
     * Converts the given value to a JSON object.
     *
     * @param name Field name
     * @param value New field value
     * @param editmeta Edit metadata JSON object
     * @param serverType Server type
     *
     * @return a JSON-encoded field value
     *
     * @throws JiraException when a value is bad or field has invalid metadata
     * @throws UnsupportedOperationException when a field type isn't supported
     */
    public static JsonNode toJson(String name, Object value, JsonNode editmeta, String serverType)
            throws JiraException, UnsupportedOperationException {

        Meta m = getFieldMetadata(name, editmeta);
        if (m.type == null)
            throw new JiraException("Field '" + name + "' is missing metadata type");

        if (m.type.equals("array")) {
            if (value == null)
                value = new ArrayList();
            else if (!(value instanceof Iterable))
                throw new JiraException("Field '" + name + "' expects an Iterable value");

            return toArray((Iterable) value, m.items, m.custom);
        } else if (m.type.equals("date")) {
            if (value == null) {
                return NullNode.getInstance();
            }

            Date d = toDate(value);
            if (d == null)
                throw new JiraException("Field '" + name + "' expects a date value or format is invalid");

            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            return JsonNodeFactory.instance.textNode(df.format(d));
        } else if (m.type.equals("datetime")) {
            if (value == null) {
                return NullNode.getInstance();
            }
            else if (!(value instanceof Timestamp))
                throw new JiraException("Field '" + name + "' expects a Timestamp value");

            SimpleDateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
            return JsonNodeFactory.instance.textNode(df.format(value));
        } else if (m.type.equals("issuetype") ||
                m.type.equals("user") || m.type.equals("resolution")) {
            ObjectNode json = JsonNodeFactory.instance.objectNode();

            if (value == null) {
                return NullNode.getInstance();
            }
            else if (value instanceof ValueTuple) {
                ValueTuple tuple = (ValueTuple) value;
                json.put(tuple.type, tuple.value.toString());
            } else if (isReporterOrIssueType(m, serverType))
                json.put(ValueType.ID_NUMBER.toString(), value.toString());
            else
                json.put(ValueType.NAME.toString(), value.toString());

            return json;
        } else if (m.type.equals("priority")) {
            ObjectNode json = JsonNodeFactory.instance.objectNode();

            if (value == null) {
                return NullNode.getInstance();
            }
            else if (value instanceof ValueTuple) {
                ValueTuple tuple = (ValueTuple) value;
                json.put(tuple.type, tuple.value.toString());
            } else {
                if(value instanceof Priority){
                    value = ((Priority) value).getId();
                }
                json.put(ValueType.ID_NUMBER.toString(), value.toString());
            }

            return json;
        } else if (m.type.equals("project") || m.type.equals("issuelink")) {
            ObjectNode json = JsonNodeFactory.instance.objectNode();

            if (value == null) {
                return NullNode.getInstance();
            }
            else if (value instanceof ValueTuple) {
                ValueTuple tuple = (ValueTuple) value;
                json.put(tuple.type, tuple.value.toString());
            } else
                json.put(ValueType.KEY.toString(), value.toString());

            return json;
        } else if (m.type.equals("string") || m.type.equals("securitylevel") || m.type.equals("any")) {
            if (value == null)
                return JsonNodeFactory.instance.textNode("");
            else if (value instanceof List)
                return toJsonMap((List) value);
            else if (value instanceof ValueTuple) {
                ObjectNode json = JsonNodeFactory.instance.objectNode();
                ValueTuple tuple = (ValueTuple) value;
                json.put(tuple.type, tuple.value.toString());
                return json;
            }

            return JsonNodeFactory.instance.textNode(value.toString());
        } else if (m.type.equals("timetracking")) {
            if (value == null) {
                return NullNode.getInstance();
            }
            else if (value instanceof TimeTracking) {
                return mapper.convertValue(value, ObjectNode.class);
            }
        } else if (m.type.equals("option")) {
            ObjectNode json = JsonNodeFactory.instance.objectNode();
            ValueTuple tuple = new ValueTuple("value", value.toString());
            json.put(tuple.type, tuple.value.toString());
            return json;
        } else if (m.type.equals("number")) {
            if (value instanceof Integer) {
                return JsonNodeFactory.instance.numberNode((Integer) value);
            }
            if (value instanceof Long) {
                return JsonNodeFactory.instance.numberNode((Long) value);
            }
            if (value instanceof Double) {
                return JsonNodeFactory.instance.numberNode((Double) value);
            }
            if (value instanceof Float) {
                return JsonNodeFactory.instance.numberNode((Float) value);
            }
            throw new JiraException("Field '" + name + "' expects a Numeric value");
        }

        throw new UnsupportedOperationException(m.type + " is not a supported field type");
    }
    private static boolean isReporterOrIssueType(Meta m, String serverType) {
        return (m.name.equalsIgnoreCase("reporter") && ServerInfo.CLOUD.equalsIgnoreCase(serverType)) || m.type.equalsIgnoreCase("issuetype");
    }

    /**
     * Converts the given map to a JSON object.
     *
     * @param list List of values to be converted
     *
     * @return a JSON-encoded map
     */
    public static JsonNode toJsonMap(List list) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();

        for (Object item : list) {
            if (item instanceof ValueTuple) {
                ValueTuple vt = (ValueTuple) item;
                json.put(vt.type, vt.value.toString());
            } else
                json.put(ValueType.VALUE.toString(), item.toString());
        }

        return json;
    }

    /**
     * Create a value tuple with value type of key.
     *
     * @param key The key value
     *
     * @return a value tuple
     */
    public static ValueTuple valueByKey(String key) {
        return new ValueTuple(ValueType.KEY, key);
    }

    /**
     * Create a value tuple with value type of name.
     *
     * @param name The name value
     *
     * @return a value tuple
     */
    public static ValueTuple valueByName(String name) {
        return new ValueTuple(ValueType.NAME, name);
    }

    /**
     * Create a value tuple with value type of ID number.
     *
     * @param id The ID number value
     *
     * @return a value tuple
     */
    public static ValueTuple valueById(String id) {
        return new ValueTuple(ValueType.ID_NUMBER, id);
    }
}

