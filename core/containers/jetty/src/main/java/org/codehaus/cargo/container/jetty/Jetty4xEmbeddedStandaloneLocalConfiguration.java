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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jetty.internal.Jetty4xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * A mostly canned config for a Jetty 4.x container.
 *  
 * @version $Id$
 */
public class Jetty4xEmbeddedStandaloneLocalConfiguration
    extends AbstractJettyStandaloneLocalConfiguration
{
    /**
     * Capability of the Jetty standalone configuration.
     */
    private static ConfigurationCapability capability =
        new Jetty4xEmbeddedStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJettyStandaloneLocalConfiguration#AbstractJettyStandaloneLocalConfiguration(String)
     */
    public Jetty4xEmbeddedStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration#activateLogging(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void activateLogging(LocalContainer container) throws Exception
    {
        ClassLoader cl = ((EmbeddedLocalContainer) container).getClassLoader();

        // Log Jetty output to a file
        Class outputStreamLogSinkClass = cl.loadClass("org.mortbay.util.OutputStreamLogSink");
        Object sink = outputStreamLogSinkClass.getConstructor(
            new Class[] {String.class}).newInstance(new Object[] {container.getOutput()});
        outputStreamLogSinkClass.getMethod("setAppend",
            new Class[] {boolean.class}).invoke(sink,
                new Object[] {Boolean.valueOf(container.isAppend())});

        outputStreamLogSinkClass.getMethod("start", null).invoke(sink, null);

        Class sinkClass = cl.loadClass("org.mortbay.util.LogSink");
        Class logClass = cl.loadClass("org.mortbay.util.Log");
        Object log = logClass.getMethod("instance", null).invoke(null, null);

        // Disable logging to remove all existing sinks
        logClass.getMethod("disableLog", null).invoke(log, null);

        // Add our log sink
        logClass.getMethod("add", new Class[] {sinkClass}).invoke(log, new Object[] {sink});

        // Turn debugging level on if logging level is high only
        String logLevel = getPropertyValue(GeneralPropertySet.LOGGING);
        if (logLevel.equalsIgnoreCase("high"))
        {
            Class codeClass = cl.loadClass("org.mortbay.util.Code");
            codeClass.getMethod("setDebug", new Class[] {boolean.class})
                .invoke(null, new Object[] {Boolean.TRUE});
        }
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Jetty 4.x Embedded Standalone Configuration";
    }
}
