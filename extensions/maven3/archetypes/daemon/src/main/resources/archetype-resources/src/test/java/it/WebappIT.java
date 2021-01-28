package ${package}.it;

import java.net.URL;
import java.net.HttpURLConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WebappIT
{
    private String baseUrl;

    @Before
    public void initializeTest() throws Exception
    {
        String host = System.getProperty("hostname");
        String port = System.getProperty("servlet.port");
        this.baseUrl = "http://" + host + ":" + port + "/${artifactId}";
    }

    @Test
    public void callIndexPage() throws Exception
    {
        URL url = new URL(this.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assert.assertEquals(200, connection.getResponseCode());
    }
}
