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
package org.codehaus.cargo.sample.maven2.artifact_installer_test;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test the Maven2/Maven3 Artifact Installer.
 * 
 * @version $Id$
 */
public class ArtifactInstallerTest extends TestCase
{

    /**
     * Test the Maven2/Maven3 Artifact Installer.
     * @throws Exception If anything fails.
     */
    public void testArtifactInstaller() throws Exception
    {
        File target = new File("target");
        assertTrue(target + " is not a directory", target.isDirectory());

        boolean foundJettyBase = false;
        boolean foundJettyDistribution = false;
        for (File contents : target.listFiles())
        {
            if (contents.isDirectory())
            {
                if (contents.getName().equals("jetty-base"))
                {
                    foundJettyBase = true;
                }
                else if (contents.getName().equals("cargo"))
                {
                    File installs = new File(contents, "installs");
                    assertTrue(installs + " is not a directory", installs.isDirectory());
                    for (File jettyDistribution : installs.listFiles())
                    {
                        if (jettyDistribution.isDirectory()
                            && jettyDistribution.getName().startsWith("jetty-distribution-"))
                        {
                            foundJettyDistribution = true;
                        }
                    }
                }
            }
        }

        assertTrue(foundJettyBase);
        assertTrue(foundJettyDistribution);
    }

}
