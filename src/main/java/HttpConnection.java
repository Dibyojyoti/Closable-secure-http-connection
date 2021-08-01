import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.cookie.SM.COOKIE;
 
import java.io.IOException;
import java.util.Objects;
 
import javax.servlet.http.HttpServletRequest;
 
import org.apache.http.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnection {
  //@formatter:off
  public static final String relativepath_Get =
    "/api/v1/rest/employee/details?eid=%1$s&dept=%2$s";
  //@formatter:on
 public static final String relativepath_Post =
   "/api/v1/rest/employee/details";
 
  private final Logger logger =LoggerFactory.getLogger(HttpConnection.class);  
  private CloseableHttpClient client;
  private HttpHost host;
 
  public HttpConnection(CloseableHttpClient client,  HttpHost host) {
    this.client = client;
    this.host = host;
  }
  public String executePost(HttpServletRequest incomingRequest, String query)  
            throws IOException {
     CloseableHttpResponse response = null;
     try {
       HttpPost request = new HttpPost(relativepath_Post);
       addHeaders(incomingRequest, request);
       request.setEntity(new StringEntity(query,ContentType.APPLICATION_JSON));
       response = execute(request);
       String postResponse = EntityUtils.toString(response.getEntity());
       return  postResponse;
     } finally {
      close(response);
     }
  }
  public QueryResponse executeGet(HttpServletRequest incomingRequest, String eid, 
        String dept) throws RuntimeException {
     CloseableHttpResponse response = null; 
     try { 
       String path = String.format(relativePath_Get, eid, dept);
       HttpGet request = new HttpGet(path);
       addHeaders(incomingRequest, request);
       response = execute(request);
       int status = response.getStatusLine().getStatusCode();
       if(status != 200) {
           String msg = String.format("received status code %1$d for request 
         %2$s %3$s", status, host, path);
           throw new RuntimeException(msg);
       }
       QueryResponse queryResponse = toQueryResponse(response);
       return  queryResponse;   
     } catch(RuntimeException e) { 
       throw e;
     } catch(Throwable t) {
      throw new RuntimeException(host.toString()+ ' '+ t.getMessage(), t);
     } finally {
      close(response);
     }
  }
  public ClosableHttpResponse execute(HttpRequest request) throws IOException {
     ClosableHttpResponse response = client.execute(host, request);
     return response;
  }
  private void close(ClosableHttpResponse response) {
     if(response == null) {
      return;
     }
     EntityUtils.consumeQuietly(response.getEntity());
     try {
       response.close();
     } catch(IOException e) {
       logger.warn(e,getMessage(), e);
     }
  }
  protected void addHeaders(HttpServletRequest request, HttpRequestbase req) {
     req.setHeader("Authorization", request.getHeader("Authorization"));
     req.setHeader(COOKIE, request.getHeader("cookie"));
     req.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }
  private QueryResponse toQueryResponse(HttpResponse response) throws IOException {
    int status = response.getStatusLine().getStatusCode();
    String body = null;
    if(status != SC_NO_CONTENT) {
      body = EntityUtils.toString(response.getEntity());
    }
    QueryResponse result = new QueryResponse(status, body);
    Header[] headers = response.getAllHeaders();
    for(Header header : headers) {
     result.addHeader(header.getName(), header.getValue());
    }
    return result;
  }
}
