/* 
 * ========================================================================
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.sample.java.jboss;

import junit.framework.Test;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.testdata.ejb.Sample;
import org.codehaus.cargo.sample.testdata.ejb.SampleHome;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class EjbCapabilityContainerTest extends AbstractCargoTestCase
{   
    public EjbCapabilityContainerTest(String testName, EnvironmentTestData testData) 
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        // We exclude jboss6x container as it seems to have issues with EJB2s
        Set excludedContainerIds = new TreeSet();
        excludedContainerIds.add("jboss6x");

        CargoTestSuite suite = new CargoTestSuite(
            "Tests that can run on containers supporting EJB deployments");
        suite.addTestSuite(EjbCapabilityContainerTest.class, new Validator[] {
            new StartsWithContainerValidator("jboss"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()}, excludedContainerIds);
        return suite;
    }

    public void testDeployEjbStatically() throws Exception    
    {        
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        Deployable ejb = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-ejb"), DeployableType.EJB);
        
        getLocalContainer().getConfiguration().addDeployable(ejb);

        getLocalContainer().start();

        // Call the EJB to verify it's correctly deployed. Unfortunately this is a bit tricky here.
        // Indeed calling an EJB remotely is different for each container and requires:
        // - a container-specific inital context factory
        // - having container client jars in the classpath
        // In addition we must be sure to use the same container client jar version as the container
        // being deployed to. Thus our solution is to use an URL classloader to load the container
        // client jar and use reflection to call the EJB using this classloader.

        File allClientJar =
            new File(getInstalledLocalContainer().getHome(), "client/jbossall-client.jar");
        URL[] urls = new URL[] { allClientJar.toURI().toURL() };
        URLClassLoader classloader = new URLClassLoader(urls, getClass().getClassLoader());
        
        // Ensure that the container's initial context factory implementation will be loaded using
        // our URL classloader. Note: I hope all JDKs use the context classloader to load the 
        // initial context factory class...
        Thread.currentThread().setContextClassLoader(classloader);
        
        Properties props = new Properties();
        props.setProperty(
            Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, "jnp://" 
            + getLocalContainer().getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME)
            + ":"
            + getLocalContainer().getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        InitialContext context = new InitialContext(props);
        Object objref = context.lookup("SampleEJB");
        
        SampleHome home = (SampleHome) PortableRemoteObject.narrow(objref, SampleHome.class);
        Sample sample = home.create();
        assertTrue("Shoud have returned true", sample.isWorking());

        getLocalContainer().stop();
    }  
}
