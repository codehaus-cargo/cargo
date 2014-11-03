package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.tomcat.Tomcat7xStandaloneLocalConfiguration;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id: Tomee1xStandaloneLocalConfiguration 3897 2014-10-12 21:15:15Z collignont $
 */
public class Tomee1xStandaloneLocalConfiguration extends Tomcat7xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see Tomcat7xStandaloneLocalConfiguration#Tomcat7xStandaloneLocalConfiguration(String)
     */
    public Tomee1xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomee 1.x Standalone Configuration";
    }

    /**
     * {@inheritDoc} Tomee provide it's own transaction factory with openejb, so we don't add
     * org.objectweb.jotm.UserTransactionFactory unlike Tomcat
     */
    @Override
    protected void setupTransactionManager()
    {
    }

}
