/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic.internal.configuration.util;

import java.util.Comparator;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.util.CargoException;

/**
 * Comparator evaluating resources according to their priority.
 */
public class PriorityComparator implements Comparator<Resource>
{

    @Override
    public int compare(Resource firstResource, Resource secondResource)
    {
        String firstResourcePriority = firstResource.getParameter("priority");
        String secondResourcePriority = secondResource.getParameter("priority");

        if (firstResourcePriority == null && secondResourcePriority == null)
        {
            return 0;
        }

        if (firstResourcePriority == null)
        {
            return 1;
        }

        if (secondResourcePriority == null)
        {
            return -1;
        }

        Integer firstResourcePriorityValue = null;
        Integer secondResourcePriorityValue = null;
        try
        {
            firstResourcePriorityValue = Integer.valueOf(firstResourcePriority);
            secondResourcePriorityValue = Integer.valueOf(secondResourcePriority);
        }
        catch (NumberFormatException e)
        {
            throw new CargoException("Priority parameter has to be integer value.");
        }

        return firstResourcePriorityValue.compareTo(secondResourcePriorityValue);
    }
}
