package org.codehaus.cargo.container.liberty;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.liberty.internal.LibertyStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.liberty.local.LibertyInstalledLocalContainer;
import org.codehaus.cargo.container.liberty.local.LibertyInstalledLocalDeployer;
import org.codehaus.cargo.container.liberty.local.LibertyStandaloneLocalConfiguration;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Liberty support
 */
public class LibertyFactoryRegistry extends AbstractFactoryRegistry 
{

    /** TODO
     * Register deployable factory. Doesn't register anything.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory) 
    {
    }

    /** TODO
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory) 
    {
        configurationCapabilityFactory.registerConfigurationCapability("liberty",
                ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                LibertyStandaloneLocalConfigurationCapability.class);
    }

    /** TODO
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory) 
    {
        configurationFactory.registerConfiguration("liberty",
                ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                LibertyStandaloneLocalConfiguration.class);
    }

    /** TODO
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory) 
    {
        deployerFactory.registerDeployer("liberty", DeployerType.INSTALLED,
                LibertyInstalledLocalDeployer.class);
    }

    /** TODO
     * Register packager. Doesn't register anything.
     * 
     * @param packagerFactory Factory on which to register.
     */
    @Override
    protected void register(PackagerFactory packagerFactory) 
    {
    }

    /**
     * Register the Liberty containers. These are things that
     * control the server lifecycle in cargo.
     * 
     * @param containerFactory the factory to register with
     */
    @Override
    protected void register(ContainerFactory containerFactory) 
    {
        containerFactory.registerContainer("liberty", ContainerType.INSTALLED,
                LibertyInstalledLocalContainer.class);
    }

    /** TODO
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory) 
    {
        containerCapabilityFactory.registerContainerCapability("liberty",
                J2EEContainerCapability.class);
    }

}
