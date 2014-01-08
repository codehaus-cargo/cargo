package ${package}.it;

import junit.framework.TestCase;

import java.net.URL;
import java.net.HttpURLConnection;

public class WebappIT extends TestCase
{
    private String baseUrl;

    public void setUp() throws Exception
    {
        super.setUp();
        String host = System.getProperty("hostname");
        String port = System.getProperty("servlet.port");
        this.baseUrl = "http://" + host + ":" + port + "/${artifactId}";
    }

    public void testCallIndexPage() throws Exception
    {
        URL url = new URL(this.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertEquals(200, connection.getResponseCode());
    }
}
