package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jboss.internal.JBoss5xInstalledLocalContainer;
import org.codehaus.cargo.container.jboss.internal.JBossInstalledLocalContainer;

public class JBoss5xStandaloneLocalConfiguration extends
		JBossStandaloneLocalConfiguration {

	public JBoss5xStandaloneLocalConfiguration(String dir) {
		super(dir);
	}

	protected FilterChain createJBossFilterChain(JBoss5xInstalledLocalContainer container) throws MalformedURLException {
		FilterChain filterChain = super.createJBossFilterChain(container);
		
		// add the deployer directory needed for JBoss5x
		File deployersDir =
            new File(container.getDeployersDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.deployers.url",
            deployersDir.toURL().toString());
        // add the deploy directory needed for JBoss5x
		File deployDir =
            new File(container.getDeployDir(getPropertyValue(JBossPropertySet.CONFIGURATION)));
        getAntUtils().addTokenToFilterChain(filterChain, "cargo.jboss.deploy.url",
            deployDir.toURL().toString());
        
		return filterChain;
	}
	
	/**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(LocalContainer)
     */
    protected void doConfigure(LocalContainer container) throws Exception
    {
        getLogger().info("Configuring JBoss using the ["
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + "] server configuration",
            this.getClass().getName());

        setupConfigurationDir();

        jbossContainer = (JBoss5xInstalledLocalContainer) container;

        FilterChain filterChain = createJBossFilterChain((JBoss5xInstalledLocalContainer)jbossContainer);

        // Setup the shared class path
        if (container instanceof InstalledLocalContainer)
        {
            InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;
            String[] sharedClassPath = installedContainer.getSharedClasspath();
            StringBuffer tmp = new StringBuffer();
            if (sharedClassPath != null)
            {
            	for (int i = 0; i < sharedClassPath.length; i++)
            	{
            		tmp.append(sharedClassPath[i]);

            		// There is the @cargo.server.deploy.url@ after the @jboss.shared.classpath@
            		tmp.append(',');
            	}
            } 
            else 
            {
            	// if the sharedClassPath is null then we have to add a blank token to be added
            	// Note: adding an empty string will result in an index out of bounds error so a space needs to be added
            	tmp = tmp.append(" ");
            }
            String sharedClassPathString = tmp.toString();
            getLogger().debug("Shared loader classpath is " + sharedClassPathString,
                getClass().getName());
            getAntUtils().addTokenToFilterChain(filterChain, "jboss.shared.classpath",
                tmp.toString());
        }

        getFileHandler().createDirectory(getHome(), "/deploy");
        getFileHandler().createDirectory(getHome(), "/lib");

        String confDir = getFileHandler().createDirectory(getHome(), "/conf");

        // Copy configuration files from cargo resources directory with token replacement
        String[] cargoFiles = new String[] {"cargo-binding.xml", "log4j.xml",
            "jboss-service.xml", "bootstrap-beans.xml"};
        for (int i = 0; i < cargoFiles.length; i++)
        {
            getResourceUtils().copyResource(
                RESOURCE_PATH + jbossContainer.getId() + "/" + cargoFiles[i],
                new File(confDir, cargoFiles[i]), filterChain);
        }

        // Copy resources from jboss installation folder and exclude files
        // that already copied from cargo resources folder
        copyExternalResources(
            new File(jbossContainer.getConfDir(getPropertyValue(JBossPropertySet.CONFIGURATION))),
            new File(confDir), cargoFiles);

        // Deploy the CPC (Cargo Ping Component) to the webapps directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(getHome(), "/deploy/cargocpc.war"));
    }

}
