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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Random;


/**
 * A simple REST client that speaks JSON.
 */
public class RestClient {

    private HttpClient httpClient = null;
    private ICredentials creds = null;
    private URI uri = null;
    private HttpContext httpContext = null;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Replacing net.sf.json with Jackson
    private boolean enableRetryOnRateLimit = false;

    private static final int MAX_RETRIES = 10;
    private static final Random RANDOM = new Random();

    /**
     * Creates a REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param uri Base URI of the remote REST service
     */
    public RestClient(HttpClient httpclient, URI uri) {
        this(httpclient, null, uri);
    }

    public RestClient(HttpClient httpclient, ICredentials creds, URI uri) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
        this.httpContext = new BasicHttpContext();
    }

    /**
     * Creates an authenticated REST client instance with a URI.
     *
     * @param httpclient Underlying HTTP client to use
     * @param creds Credentials to send with each request
     * @param uri Base URI of the remote REST service
     */
    public RestClient(HttpClient httpclient, ICredentials creds, URI uri, HttpContext httpContext) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
        this.httpContext = httpContext;
    }

    public RestClient(HttpClient httpclient, ICredentials creds, URI uri, HttpContext httpContext, boolean enableRetryOnRateLimit) {
        this.httpClient = httpclient;
        this.creds = creds;
        this.uri = uri;
        this.httpContext = httpContext;
        this.enableRetryOnRateLimit = enableRetryOnRateLimit;
    }

    /**
     * Build a URI from a path.
     *
     * @param path Path to append to the base URI
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path) throws URISyntaxException {
        return buildURI(path, null);
    }

    /**
     * Build a URI from a path and query parmeters.
     *
     * @param path Path to append to the base URI
     * @param params Map of key value pairs
     *
     * @return the full URI
     *
     * @throws URISyntaxException when the path is invalid
     */
    public URI buildURI(String path, Map<String, String> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder(uri);

        if (ub.getPath() != null) {
            path = ub.getPath() + path;
        }

        ub.setPath(path);

        if (params != null) {
            for (Map.Entry<String, String> ent : params.entrySet())
                ub.addParameter(ent.getKey(), ent.getValue());
        }

        return ub.build();
    }

    private JsonNode request(HttpRequestBase req) throws RestException, IOException {
        return httpContext == null ? request(req, new BasicHttpContext()) : request(req, httpContext);
    }

    private JsonNode request(HttpRequestBase req, HttpContext ctx) throws RestException, IOException {
        req.addHeader("Accept", "application/json");

        if (creds != null) {
            creds.authenticate(req);
        }

        HttpResponse response = null;
        int attempt = 0;

        while (attempt == 0 || (enableRetryOnRateLimit && attempt < MAX_RETRIES)) {
            response = httpClient.execute(req, ctx);
            int status = response.getStatusLine().getStatusCode();
            if (status == 429) {
                long waitTime = calculateWaitTimeMillis(response, attempt);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return handleResponse(response);
            }
            attempt++;
        }

        return handleResponse(response);
    }

    private long calculateWaitTimeMillis(HttpResponse response, int attempt) {
        long jitter = RANDOM.nextInt(500); // A random extra time (up to half second) to distribute better the requests

        // If we have value in Retry-After header we use exactly this value (is the direct indication from Jira) plus the jitter
        long retryAfter = getNumericValueForHeader(response, "Retry-After");
        if (retryAfter > 0) {
            return (retryAfter * 1000L) + jitter;
        }

        // If we don't have Retry-After, we will wait until the next interval, when new requests will be available, plus the jitter
        long intervalSeconds = getNumericValueForHeader(response, "X-RateLimit-Interval-Seconds");
        if (intervalSeconds > 0) {
            return (intervalSeconds * 1000L) + jitter;
        }

        // Otherwise, we wait as many seconds as retries plus the jitter
        return (attempt * 1000L) + jitter;
    }

    private Long getNumericValueForHeader(HttpResponse response, String name) {
        if (name == null || name.isEmpty() ) {
            return 0L;
        }
        Header header = response.getFirstHeader(name);
        if (header == null) {
            return 0L;
        }
        try {
            return Long.parseLong(header.getValue());
        } catch(NumberFormatException ignored) {
            return 0L;
        }
    }

    private JsonNode handleResponse(HttpResponse response) throws RestException, IOException {
        HttpEntity entity = response.getEntity();
        StringBuilder result = new StringBuilder();

        if (entity != null) {
            String encoding = null;
            if (entity.getContentEncoding() != null) {
                encoding = entity.getContentEncoding().getValue();
            }

            if (encoding == null) {
                Header contentTypeHeader = response.getFirstHeader("Content-Type");
                HeaderElement[] contentTypeElements = contentTypeHeader.getElements();
                for (HeaderElement he : contentTypeElements) {
                    NameValuePair nvp = he.getParameterByName("charset");
                    if (nvp != null) {
                        encoding = nvp.getValue();
                    }
                }
            }

            InputStreamReader isr = encoding != null ?
                    new InputStreamReader(entity.getContent(), encoding) :
                    new InputStreamReader(entity.getContent());
            BufferedReader br = new BufferedReader(isr);
            String line = "";

            while ((line = br.readLine()) != null)
                result.append(line);
        }

        StatusLine sl = response.getStatusLine();

        if (sl.getStatusCode() >= 300)
            throw new RestException(sl.getReasonPhrase(), sl.getStatusCode(), result.toString(), response.getAllHeaders());

        return result.length() > 0 ? objectMapper.readTree(result.toString()) : null;
    }

    private JsonNode request(HttpEntityEnclosingRequestBase req, String payload)
        throws RestException, IOException {

        if (payload != null) {
            StringEntity ent = new StringEntity(payload, "UTF-8");
            ent.setContentType("application/json");
            req.addHeader("Content-Type", "application/json");
            req.setEntity(ent);
        }

        return request(req);
    }

    private JsonNode request(HttpEntityEnclosingRequestBase req, File file)
        throws RestException, IOException {
        if (file != null) {
            File fileUpload = file;
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntity ent = new MultipartEntity();
            ent.addPart("file", new FileBody(fileUpload));
            req.setEntity(ent);
        }
        return request(req);
    }

    private JsonNode request(HttpEntityEnclosingRequestBase req, Issue.NewAttachment... attachments)
        throws RestException, IOException {
        if (attachments != null) {
            req.setHeader("X-Atlassian-Token", "nocheck");
            MultipartEntity ent = new MultipartEntity();
            for (Issue.NewAttachment attachment : attachments) {
                String filename = attachment.getFilename();
                Object content = attachment.getContent();
                if (content instanceof byte[]) {
                    ent.addPart("file", new ByteArrayBody((byte[]) content, filename));
                } else if (content instanceof InputStream) {
                    ent.addPart("file", new InputStreamBody((InputStream) content, filename));
                } else if (content instanceof File) {
                    ent.addPart("file", new FileBody((File) content, filename));
                } else if (content == null) {
                    throw new IllegalArgumentException("Missing content for the file " + filename);
                } else {
                    throw new IllegalArgumentException(
                        "Expected file type byte[], java.io.InputStream or java.io.File but provided " +
                                content.getClass().getName() + " for the file " + filename);
                }
            }
            req.setEntity(ent);
        }
        return request(req);
    }

    // Updated method to handle Jackson ObjectNode instead of JSON from net.sf.json
    private JsonNode request(HttpEntityEnclosingRequestBase req, ObjectNode payload)
            throws RestException, IOException {
        return request(req, payload != null ? payload.toString() : null);
    }

    /**
     * Executes an HTTP DELETE with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode delete(URI uri) throws RestException, IOException {
        return request(new HttpDelete(uri));
    }

    /**
     * Executes an HTTP DELETE with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode delete(String path) throws RestException, IOException, URISyntaxException {
        return delete(buildURI(path));
    }

    /**
     * Executes an HTTP GET with the given URI.
     *
     * @param uri Full URI of the remote endpoint
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode get(URI uri) throws RestException, IOException {
        return request(new HttpGet(uri));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param params Map of key value pairs
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode get(String path, Map<String, String> params) throws RestException, IOException, URISyntaxException {
        return get(buildURI(path, params));
    }

    /**
     * Executes an HTTP GET with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode get(String path) throws RestException, IOException, URISyntaxException {
        return get(path, null);
    }


    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode post(URI uri, ObjectNode payload) throws RestException, IOException {
        return request(new HttpPost(uri), payload);
    }

    /**
     * Executes an HTTP POST with the given URI and payload.
     *
     * At least one JIRA REST endpoint expects malformed JSON. The payload
     * argument is quoted and sent to the server with the application/json
     * Content-Type header. You should not use this function when proper JSON
     * is expected.
     *
     *
     * @param uri Full URI of the remote endpoint
     * @param payload Raw string to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode post(URI uri, String payload) throws RestException, IOException {
    	String quoted = null;
    	if(payload != null && !payload.equals(new ObjectNode(null))){
    		quoted = String.format("\"%s\"", payload);
    	}
        return request(new HttpPost(uri), quoted);
    }

    /**
     * Executes an HTTP POST with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode post(String path, ObjectNode payload)
        throws RestException, IOException, URISyntaxException {

        return post(buildURI(path), payload);
    }
    
    /**
     * Executes an HTTP POST with the given path.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode post(String path)
        throws RestException, IOException, URISyntaxException {
    	
        return post(buildURI(path), new ObjectNode(null));
    }
    
    /**
     * Executes an HTTP POST with the given path and file payload.
     * 
     * @param path Full URI of the remote endpoint
     * @param file java.io.File
     * 
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws RestException 
     */
    public JsonNode post(String path, File file) throws RestException, IOException, URISyntaxException{
        return request(new HttpPost(buildURI(path)), file);
    }

    /**
     * Executes an HTTP POST with the given path and file payloads.
     *
     * @param path    Full URI of the remote endpoint
     * @param attachments   the name of the attachment
     *
     * @throws URISyntaxException
     * @throws IOException
     * @throws RestException
     */
    public JsonNode post(String path, Issue.NewAttachment... attachments)
        throws RestException, IOException, URISyntaxException
    {
        return request(new HttpPost(buildURI(path)), attachments);
    }

    /**
     * Executes an HTTP PUT with the given URI and payload.
     *
     * @param uri Full URI of the remote endpoint
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     */
    public JsonNode put(URI uri, ObjectNode payload) throws RestException, IOException {
        return request(new HttpPut(uri), payload);
    }

    /**
     * Executes an HTTP PUT with the given path and payload.
     *
     * @param path Path to be appended to the URI supplied in the construtor
     * @param payload JSON-encoded data to send to the remote service
     *
     * @return JSON-encoded result or null when there's no content returned
     *
     * @throws RestException when an HTTP-level error occurs
     * @throws IOException when an error reading the response occurs
     * @throws URISyntaxException when an error occurred appending the path to the URI
     */
    public JsonNode put(String path, ObjectNode payload)
        throws RestException, IOException, URISyntaxException {

        return put(buildURI(path), payload);
    }
    
    /**
     * Exposes the http client.
     *
     * @return the httpClient property
     */
    public HttpClient getHttpClient(){
        return this.httpClient;
    }
}