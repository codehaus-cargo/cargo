/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.Dtd;

/**
 * Web 2.5 Descriptor.
 * @version $Id$
 */
public class WebXml25Type extends WebXmlType
{
    /**
     * Single instance.
     */
    private static WebXml25Type instance = new WebXml25Type();

    /**
     * Protected constructor.
     */
    protected WebXml25Type()
    {
        // We don't have an XSD grammar orderer yet so use 2.3 for now
        super(WebXml22Type.getInstance(), new Dtd(
                "http://www.sun.com/software/dtd/appserver/sun-web-app_2_5-0.dtd"));
        setDescriptorIo(new WebXmlIo(this));
    }

    /**
     * Get the instance of the WEB XML Type.
     * @return WebXmlType
     */
    public static WebXmlType getInstance()
    {
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebXmlVersion getVersion()
    {
        return WebXmlVersion.V2_5;
    }
}
