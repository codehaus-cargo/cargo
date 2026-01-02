/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.sample.maven3.artifact_installer_test;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test the Maven 3 Artifact Installer.
 */
public class ArtifactInstallerTest
{

    /**
     * Test the Maven 3 Artifact Installer.
     * @throws Exception If anything fails.
     */
    @Test
    public void testArtifactInstaller() throws Exception
    {
        File target = new File("target");
        Assertions.assertTrue(target.isDirectory(), target + " is not a directory");

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
                    Assertions.assertTrue(
                        installs.isDirectory(), installs + " is not a directory");
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

        Assertions.assertTrue(foundJettyBase);
        Assertions.assertTrue(foundJettyDistribution);
    }

}
