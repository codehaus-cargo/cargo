package org.codehaus.cargo.container.jboss.internal;

public interface JBoss5xInstalledLocalContainer extends JBossInstalledLocalContainer{

	/**
	 * @param configurationName the JBoss server configuration name for which to return the deployer dir
	 * @return The deployer directory located under the container's home installation directory
	 */
	String getDeployersDir(String configurationName);

}