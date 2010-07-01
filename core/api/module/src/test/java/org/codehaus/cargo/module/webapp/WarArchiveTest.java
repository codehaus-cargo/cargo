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
package org.codehaus.cargo.module.webapp;

import java.io.File;

import org.codehaus.cargo.util.AbstractResourceTest;

/**
 * Unit tests for {@link WarArchive}.
 *
 * @version $Id$
 */
public final class WarArchiveTest extends AbstractResourceTest
{
    /**
     * Path to package.
     */
    private static final String PACKAGE_PATH = "org/codehaus/cargo/module/";
    
    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>true</code> if the WAR contains the requested class in
     * <code>WEB-INF/classes</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassInWebinfClasses() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getResourcePath(PACKAGE_PATH + "containsclass.war"));
        assertTrue(war.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>true</code> if the WAR contains the requested class in a JAR in
     * <code>WEB-INF/lib</code>.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassInWebinfLib() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getResourcePath(PACKAGE_PATH + "containsclasslib.war"));
        assertTrue(war.containsClass("test.Test"));
    }

    /**
     * Verifies that the method <code>containsClass()</code> returns
     * <code>false</code> if the WAR does not contain such a class.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testContainsClassEmpty() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getResourcePath(PACKAGE_PATH + "empty.war"));
        assertTrue(!war.containsClass("test.Test"));
    }

    public void testStoreArchive() throws Exception
    {
        WarArchive war = new DefaultWarArchive(getResourcePath(PACKAGE_PATH + "weblogic.war"));
        File tmpFile = File.createTempFile("cargo", null);
        war.store(tmpFile);
        
        WarArchive storedWar = new DefaultWarArchive(tmpFile.getPath());
        WebXml descr = storedWar.getWebXml();
        assertTrue("There should be 1 descriptor", descr.getVendorDescriptors().hasNext());
    }
}
