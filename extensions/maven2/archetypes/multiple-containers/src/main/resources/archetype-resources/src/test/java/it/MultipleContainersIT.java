package ${package}.it;

import junit.framework.TestCase;

import java.net.URL;
import java.net.HttpURLConnection;

public class MultipleContainersIT extends TestCase
{
    private String baseUrl;
    private String baseUrl2;

    public void setUp() throws Exception
    {
        super.setUp();
        String port = System.getProperty("servlet.port");
        this.baseUrl = "http://localhost:" + port + "/${artifactId}";
        port = System.getProperty("servlet2.port");
        this.baseUrl2 = "http://localhost:" + port + "/${artifactId}";
    }

    public void testCallIndexPage() throws Exception
    {
        URL url = new URL(this.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertEquals(200, connection.getResponseCode());
    }

    public void testCallIndexPageOnSecondContainer() throws Exception
    {
        URL url = new URL(this.baseUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        assertEquals(200, connection.getResponseCode());
    }
}
