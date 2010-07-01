/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.application;

import java.io.IOException;

import org.codehaus.cargo.module.JarArchive;
import org.codehaus.cargo.module.ejb.EjbArchive;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.jdom.JDOMException;

/**
 * Class that encapsulates access to an EAR.
 *
 * @version $Id$
 */
public interface EarArchive extends JarArchive
{
    /**
     * Returns the deployment descriptor of the web application.
     *
     * @return The parsed deployment descriptor
     * @throws IOException If there was a problem reading the  deployment descriptor in the EAR
     * @throws JDOMException  If there is an exception reading the application xml
     */
    ApplicationXml getApplicationXml()
        throws IOException, JDOMException;

    /**
     * Returns the web-app archive stored in the EAR with the specified URI.
     *
     * @param uri The URI of the web module
     * @return The web-app archive, or <code>null</code> if no WAR was found at the specified URI
     * @throws IOException If there was an errors reading from the EAR or WAR
     */
    WarArchive getWebModule(String uri) throws IOException;

    /**
     * Returns the ejb archive stored in the EAR with the specified URI.
     *
     * @param uri The URI of the ejb module
     * @return The ejb archive, or <code>null</code> if no WAR was found at the specified URI
     * @throws IOException If there was an errors reading from the EAR or EJB
     */
    EjbArchive getEjbModule(String uri) throws IOException;
}
