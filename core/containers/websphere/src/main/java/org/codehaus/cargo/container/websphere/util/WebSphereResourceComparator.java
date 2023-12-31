/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.websphere.internal.configuration.WebSphereConfigurationEntryType;

/**
 * Comparator evaluating resources according to order which they should be created in.
 */
public class WebSphereResourceComparator implements Comparator<Resource>
{

    /**
     * Contains order in which resources should be ordered.
     */
    private List<String> orderList = Arrays.asList(
            WebSphereConfigurationEntryType.JMS_SIBUS,
            WebSphereConfigurationEntryType.JMS_SIBUS_MEMBER,
            WebSphereConfigurationEntryType.JMS_CONNECTION_FACTORY,
            WebSphereConfigurationEntryType.JMS_QUEUE
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Resource firstResource, Resource secondResource)
    {
        Integer firstResourceIndex = orderList.indexOf(firstResource.getType());
        Integer secondResourceIndex = orderList.indexOf(secondResource.getType());

        return firstResourceIndex.compareTo(secondResourceIndex);
    }
}
