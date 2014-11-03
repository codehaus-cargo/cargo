package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer;
import org.codehaus.cargo.container.tomcat.TomcatDirectoryPackager;
import org.codehaus.cargo.container.tomcat.TomcatExistingLocalConfiguration;
import org.codehaus.cargo.container.tomcat.TomcatRuntimeConfiguration;
import org.codehaus.cargo.container.tomcat.TomcatWAR;
import org.codehaus.cargo.container.tomcat.internal.Tomcat7x8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.TomcatExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.TomcatRuntimeConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Tomee support into default factories.
 * 
 * @version $Id: TomeeFactoryRegistry 3897 2014-10-12 21:15:15Z collignont $
 */
public class TomeeFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Register deployable factory.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("tomee1x", DeployableType.WAR, TomcatWAR.class);
    }

    /**
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("tomee1x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomee1x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomee1x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            TomcatRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("tomee1x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, Tomee1xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomee1x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomee1x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, TomcatRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("tomee1x", DeployerType.INSTALLED,
            TomcatCopyingInstalledLocalDeployer.class);
    }

    /**
     * Register packager.
     * 
     * @param packagerFactory Factory on which to register.
     */
    @Override
    protected void register(PackagerFactory packagerFactory)
    {
        packagerFactory.registerPackager("tomee1x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("tomee1x",
            ServletContainerCapability.class);
    }

    /**
     * Register container.
     * 
     * @param containerFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerFactory containerFactory)
    {
        containerFactory.registerContainer("tomee1x", ContainerType.INSTALLED,
            Tomee1xInstalledLocalContainer.class);
    }
}
