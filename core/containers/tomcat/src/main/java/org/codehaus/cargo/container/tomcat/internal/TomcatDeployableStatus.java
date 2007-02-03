/* 
 * ========================================================================
 * 
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat.internal;

/**
 * Status of a Deployable deployed in Tomcat. Can be a "running", "stopped", "not found".
 * 
 * @version $Id$
 */
public final class TomcatDeployableStatus
{
    /**
     * The status of the deployable when it's running in Tomcat.
     */
    public static final TomcatDeployableStatus RUNNING = new TomcatDeployableStatus("running");

    /**
     * The status of the deployable when it's stopped in Tomcat.
     */
    public static final TomcatDeployableStatus STOPPED = new TomcatDeployableStatus("stopped");

    /**
     * The status of the deployable when it doesn't exists in Tomcat.
     */
    public static final TomcatDeployableStatus NOT_FOUND = new TomcatDeployableStatus("not found");

    /**
     * A unique id that identifies a deployable status.
     */
    private String status;

    /**
     * @param status A unique id that identifies a deployable status
     */
    private TomcatDeployableStatus(String status)
    {
        this.status = status;
    }

    /**
     * Transform a status represented as a string into a {@link TomcatDeployableStatus} object.
     * 
     * @param statusAsString the string to transform
     * @return the {@link TomcatDeployableStatus} object
     */
    public static TomcatDeployableStatus toStatus(String statusAsString)
    {
        TomcatDeployableStatus status;

        if (statusAsString.equalsIgnoreCase(RUNNING.status))
        {
            status = RUNNING;
        }
        else if (statusAsString.equalsIgnoreCase(STOPPED.status))
        {
            status = STOPPED;
        }
        else if (statusAsString.equalsIgnoreCase(NOT_FOUND.status))
        {
            status = NOT_FOUND;
        }
        else
        {
            status = new TomcatDeployableStatus(statusAsString);
        }

        return status;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object instanceof TomcatDeployableStatus)
        {
            TomcatDeployableStatus status = (TomcatDeployableStatus) object;
            if (status.status.equalsIgnoreCase(this.status))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return this.status.hashCode();
    }

    /**
     * @return the deployable status
     */
    public String getStatus()
    {
        return this.status;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    public String toString()
    {
        return this.status;
    }
}
