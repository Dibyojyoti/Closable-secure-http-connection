import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;   
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
 
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
 
import org.apache.http.HttpPost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBulder;
import org.apache.http.ssl.SSLContexts;

public class HttpConnManager {
  public static final String[] secureProtocols = {"TLSv1","TLSv1.1", "TLSv1.2"};
  public static final String SOCKET_TIMEOUT_PROP = "http.client.timeout.read";
  public static final int SOCKET_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(4);
  public static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
 
  public static final String MAX_CONN_PER_ROUTE_PROP =  
  "http.client.conn.max.route";
  public static final int MAX_CONN_PER_ROUTE = 128;
  public static final String MAX_CONN_TOTAL_PROP = "http.client.conn.max.total";
  public static final int MAX_CONN_TOTAL = 500 * MAX_CONN_PER_ROUTE ;
  public static final String EVICT_INTERVAL_PROP = 
   "http.client.conn.evict-idle.interval";
  public static final long EVICT_INTERVAL = TimeUnit.MINUTES.toMillis(1);
  public static final String CONN_TIME_TO_LIVE_PROP = "http.client.conn.ttl";
  public static final int CONN_TIME_TO_LIVE = (int) TimeUnit.MINUTES.toMillis(1);
 
 private CloseableHttpClient CClient;
  private Lock lock = new ReentrantLock();
  private ConcurrentMap<HttpHost, HttpConnection> connections = 
    new ConcurrentHashMap<>();
public  HttpConnManager(HttpClientBuilder builder, Properties properties) {
   init(builder, properties);
  }
  private void init(HttpClientBuilder builder, Properties properties ) {
    Builder requestConfig = RequestConfig.custom();
    requestConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
    int socketTimeout = getProperty(properties, SOCKET_TIMEOUT_PROP,  
          SOCKET_TIMEOUT);
    requestConfig.setSocketTimeout(socketTimeout);
 
    builder.setDefaultRequestConfig(requestConfig.build());
    builder.disableAutomaticRetries();
    builder.disableAuthCaching();
    builder.disableConnectionState();
    builder.disableCookiemanagement();
    builder.disableRedirectHandling();  
 
    long evictInterval = getProperty (properties, EVICT_INTERVAL_PROP,  
         EVICT_INTERVAL);
    builder.evictIdleConnections( evictInterval, TimeUnit.MILLISECONDS);
 
    int maxConnperRoute =  getProperty (properties, MAX_CONN_PER_ROUTE_PROP ,  
         MAX_CONN_PER_ROUTE);
    builder.setMaxConnPeroute(maxConnperRoute);
 
    int maxConnTotal = getProperty(properties,MAX_CONN_TOTAL_PROP,
         MAX_CONN_TOTAL);
    builder.setMaxConnTotal(maxConnTotal);
 
    int connTtl =  getProperty (properties, CONN_TIME_TO_LIVE_PROP,  
CONN_TIME_TO_LIVE);
    builder.setConnectionTimeToLive(connTtl);
 
    CClient = secureClient(builder); 
  }
  private CloseableHttpClient secureClient(HttpClientBuilder builder) {
    try {
      SSLContext sslcontext = SSLContexts.custom()                                                   .   .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
            .build();
      HostnameVerifier verifier = v   
    SSLConnectionSocketFactory.getDefaultHostnameVerifier();
      SSLConnectionSocketFactory socketFactory = 
    new SSLConnectionSocketFactory(sslcontext, secureProtocols, null, 
         verifier);
      builder.setSSLSocketFactory(socketFactory);
 
      return builder.build();
    }catch(NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      throw new RuntimeException(e);
    }
  }
  private int getProperty(Properties properties, String name, long value) {
    final String strValue =   properties.getProperty(name);
    if( strValue !== null) { value = Integer.parseInt(strValue);}
    return value;
  }
  private long getProperty(Properties properties, String name, long value) {
    final String strValue =   properties.getProperty(name);
    if( strValue !== null) { value = Long.parseLong(strValue);}
    return value;
  }
  public HttpConnection getConnection(HttpHost host) {
    HttpConnection conn = connections.get(host);
    if( conn == null) {
       conn = createConnection(host); 
    }
    return  conn;
  }
  private  HttpConnection createConnection(HttpHost host) {
     HttpConnection conn;
     lock.lock();
     try {
       conn = connections.get(host);
       if( conn == null) {
          conn = createConnectionImpl(host);
          connections.put(host, connection);
       }
       return conn;
     } finally {
       lock.unlock();
     }
  }
  private  HttpConnection createConnectionImpl(HttpHost host) {
    return new HttpConnection(CClient, host); 
  }  
} 
