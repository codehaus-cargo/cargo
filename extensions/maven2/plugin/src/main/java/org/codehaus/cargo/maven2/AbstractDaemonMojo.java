/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.maven2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonPropertySet;

/**
 * Common mojo for all daemon actions (start deployable, stop deployable).
 * 
 * @version $Id$
 */
public abstract class AbstractDaemonMojo extends AbstractCargoMojo
{
    /**
     * The daemon client instance.
     */
    protected DaemonClient daemonClient = null;

    /**
     * The daemon handle identifier to use.
     */
    protected String daemonHandleId = null;

    /**
     * Tells if the container should autostart.
     */
    protected boolean daemonAutostartContainer = false;

    /**
     * The additional classpath entries the daemon should use.
     */
    protected List<String> daemonClasspaths = null;

    /**
     * The deployables to deploy.
     */
    protected final List<org.codehaus.cargo.container.deployable.Deployable> daemonDeployables =
        new ArrayList<org.codehaus.cargo.container.deployable.Deployable>();

    /**
     * The container that should be started by the daemon.
     */
    protected InstalledLocalContainer daemonContainer;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        getCargoProject().setDaemonRun(true);

        org.codehaus.cargo.container.Container container = createContainer();

        if (!(container instanceof InstalledLocalContainer))
        {
            throw new MojoExecutionException("Container must be of INSTALLED type.");
        }

        String daemonURL = getDaemon().getProperty(DaemonPropertySet.URL);
        String daemonUsername = getDaemon().getProperty(DaemonPropertySet.USERNAME);
        String daemonPassword = getDaemon().getProperty(DaemonPropertySet.PASSWORD);
        String daemonHandleId = getDaemon().getProperty(DaemonPropertySet.HANDLE);

        if (daemonURL == null || daemonURL.length() == 0)
        {
            throw new MojoExecutionException("Missing daemon URL property.");
        }

        if (daemonHandleId == null || daemonHandleId.length() == 0)
        {
            throw new MojoExecutionException("Missing daemon handle id property.");
        }

        daemonAutostartContainer =
            Boolean.valueOf(getDaemon().getProperty(DaemonPropertySet.AUTOSTART));
        daemonClasspaths = getDaemon().getClasspaths();

        try
        {
            if (daemonUsername != null && daemonUsername.length() > 0 && daemonPassword != null
                && daemonPassword.length() > 0)
            {
                this.daemonClient =
                    new DaemonClient(new URL(daemonURL), daemonUsername, daemonPassword);
            }
            else if (daemonUsername != null && daemonUsername.length() > 0)
            {
                this.daemonClient = new DaemonClient(new URL(daemonURL), daemonUsername);
            }
            else
            {
                this.daemonClient = new DaemonClient(new URL(daemonURL));
            }
        }
        catch (MalformedURLException e)
        {
            throw new MojoExecutionException("Malformed daemon URL: " + e);
        }
        this.daemonHandleId = daemonHandleId;
        this.daemonContainer = (InstalledLocalContainer) container;

        createDeployables(container);

        performAction();
    }

    /**
     * Performs the actual action.
     * 
     * @throws MojoExecutionException If an error happens
     */
    protected abstract void performAction() throws MojoExecutionException;

    /**
     * Perform deployment action on all deployables (defined in the deployer configuration element
     * and on the autodeployable).
     * 
     * @param container the container to deploy to the daemon
     * @throws MojoExecutionException in case of a deployment error
     */
    private void createDeployables(org.codehaus.cargo.container.Container container)
        throws MojoExecutionException
    {
        if (getDeployablesElement() != null)
        {
            for (Deployable deployableElement : getDeployablesElement())
            {
                org.codehaus.cargo.container.deployable.Deployable deployable =
                    deployableElement.createDeployable(container.getId(), getCargoProject());
                URL pingURL = deployableElement.getPingURL();
                Long pingTimeout = deployableElement.getPingTimeout();
                addDeployable(deployable, pingURL, pingTimeout);
            }
        }

        // Perform deployment action on the autodeployable (if any).
        if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging())
        {
            if (getDeployablesElement() == null
                || !containsAutoDeployable(getDeployablesElement()))
            {
                // Has the auto-deployable already been specified as part of the <deployables>
                // config element?
                addDeployable(createAutoDeployDeployable(container), null, null);
            }
        }
    }

    /**
     * Adds a deployable to the list.
     * 
     * @param deployable The deployable
     * @param pingURL The pingURL
     * @param pingTimeout The pingTimeout
     */
    private void addDeployable(org.codehaus.cargo.container.deployable.Deployable deployable,
        URL pingURL, Long pingTimeout)
    {
        daemonDeployables.add(deployable);
    }
}
