package org.codehaus.cargo.container.spi;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Default container implementation that all local embedded container implementations must extend.
 *
 * @version $Id$
 */
public abstract class AbstractEmbeddedLocalContainer
    extends AbstractLocalContainer implements EmbeddedLocalContainer
{
    /**
     * Classloader to use for loading the Embedded container's classes.
     */
    private ClassLoader classLoader;

    /**
     * {@inheritDoc}
     * @see AbstractLocalContainer#AbstractLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @param classLoader the custom classloader to use for loading the Embedded container's
     *        classes.
     */
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     * @return the custom classloader to use for loading the Embedded container's classes.
     */
    public ClassLoader getClassLoader()
    {
        ClassLoader cl = this.classLoader;

        if (this.classLoader == null)
        {
            cl = getClass().getClassLoader();
        }

        return cl;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#startInternal()
     */
    protected final void startInternal() throws Exception
    {
        doStart();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#stopInternal()
     */
    protected final void stopInternal() throws Exception
    {
        doStop();
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} that all
     * containers extending this class must implement.
     *
     * @throws Exception if any error is raised during the container start
     */
    protected abstract void doStart() throws Exception;

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} that all
     * containers extending this class must implement.
     *
     * @throws Exception if any error is raised during the container stop
     */
    protected abstract void doStop() throws Exception;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getType()
     */
    public ContainerType getType()
    {
        return ContainerType.EMBEDDED;
    }
}
