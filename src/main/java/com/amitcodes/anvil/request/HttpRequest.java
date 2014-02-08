package com.amitcodes.anvil.request;

import com.amitcodes.anvil.HttpMethodEnum;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpRequest implements Request {

    private Logger logger = Logger.getLogger(HttpRequest.class.getCanonicalName());
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private URL target;
    private HttpMethodEnum method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> params;
    private String payloadBody;
    private File payloadFile;
    // TODO: Proxy support

    public HttpRequest(Logger logger, HttpMethodEnum method,
                       Map<String, List<String>> headers,
                       Map<String, List<String>> params, String payloadBody, File payloadFile) {

        this.method = method;
        this.headers = headers;
        this.params = params;
        this.payloadBody = payloadBody;
        this.payloadFile = payloadFile;

        if(this.headers == null) {
            this.headers = new HashMap<String, List<String>>();
        }

        if(this.params == null){
            this.headers = new HashMap<String, List<String>>();
        }
    }

    public HttpRequest(HttpMethodEnum method) {
        this.method = method;
        this.headers = new HashMap<String, List<String>>();
        this.headers = new HashMap<String, List<String>>();
    }

    public void addHeader(String headerName, String headerValue) {

        if(headerName == null || headerValue == null ) {
            return;
        }

        headerName = headerName.trim();
        headerValue = headerValue.trim();

        if(headerName.isEmpty() || headerValue.isEmpty() ) {
            return;
        }

        if(headers.get(headerName) == null) {
            headers.put(headerName, new ArrayList<String>());
        }

        headers.get(headerName).add(headerValue);
    }

    public void addParam(String paramName, String paramValue) {

        if(paramName == null || paramValue == null ) {
            return;
        }

        paramName = paramName.trim();
        paramValue = paramValue.trim();

        if(paramName.isEmpty() || paramValue.isEmpty() ) {
            return;
        }

        if(params.get(paramName) == null) {
            params.put(paramName, new ArrayList<String>());
        }

        params.get(paramName).add(paramValue);
    }

    public HttpMethodEnum getMethod() {
        return method;
    }

    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, List<String>> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public String getPayloadBody() {
        return payloadBody;
    }

    public void setPayloadBody(String payloadBody) {
        this.payloadBody = payloadBody;
    }

    public File getPayloadFile() {
        return payloadFile;
    }

    public void setPayloadFile(File payloadFile) {
        this.payloadFile = payloadFile;
    }

    @Override
    public void make() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        HttpURLConnection httpConn = createConnection();
        writeRequestHeaders(httpConn);
        if(!method.isReadOnly()) {
            writeToUrl(httpConn);
        }
        readResponse(httpConn);
    }

    private HttpURLConnection createConnection(){

        // append query parameters to URL if HTTP method
        // is GET / DELETE
        if(method.isReadOnly()) {
            appendQueryParamsToUrl();
        }

        HttpURLConnection httpConn = (HttpURLConnection)target.openConnection();
        httpConn.setDoInput(true);
        if(method.isReadOnly()) {
            httpConn.setDoOutput(true);
        }
        return httpConn;
    }

    private void appendQueryParamsToUrl(){

            String urlAsString = target.toExternalForm();

            if(urlAsString.contains("?")) { // has query string
                if(urlAsString.endsWith("?")) {
                    urlAsString = urlAsString + toParametersString();
                }
                else if(!urlAsString.endsWith("&")) {
                    urlAsString = urlAsString + "&";
                }
                else {
                    urlAsString = urlAsString + toParametersString();
                }
            } else {
                urlAsString = urlAsString + "?" + toParametersString();
            }

            target = new URL(urlAsString);
    }

    private void writeRequestHeaders(HttpURLConnection httpConn){
        for(Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            for(String headerValue : headerEntry.getValue()){
                httpConn.setRequestProperty(headerEntry.getKey(), headerValue);
                logger.log(Level.FINEST, "Added header: %s=%s",
                           new Object[]{headerEntry.getKey(), headerValue});
            }
        }
    }

    private String toParametersString() {
        // Create the parameter string
        StringBuilder buff = new StringBuilder();
        for(Map.Entry<String, List<String>> paramEntry : params.entrySet()) {
            for(String paramValue : paramEntry.getValue()){

                try {
                    buff.append( URLEncoder.encode(paramEntry.getKey(), "UTF-8") )
                            .append('=')
                            .append( URLEncoder.encode(paramValue, "UTF-8") )
                            .append( '&' );
                } catch (UnsupportedEncodingException e) {
                    // simply log as UTF-8 is always supported
                    logger.log(Level.INFO, "URL encoding failed", e);
                }
            }
        }

        return buff.toString();
    }

    private void writeToUrl(HttpURLConnection httpConn) throws IOException {
        if(payloadBody != null) {
            writePayloadBody(httpConn);
        }

        if(payloadFile != null) {
            writeFile(httpConn);
        }
    }

    private void writePayloadBody(HttpURLConnection httpConn) throws IOException {
        OutputStream os = null;
        try {
            os = httpConn.getOutputStream();
            os.write(payloadBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } finally {
            if(os != null) {
                os.close();
            }
        }
    }

    private void writeFile(HttpURLConnection httpConn) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        byte[] buffer = new byte[4096]; // 4 KB
        int bytesRead;
        try {
            is = new FileInputStream(payloadFile);
            os = httpConn.getOutputStream();

            while((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } finally {
            if(os != null) {
                try{ os.close(); } catch (Exception e){/*muffle*/}
            }

            if(is != null) {
                try{ is.close(); } catch (Exception e){/*muffle*/}
            }
        }
    }

    void readResponse(HttpURLConnection httpConn) throws IOException {

        String responseHeaders = readResponseHeaders(httpConn);

        //readResponseBody(httpConn);

        InputStream in = null;
        OutputStream out = null;
        byte[] buff = new byte[4096];
        int bytesRead;

        try {
            in = httpConn.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(getTempFileName()));
            out.write(headers.toString().getBytes());

            while((bytesRead = in.read(buff)) != -1) {
                out.write(buff, 0, bytesRead);
            }

            out.write("\n==============================\n\n".getBytes());
        } finally {
            if(out != null) {
                try{ out.close(); } catch (Exception e) {
                    logger.log(Level.WARNING, "Unable to close output stream", e);
                }
                try{ in.close(); } catch (Exception e) {
                    logger.log(Level.WARNING, "Unable to close input stream", e);
                }
            }
        }
    }

    private String readResponseHeaders(HttpURLConnection httpConn) {
        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, List<String>> e : httpConn.getHeaderFields().entrySet()) {
            for (String headerValue : e.getValue()) {
                if(e.getKey() != null ) { headers.append(e.getKey()).append(": "); }
                headers.append(headerValue).append(LINE_SEPARATOR);
            }
        }
        headers.append(LINE_SEPARATOR);
        return headers.toString();
    }
}
