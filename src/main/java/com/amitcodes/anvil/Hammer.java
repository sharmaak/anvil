package com.amitcodes.anvil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
- This should run in an independent thread
- One connection handler for each hammering-request
- Better logging
- Support for Proxy
- support for HTTPS
- support for username:password (server level)
- headers (custom headers)
- Cookies
- turn around time for each request
- average time for N requests

OPTIONS
- Should dump body of each request
- should dump header of each request
- should dump summary only
- keep count of non 2XX responses

- number of parallel requests
- total requests to make
-

*/
public class Hammer
{
    private Logger logger = Logger.getLogger(Hammer.class.getCanonicalName());

    public static void main( String[] args ) throws Exception
    {
        Hammer hammer = new Hammer();
        hammer.foo(new URL("http://www.tired.com"), HttpMethodEnum.GET);
        hammer.foo(new URL("http://www.reddit.com"), HttpMethodEnum.GET);
        hammer.foo(new URL("http://www.reddit.com/djfhsdf"), HttpMethodEnum.GET);
    }

    public void foo(URL target, HttpMethodEnum httpMethod) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection)target.openConnection();
        httpConn.setRequestMethod(httpMethod.toString());
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        // if PUT/POST/DELETE, attempt to write
        if(httpMethod.equals(HttpMethodEnum.POST) ||
                httpMethod.equals(HttpMethodEnum.PUT)  ||
                httpMethod.equals(HttpMethodEnum.DELETE  )) {
            writeToUrl(httpConn);
        }
        // read response
        readResponse(httpConn);
        httpConn.getInputStream();
    }

    void writeToUrl(HttpURLConnection httpConn) throws IOException {
        OutputStream os = null;
        try {
            os = httpConn.getOutputStream();
            logger.log(Level.INFO, "Attempting to write to url");
            // TODO: data abstrction for content to write to server

        } finally {
            if(os != null) {
                os.close();
            }
        }
    }

    void readResponse(HttpURLConnection httpConn) throws IOException {

        // read headers
        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, List<String>> e : httpConn.getHeaderFields().entrySet()) {
            for (String headerValue : e.getValue()) {
                if(e.getKey() != null ) { headers.append(e.getKey()).append(": "); }
                headers.append(headerValue).append(System.getProperty("line.separator"));
            }
        }
        headers.append(System.getProperty("line.separator"));

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

    private String getTempFileName() {
        String outFileName = System.getProperty("java.io.tmpdir") + File.separator + "out-" + System.currentTimeMillis() +".txt";
        logger.log(Level.INFO, "created temp file: {0}", outFileName);
        return outFileName;
    }
}


