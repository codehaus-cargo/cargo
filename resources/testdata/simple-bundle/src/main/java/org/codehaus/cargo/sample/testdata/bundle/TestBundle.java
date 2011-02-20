/*
 * ========================================================================
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
package org.codehaus.cargo.sample.testdata.bundle;

import java.io.FileOutputStream;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Sample test OSGi bundle.
 * 
 * @version $Id$
 */
public class TestBundle implements BundleActivator
{
    /**
     * Starts the bundle.
     * 
     * @param bundleContext OSGi bundle context.
     */
    public void start(final BundleContext bundleContext) throws Exception
    {
        FileOutputStream fos = new FileOutputStream("bundle-output.txt", false);

        try
        {
            fos.write("Hello, World".getBytes("UTF-8"));
            fos.flush();
        }
        finally
        {
            fos.close();
        }
    }

    /**
     * Stops the bundle.
     * 
     * @param bundleContext OSGi bundle context.
     */
    public void stop(final BundleContext bundleContext) throws Exception
    {
        FileOutputStream fos = new FileOutputStream("bundle-output.txt", false);

        try
        {
            fos.write("Goodbye, World".getBytes("UTF-8"));
            fos.flush();
        }
        finally
        {
            fos.close();
        }
    }
}
