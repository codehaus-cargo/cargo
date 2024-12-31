/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.deployable;

/**
 * Version of Deployable. Can be J2EE, Java EE, Jakarta EE, etc.
 */
public final class DeployableVersion
{
    /**
     * The J2EE deployable version.
     */
    public static final DeployableVersion J2EE = new DeployableVersion("j2ee");

    /**
     * The Java EE deployable version.
     */
    public static final DeployableVersion JAVA_EE = new DeployableVersion("javaee");

    /**
     * The Jakarta EE deployable version.
     */
    public static final DeployableVersion JAKARTA_EE = new DeployableVersion("jakartaee");

    /**
     * A unique id that identifies a deployable version.
     */
    private String version;

    /**
     * @param version A unique id that identifies a deployable version
     */
    private DeployableVersion(String version)
    {
        this.version = version;
    }

    /**
     * Transform a version represented as a string into a {@link DeployableVersion} object.
     * 
     * @param versionAsString the string to transform
     * @return the {@link DeployableVersion} object
     */
    public static DeployableVersion toVersion(String versionAsString)
    {
        DeployableVersion version;

        if (versionAsString.equalsIgnoreCase(J2EE.version))
        {
            version = J2EE;
        }
        else if (versionAsString.equalsIgnoreCase(JAVA_EE.version))
        {
            version = JAVA_EE;
        }
        else if (versionAsString.equalsIgnoreCase(JAKARTA_EE.version))
        {
            version = JAKARTA_EE;
        }
        else
        {
            throw new IllegalArgumentException("Invalid deployable version: " + versionAsString);
        }

        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object instanceof DeployableVersion)
        {
            DeployableVersion version = (DeployableVersion) object;
            if (version.version.equalsIgnoreCase(this.version))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.version.hashCode();
    }

    /**
     * @return the deployable version
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.version;
    }
}
