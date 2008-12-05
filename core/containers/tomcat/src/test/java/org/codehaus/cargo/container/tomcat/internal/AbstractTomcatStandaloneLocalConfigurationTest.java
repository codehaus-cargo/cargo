package org.codehaus.cargo.container.tomcat.internal;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

import junit.framework.TestCase;

/**
 * Base class for testing TomCat configuration
 */
public abstract class AbstractTomcatStandaloneLocalConfigurationTest extends TestCase
{
    /**
     * set the base directory that these tests will execute from. Ensure it is a unique directory
     * per test so that forking is possible
     */
    abstract protected String getTestHome();

    protected String CONFIG_HOME = getTestHome() + "/config";

    protected String CONTAINER_HOME = getTestHome() + "/container";

    protected String AJP_PORT = "8001";

    protected AbstractTomcatInstalledLocalContainer container;

    protected AbstractTomcatStandaloneLocalConfiguration configuration;

    protected StandardFileSystemManager fsManager;

    protected FileHandler fileHandler;

    public AbstractTomcatStandaloneLocalConfigurationTest()
    {
        super();
    }

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpContainerDefaults();
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        fileHandler.mkdirs(CONFIG_HOME);
        fileHandler.mkdirs(CONTAINER_HOME);
        this.configuration.setFileHandler(this.fileHandler);
        this.container.setHome(CONTAINER_HOME);
        this.container.setFileHandler(this.fileHandler);

    }
    
    /**
     * initializes the following, based on context of the test.
     *    configuration
     *    container
     *    
     *  ex.
     *    this.configuration = new Tomcat5xStandaloneLocalConfiguration(CONFIG_HOME);
     *    this.container = new Tomcat5xInstalledLocalContainer(configuration);
     */
    abstract protected void setUpContainerDefaults();

    /**
     * reads a file into a String
     * 
     * @param in - what to read
     * @return String contents of the file
     * @throws IOException
     */
    public String slurp(String file) throws IOException
    {
        InputStream in = this.fsManager.resolveFile(file).getContent().getInputStream();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;)
        {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
