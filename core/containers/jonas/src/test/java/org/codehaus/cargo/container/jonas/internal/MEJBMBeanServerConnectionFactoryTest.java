/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import java.util.Hashtable;

import javax.naming.Context;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.jonas.JonasRuntimeConfiguration;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.jmock.MockObjectTestCase;

/**
 * Unit tests for {@link MEJBMBeanServerConnectionFactory}.
 * 
 * @version $Id$
 */
public class MEJBMBeanServerConnectionFactoryTest extends MockObjectTestCase
{
    /**
     * MEJB MBean sever connection factory.
     */
    private MEJBMBeanServerConnectionFactory factory;

    /**
     * Creates the test MEJB server connection factory. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        factory = new MEJBMBeanServerConnectionFactory();
    }

    /**
     * Test default configuration.
     */
    public void testDefaultConfiguration()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test initial context factory.
     */
    public void testInitialContextFactory()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JNDI_INIT_CTX_FACT, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY), "foo");
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test provider URL.
     */
    public void testProviderURL()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.URI, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL), "foo");
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test username.
     */
    public void testUsername()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.USERNAME, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test password.
     */
    public void testPassword()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.PASSWORD, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test username and password.
     */
    public void testUsernameAndPassword()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(RemotePropertySet.USERNAME, "foo");
        configuration.setProperty(RemotePropertySet.PASSWORD, "bar");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertEquals(connectionOptions.get(Context.SECURITY_PRINCIPAL), "foo");
        assertEquals(connectionOptions.get(Context.SECURITY_CREDENTIALS), "bar");

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertEquals(factory.username, "foo");
        assertEquals(factory.password, "bar");
        assertNull(factory.jaasFile);
        assertNull(factory.jaasEntry);
    }

    /**
     * Test JAAS file.
     */
    public void testJAASFile()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        try
        {
            factory.readConfiguration(configuration);
            fail();
        }
        catch (IllegalArgumentException expected)
        {
            // OK
        }
    }

    /**
     * Test JAAS entry.
     */
    public void testJAASEntry()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY, "foo");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertEquals(factory.jaasFile, MEJBMBeanServerConnectionFactory.DEFAULT_JAAS_FILE);
        assertEquals(factory.jaasEntry, "foo");
    }

    /**
     * Test JAAS file and entry.
     */
    public void testJAASFileAndEntry()
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_FILE, "foo");
        configuration.setProperty(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY, "bar");
        Hashtable<String, Object> connectionOptions = factory.readConfiguration(configuration);

        assertEquals(connectionOptions.get(Context.INITIAL_CONTEXT_FACTORY),
                MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_INITIAL_CTX_FACTORY);
        assertEquals(connectionOptions.get(Context.PROVIDER_URL),
                MEJBMBeanServerConnectionFactory.DEFAULT_PROVIDER_URI);
        assertNull(connectionOptions.get(Context.SECURITY_PRINCIPAL));
        assertNull(connectionOptions.get(Context.SECURITY_CREDENTIALS));

        assertEquals(factory.mejbJndiPath, MEJBMBeanServerConnectionFactory.DEFAULT_JNDI_MEJB_PATH);

        assertNull(factory.username);
        assertNull(factory.password);
        assertEquals(factory.jaasFile, "foo");
        assertEquals(factory.jaasEntry, "bar");
    }

    /**
     * Test empty handler.
     */
    public void testEmptyHandler() throws UnsupportedCallbackException
    {
        testDefaultConfiguration();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            fail();
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
            fail();
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
            fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test username handler.
     */
    public void testUsernameHandler() throws UnsupportedCallbackException
    {
        testUsername();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            fail();
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
            fail();
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
            fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test password handler.
     */
    public void testPasswordHandler() throws UnsupportedCallbackException
    {
        testPassword();

        try
        {
            Callback[] callbacks = new Callback[]
            {
                new NameCallback("test")
            };

            factory.handle(callbacks);
            fail();
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
            fail();
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
            fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }

    /**
     * Test username and password handler.
     */
    public void testUsernameAndPasswordHandler() throws UnsupportedCallbackException
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
            fail();
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
            fail();
        }
        catch (UnsupportedCallbackException expected)
        {
            // OK
        }
    }
}
