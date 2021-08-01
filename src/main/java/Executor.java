import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpHost;
import java.util.Properties;
 
public class Executor {
  private static final int  bufferSize = 1024 * 32;
 
  public static void main(String[] args) {
    String hostString1 = "https://sample.host1.com";
    String hostString2 = "https://sample.host2.com";
    int port = 443;
    String scheme = "https";
    HttpHost host1 = new HttpHost(hostString1, port, scheme);
    HttpHost host2 = new HttpHost(hostString2, port, scheme);
 
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    final ConnectionConfig connectionConfig = 
   ConnectionConfig.custom().setBuffer(bufferSize).build();
    httpClientBuilder.setDefaultConnectionConfig(connectionConfig);
 
    Properties properties = new  Properties();
 //properties needed to create the closeable client
    properties.setProperty(<name>,<value>); 
 
    HttpConnManager connManager = new HttpConnManager(httpClientBuilder,
           properties);
    HttpConnection httpConnection = connManager.getConnection(host1);
 //incomingRequest is a HttpServletRequest, and query is a query payload
    httpConnection.executePost(incomingRequest, query); 
 //eid is employee id , dept is employee department, retrieved from get 
    //request URL parameter
    httpConnection.executeGet(incomingRequest, eid, dept);  
  }
}
