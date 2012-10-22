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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Special container support for the Jetty 9.x servlet container.
 * 
 * @version $Id$
 */
public class Jetty9xInstalledLocalContainer extends Jetty8xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    private static final String ID = "jetty9x";

    /**
     * Jetty9xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty9xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "Jetty 9.x";
    }

    /**
     * @return Arguments to add to the Jetty <code>start.jar</code> command.
     */
    @Override
    protected String[] getStartArguments()
    {
        List<String> startArguments = new ArrayList<String>();

        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-logging.xml"));
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty.xml"));
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-annotations.xml"));
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-http.xml"));
        // Make sure Jetty-plus is part of startup so that CARGO-1122 is tested
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-plus.xml"));
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/jetty-deploy.xml"));
        startArguments.add(getFileHandler().append(getConfiguration().getHome(),
                "etc/test-realm.xml"));

        String[] startArgumentsArray = new String[startArguments.size()];
        return startArguments.toArray(startArgumentsArray);
    }
}
