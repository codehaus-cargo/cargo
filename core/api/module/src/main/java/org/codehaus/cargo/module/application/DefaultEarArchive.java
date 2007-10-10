/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module.application;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.DefaultJarArchive;
import org.codehaus.cargo.module.ejb.DefaultEjbArchive;
import org.codehaus.cargo.module.ejb.EjbArchive;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.jdom.JDOMException;

/**
 * Encapsulates access to an EAR.
 *
 * @version $Id$
 */
public class DefaultEarArchive extends DefaultJarArchive implements EarArchive
{
    /**
     * The parsed deployment descriptor.
     */
    private ApplicationXml applicationXml;

    /**
     * {@inheritDoc}
     * @see DefaultJarArchive#DefaultJarArchive(String)
     */
    public DefaultEarArchive(String file)
    {
        super(file);
    }

    /**
     * Constructor.
     *
     * @param inputStream The input stream for the enterprise application archive
     * @throws IOException If there was a problem reading the EAR
     */
    public DefaultEarArchive(InputStream inputStream) throws IOException
    {
        super(inputStream);
    }

    /**
     * {@inheritDoc}
     * @throws JDOMException
     * @see EarArchive#getApplicationXml()
     */
    public final ApplicationXml getApplicationXml()
        throws IOException, JDOMException
    {
        if (this.applicationXml == null)
        {
            InputStream in = null;
            try
            {
                in = getResource("META-INF/application.xml");
                this.applicationXml = ApplicationXmlIo.parseApplicationXml(in, null);
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
        }
        return this.applicationXml;
    }

    /**
     * {@inheritDoc}
     * @see EarArchive#getWebModule(String)
     */
    public final WarArchive getWebModule(String uri) throws IOException
    {
        InputStream war = null;
        try
        {
            war = getResource(uri);
            if (war != null)
            {
                return new DefaultWarArchive(war);
            }
        }
        finally
        {
            if (war != null)
            {
                war.close();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @see EarArchive#getWebModule(String)
     */
    public final EjbArchive getEjbModule(String uri) throws IOException
    {
        InputStream ejb = null;
        try
        {
            ejb = getResource(uri);
            if (ejb != null)
            {
                return new DefaultEjbArchive(ejb);
            }
        }
        finally
        {
            if (ejb != null)
            {
                ejb.close();
            }
        }
        return null;
    }
}
