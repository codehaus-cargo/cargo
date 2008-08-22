/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

/**
 * Unit tests for {@link Jonas4xInstalledLocalContainer}.
 */
public class Jonas4xInstalledLocalContainerTest extends MockObjectTestCase
{

    private static final String JONAS_ROOT = "ram:///jonasroot";

    private static final String JONAS_BASE = "ram:///jonasbase";

    private Jonas4xInstalledLocalContainer container;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);

        this.fileHandler.createDirectory(null, JONAS_ROOT);
        this.fileHandler.createDirectory(null, JONAS_BASE);

        LocalConfiguration configuration = new Jonas4xStandaloneLocalConfiguration(JONAS_BASE);

        this.container = new Jonas4xInstalledLocalContainer(configuration);
        this.container.setFileHandler(this.fileHandler);
        this.container.setHome(JONAS_ROOT);
    }

    public void testSetupSysProps()
    {
        Mock mockJava = mock(Java.class);

        DoActionConstraint constaint = new DoActionConstraint();
        mockJava.expects(exactly(15)).method("addSysproperty").with(constaint);

        container.setupSysProps((Java) mockJava.proxy());

        mockJava.verify();
        assertEquals(0, constaint.remaingChecks);
    }

    private class DoActionConstraint implements Constraint
    {

        int remaingChecks = 5;

        String settingPair = null;

        public boolean eval(Object arg)
        {
            org.apache.tools.ant.types.Environment.Variable var = (org.apache.tools.ant.types.Environment.Variable) arg;
            settingPair = var.getKey() + "=" + var.getValue();
            if (var.getKey().equals("install.root"))
            {
                remaingChecks--;
                return var.getValue().endsWith("ram:/jonasroot");
            }
            else if (var.getKey().equals("jonas.base"))
            {
                remaingChecks--;
                return var.getValue().endsWith("ram:/jonasbase");
            }
            else if (var.getKey().equals("java.endorsed.dirs"))
            {
                remaingChecks--;
                return var.getValue()
                    .endsWith(fileHandler.append("ram:/jonasroot", "lib/endorsed"));
            }
            else if (var.getKey().equals("java.security.policy"))
            {
                remaingChecks--;
                return var.getValue().endsWith(
                    fileHandler.append("ram:/jonasbase", "conf/java.policy"));
            }
            else if (var.getKey().equals("java.security.auth.login.config"))
            {
                remaingChecks--;
                return var.getValue().endsWith(
                    fileHandler.append("ram:/jonasbase", "conf/jaas.config"));
            }
            return true;
        }

        public StringBuffer describeTo(StringBuffer buffer)
        {
            buffer.append("unexpected settings " + settingPair);
            return buffer;
        }

    }
}
