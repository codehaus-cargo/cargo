package ${package}.it;

import java.net.URL;
import java.net.HttpURLConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultipleContainersIT
{
    private String baseUrl;
    private String baseUrl2;

    @Before
    public void initializeTest() throws Exception
    {
        String port = System.getProperty("servlet.port");
        this.baseUrl = "http://localhost:" + port + "/${artifactId}";
        port = System.getProperty("servlet2.port");
        this.baseUrl2 = "http://localhost:" + port + "/${artifactId}";
    }

    @Test
    public void callIndexPage() throws Exception
    {
        URL url = new URL(this.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assert.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void callIndexPageOnSecondContainer() throws Exception
    {
        URL url = new URL(this.baseUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assert.assertEquals(200, connection.getResponseCode());
    }
}
