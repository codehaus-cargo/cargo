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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty4xEmbeddedStandaloneLocalConfigurationCapability;

/**
 * A mostly canned configuration for a Jetty 5.x running embedded. User uses properties to minimally
 * customize the config.
 * 
 */
public class Jetty5xEmbeddedStandaloneLocalConfiguration extends
    AbstractJettyEmbeddedStandaloneLocalConfiguration
{
    /**
     * convenience field for empty Class argument list to reflection methods.
     */
    private static final Class[] EMPTY_CLASS_LIST = new Class[] {};

    /**
     * convenience field for empty Object argument list to reflection methods.
     */
    private static final Object[] EMPTY_ARGS_LIST = new Object[] {};

    /**
     * Capability set for this type of config.
     */
    private static ConfigurationCapability capability =
        new Jetty4xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedStandaloneLocalConfiguration#AbstractJettyEmbeddedStandaloneLocalConfiguration(String)
     */
    public Jetty5xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        // Jetty5x logging works by the use of System properties. If noDiscovery is false, or not
        // set, a Java Commons Logging logger will be used instead. We want to prevent that, and in
        // the case of the canned standalone embedded configuration we want to always use the jetty
        // internal logger.
        System.setProperty("org.mortbay.log.LogFactory.noDiscovery", "true");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * Configure the logging for the Jetty container. For this standalone config, Jetty's internal
     * logging mechanism will be used, iff the user has configured an output file. Otherwise, it is
     * assumed that the user wants to use the JCL discovery mechanism and will therefore be using
     * other means of configuring logging.
     * 
     * TODO this setup might be done a little late in the startup sequence - some log messages may
     * already have come out on stderr.
     * 
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedStandaloneLocalConfiguration#activateLogging(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void activateLogging(LocalContainer container) throws Exception
    {
        ClassLoader cl = ((EmbeddedLocalContainer) container).getClassLoader();

        Class logFactoryClass = cl.loadClass("org.mortbay.log.LogFactory");
        Class outputStreamLogSinkClass = cl.loadClass("org.mortbay.log.OutputStreamLogSink");
        Class sinkClass = cl.loadClass("org.mortbay.log.LogSink");

        Object logFactory = logFactoryClass.getMethod("getFactory", EMPTY_CLASS_LIST)
            .invoke(null, null);

        // Jetty5x logging can either be used with the Java Commons Logging discovery mechanism, or
        // it can use it's own internal logging mechanism. For simplicity, with the canned
        // standalone configuration, we will choose to always use the internal mechanism if the
        // user has configured a log file name.
        if (container.getOutput() != null)
        {
            Object logInstance = logFactory.getClass().getMethod("getInstance",
                new Class[] {String.class}).invoke(logFactory, new Object[] {null});

            logInstance.getClass().getMethod("reset", EMPTY_CLASS_LIST).invoke(logInstance,
                EMPTY_ARGS_LIST);

            Object sink = outputStreamLogSinkClass.getConstructor(new Class[] {String.class})
                .newInstance(new Object[] {container.getOutput()});
            logInstance.getClass().getMethod("add", new Class[] {sinkClass}).invoke(logInstance,
                new Object[] {sink});

            outputStreamLogSinkClass.getMethod("setAppend", new Class[] {boolean.class}).invoke(
                sink, new Object[] {Boolean.valueOf(container.isAppend())});
        }
        else
        {
            getLogger().info("Logging for Jetty container deferred to JCL discovery mechanism",
                this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 5.x Embedded Standalone Configuration";
    }
}
