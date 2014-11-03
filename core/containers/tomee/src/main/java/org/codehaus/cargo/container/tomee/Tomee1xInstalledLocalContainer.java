package org.codehaus.cargo.container.tomee;

import java.io.File;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaInstalledLocalContainer;

/**
 * Special container support for the Apache Tomee 1.x servlet container.
 * 
 * @version $Id: Tomee1xInstalledLocalContainer 3897 2014-10-12 21:15:15Z collignont $
 */
public class Tomee1xInstalledLocalContainer extends AbstractCatalinaInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "tomee1x";

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCatalinaInstalledLocalContainer#AbstractCatalinaInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Tomee1xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public final String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public final String getName()
    {
        return "Tomee " + getVersion("1.x");
    }

    /**
     * Add the <code>tomcat-juli.jar</code> file to classpath and call parent.
     * 
     * @param action Either 'start' or 'stop'
     * @param java the prepared Ant Java command that will be executed
     * @exception Exception in case of container invocation error
     */
    @Override
    protected void invokeContainer(String action, JvmLauncher java) throws Exception
    {
        java.addClasspathEntries(new File(getHome(), "bin/tomcat-juli.jar"));
        super.invokeContainer(action, java);
    }
}
