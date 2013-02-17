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

import org.codehaus.cargo.module.Xsd;

/**
 * Web 3.0 Descriptor.
 * @version $Id$
 */
public class WebXml30Type extends WebXmlType
{
    /**
     * Single instance.
     */
    private static WebXml30Type instance = new WebXml30Type();

    /**
     * Protected constructor.
     */
    protected WebXml30Type()
    {
        super(WebXml25Type.getInstance(), new Xsd(
                "http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"));
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
        return WebXmlVersion.V3_0;
    }
}
