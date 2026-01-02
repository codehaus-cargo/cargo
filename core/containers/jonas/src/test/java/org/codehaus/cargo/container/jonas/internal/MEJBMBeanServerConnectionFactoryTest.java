/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import java.util.Hashtable;

import javax.naming.Context;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.Jonas4xRuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;

/**
 * Unit tests for {@link MEJBMBeanServerConnectionFactory}.
 */
public class MEJBMBeanServerConnectionFactoryTest
{
    /**
     * MEJB MBean sever connection factory.
     */
    private MEJBMBeanServerConnectionFactory factory;

    /**
     * Creates the test MEJB server connection factory.
     */
    @BeforeEach
    protected void setUp()
    {
        factory = new MEJBMBeanServerConnectionFactory();
    }

    /**
     * Test default configuration.
     */
    @Test
    public void testDefaultConfiguration()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test initial context factory.
     */
    @Test
    public void testInitialContextFactory()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JNDI_INIT_CTX_FACT, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY), "foo");
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test provider URL.
     */
    @Test
    public void testProviderURL()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.URI, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL), "foo");
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test username.
     */
    @Test
    public void testUsername()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.USERNAME, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test password.
     */
    @Test
    public void testPassword()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.PASSWORD, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test username and password.
     */
    @Test
    public void testUsernameAndPassword()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.USERNAME, "foo");
        configuration.setProperty(RemotePropertySet.PASSWORD, "bar");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertEquals(connectionOptions.get(Context.SECURITY_PRINCIPAL), "foo");
        Assertions.assertEquals(connectionOptions.get(Context.SECURITY_CREDENTIALS), "bar");

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertEquals(factory.username, "foo");
        Assertions.assertEquals(factory.password, "bar");
        Assertions.assertNull(factory.jaasFile);
        Assertions.assertNull(factory.jaasEntry);
    }

    /**
     * Test JAAS file.
     */
    @Test
    public void testJAASFile()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        try
        {
            factory.readConfiguration(configuration);
            Assertions.fail();
        }
        catch (IllegalArgumentException expected)
        {
            // OK
        }
    }

    /**
     * Test JAAS entry.
     */
    @Test
    public void testJAASEntry()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertEquals(
            factory.jaasFile, MEJBMBeanServerConnectionFactory.DEFAULT_JAAS_FILE);
        Assertions.assertEquals(factory.jaasEntry, "foo");
    }

    /**
     * Test JAAS file and entry.
     */
    @Test
    public void testJAASFileAndEntry()
    {
        RuntimeConfiguration configuration = new Jonas4xRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY, "bar");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        Assertions.assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
            MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        Assertions.assertEquals(connectionOptions.get(Context.PROVIDER_URL),
            MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        Assertions.assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        Assertions.assertEquals(
            factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        Assertions.assertNull(factory.username);
        Assertions.assertNull(factory.password);
        Assertions.assertEquals(factory.jaasFile, "foo");
        Assertions.assertEquals(factory.jaasEntry, "bar");
    }

    /**
     * Test empty handler.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testEmptyHandler() throws Exception
    {
        testDefaultConfiguration();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test"),
                new PasswordCallback("test", false)
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new TextInputCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test username handler.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testUsernameHandler() throws Exception
    {
        testUsername();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test"),
                new PasswordCallback("test", false)
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new TextInputCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test password handler.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testPasswordHandler() throws Exception
    {
        testPassword();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test"),
                new PasswordCallback("test", false)
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (NullPointerException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new TextInputCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test username and password handler.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testUsernameAndPasswordHandler() throws Exception
    {
        testUsernameAndPassword();

        factory.handle(new Callback[]
        {
            new NameCallback("test")
        });

        factory.handle(new Callback[]
        {
            new NameCallback("test"),
            new PasswordCallback("test", false)
        });

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test"),
                new PasswordCallback("test", false),
                new TextInputCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new TextInputCallback("test")
            };

            factory.handle(callbacks);
            Assertions.fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }
}
