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
package org.codehaus.cargo.sample.testdata.bundle;

import java.io.File;
import java.io.FileOutputStream;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Sample test OSGi bundle.
 */
public class TestBundle implements BundleActivator
{
    /**
     * The test bundle will be used with containers running on Java 6, we hence cannot use
     * <code>java.nio.charset.StandardCharsets</code> which was introduced in Java 7 only.
     */
    private static final String CHARSET_UTF_8 = "UTF-8";

    /**
     * Starts the bundle, will write a <code>Hello, World</code> text.
     * 
     * @param bundleContext OSGi bundle context.
     * @throws java.lang.Exception If anything goes wrong.
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        String target = System.getProperty("cargo.samples.bundle.targetFile");
        if (target == null || target.isEmpty())
        {
            throw new IllegalArgumentException("cargo.samples.bundle.targetFile not set!");
        }

        File targetFile = new File(target);
        targetFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(targetFile, false);
        try
        {
            fos.write("Hello, World".getBytes(TestBundle.CHARSET_UTF_8));
            fos.flush();
        }
        finally
        {
            fos.close();
        }
    }

    /**
     * Stops the bundle, will write a <code>Goodbye, World</code> text.
     * 
     * @param bundleContext OSGi bundle context.
     * @throws java.lang.Exception If anything goes wrong.
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        String target = System.getProperty("cargo.samples.bundle.targetFile");
        if (target == null || target.isEmpty())
        {
            throw new IllegalArgumentException("cargo.samples.bundle.targetFile not set!");
        }

        File targetFile = new File(target);
        targetFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(targetFile, false);
        try
        {
            fos.write("Goodbye, World".getBytes(TestBundle.CHARSET_UTF_8));
            fos.flush();
        }
        finally
        {
            fos.close();
        }
    }
}
