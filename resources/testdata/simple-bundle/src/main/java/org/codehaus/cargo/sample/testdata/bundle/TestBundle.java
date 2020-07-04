/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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
package org.codehaus.cargo.sample.testdata.bundle;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Sample test OSGi bundle.
 */
public class TestBundle implements BundleActivator
{
    /**
     * Starts the bundle.
     * 
     * @param bundleContext OSGi bundle context.
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        try (FileOutputStream fos = new FileOutputStream("bundle-output.txt", false))
        {
            fos.write("Hello, World".getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
    }

    /**
     * Stops the bundle.
     * 
     * @param bundleContext OSGi bundle context.
     */
    public void stop(BundleContext bundleContext) throws Exception
    {
        try (FileOutputStream fos = new FileOutputStream("bundle-output.txt", false))
        {
            fos.write("Goodbye, World".getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
    }
}
