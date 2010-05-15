/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.util.log.Logger;

/**
 * Server connection factory based on the MEJB API.
 * 
 * @version $Id$
 */
public class MEJBMBeanServerConnectionFactory implements MBeanServerConnectionFactory,
    CallbackHandler
{
    /**
     * Default JOnAS initial context factory.
     */
    public static final String DEFAULT_JNDI_INITIAL_CTX_FACTORY =
        "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * Default provider URI.
     */
    public static final String DEFAULT_PROVIDER_URI = "rmi://localhost:1099";

    /**
     * Default path for MEJB on JNDI.
     */
    public static final String DEFAULT_JNDI_MEJB_PATH = "ejb/mgmt/MEJB";

    /**
     * Default JAAS file name.
     */
    public static final String DEFAULT_JAAS_FILE = "jaas.config";

    /**
     * The logger.
     */
    protected Logger logger;

    /**
     * MEJB JNDI path.
     */
    protected String mejbJndiPath;

    /**
     * The initial context to use.
     */
    protected String initialContextFactory;

    /**
     * JAAS configuration file.
     */
    protected String jaasFile;

    /**
     * JAAS entry to use.
     */
    protected String jaasEntry;

    /**
     * User name to use.
     */
    protected String username;

    /**
     * Password to use.
     */
    protected String password;

    /**
     * The JNDI context.
     */
    protected Context jndiContext;

    /**
     * MEJB proxy.
     */
    protected static class MEJBProxy implements MBeanServerConnection
    {
        /**
         * Management MBean.
         */
        protected Management mejb;

        /**
         * Parent MEJB MBean connection factory object.
         */
        protected MEJBMBeanServerConnectionFactory parent;

        /**
         * Constructor.
         * 
         * @param mejb Management MBean.
         * @param parent Parent MEJB MBean connection factory object.
         */
        public MEJBProxy(Management mejb, MEJBMBeanServerConnectionFactory parent)
        {
            super();
            this.mejb = mejb;
            this.parent = parent;
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#addNotificationListener(ObjectName,
         *      NotificationListener, NotificationFilter,Object)
         */
        public void addNotificationListener(ObjectName arg0, NotificationListener arg1,
            NotificationFilter arg2, Object arg3) throws InstanceNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#addNotificationListener(ObjectName,
         *      ObjectName, NotificationFilter,Object)
         */
        public void addNotificationListener(ObjectName arg0, ObjectName arg1,
            NotificationFilter arg2, Object arg3) throws InstanceNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#createMBean(String, ObjectName)
         */
        public ObjectInstance createMBean(String arg0, ObjectName arg1) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#createMBean(String, ObjectName, ObjectName)
         */
        public ObjectInstance createMBean(String arg0, ObjectName arg1, ObjectName arg2)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanException,
            NotCompliantMBeanException, InstanceNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#createMBean(String, ObjectName, Object[],
         *      String[])
         */
        public ObjectInstance createMBean(String arg0, ObjectName arg1, Object[] arg2,
            String[] arg3) throws ReflectionException, InstanceAlreadyExistsException,
            MBeanException, NotCompliantMBeanException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#createMBean(String, ObjectName, ObjectName
         *      ,Object[], String[])
         */
        public ObjectInstance createMBean(String arg0, ObjectName arg1, ObjectName arg2,
            Object[] arg3, String[] arg4) throws ReflectionException,
            InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getAttribute(ObjectName, String)
         */
        public Object getAttribute(ObjectName arg0, String arg1) throws MBeanException,
            AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException
        {
            return mejb.getAttribute(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getAttributes(ObjectName, String[])
         */
        public AttributeList getAttributes(ObjectName arg0, String[] arg1)
            throws InstanceNotFoundException, ReflectionException, IOException
        {
            return mejb.getAttributes(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getDefaultDomain()
         */
        public String getDefaultDomain() throws IOException
        {
            return mejb.getDefaultDomain();
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getDomains()
         */
        public String[] getDomains() throws IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getMBeanCount()
         */
        public Integer getMBeanCount() throws IOException
        {
            return mejb.getMBeanCount();
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getMBeanInfo(ObjectName)
         */
        public MBeanInfo getMBeanInfo(ObjectName arg0) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException
        {
            return mejb.getMBeanInfo(arg0);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getObjectInstance(ObjectName)
         */
        public ObjectInstance getObjectInstance(ObjectName arg0) throws InstanceNotFoundException,
            IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#invoke(ObjectName, String, Object[],
         *      String[])
         */
        public Object invoke(ObjectName arg0, String arg1, Object[] arg2, String[] arg3)
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException
        {
            return mejb.invoke(arg0, arg1, arg2, arg3);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#isInstanceOf(ObjectName, String)
         */
        public boolean isInstanceOf(ObjectName arg0, String arg1) throws InstanceNotFoundException,
            IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#isRegistered(ObjectName)
         */
        public boolean isRegistered(ObjectName arg0) throws IOException
        {
            return mejb.isRegistered(arg0);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#queryMBeans(ObjectName, QueryExp)
         */
        public Set queryMBeans(ObjectName arg0, QueryExp arg1) throws IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#queryNames(ObjectName, QueryExp)
         */
        public Set queryNames(ObjectName arg0, QueryExp arg1) throws IOException
        {
            return mejb.queryNames(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#removeNotificationListener(ObjectName,
         *      ObjectName)
         */
        public void removeNotificationListener(ObjectName arg0, ObjectName arg1)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#removeNotificationListener(ObjectName,
         *      NotificationListener)
         */
        public void removeNotificationListener(ObjectName arg0, NotificationListener arg1)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#removeNotificationListener(ObjectName,
         *      ObjectName, NotificationFilter, Object)
         */
        public void removeNotificationListener(ObjectName arg0, ObjectName arg1,
            NotificationFilter arg2, Object arg3) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#removeNotificationListener(ObjectName,
         *      NotificationListener, NotificationFilter, Object )
         */
        public void removeNotificationListener(ObjectName arg0, NotificationListener arg1,
            NotificationFilter arg2, Object arg3) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#setAttribute(ObjectName, Attribute)
         */
        public void setAttribute(ObjectName arg0, Attribute arg1) throws InstanceNotFoundException,
            AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
            ReflectionException, IOException
        {
            mejb.setAttribute(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#setAttributes(ObjectName, AttributeList)
         */
        public AttributeList setAttributes(ObjectName arg0, AttributeList arg1)
            throws InstanceNotFoundException, ReflectionException, IOException
        {
            return mejb.setAttributes(arg0, arg1);
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#unregisterMBean(ObjectName)
         */
        public void unregisterMBean(ObjectName arg0) throws InstanceNotFoundException,
            MBeanRegistrationException, IOException
        {
            throw new UnsupportedOperationException("MEJB proxy does not support this method call");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory#getServerConnection()
     */
    public MBeanServerConnection getServerConnection(RuntimeConfiguration configuration)
        throws Exception
    {
        return createConnection(readConfiguration(configuration));
    }

    /**
     * Fills the private variables based on the runtime configuration.
     * 
     * @param configuration Runtime configuration.
     * 
     * @return Properties to use for the InitialContext.
     */
    protected Hashtable readConfiguration(RuntimeConfiguration configuration)
    {
        logger = configuration.getLogger();

        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        String jndiUrl = configuration.getPropertyValue(RemotePropertySet.URI);

        mejbJndiPath = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JNDI_PATH);
        initialContextFactory = configuration
            .getPropertyValue(JonasPropertySet.JONAS_MEJB_JNDI_INIT_CTX_FACT);

        if (jndiUrl == null || jndiUrl.trim().length() == 0)
        {
            jndiUrl = DEFAULT_PROVIDER_URI;

            String port = configuration.getPropertyValue(GeneralPropertySet.RMI_PORT);
            if (port != null)
            {
                jndiUrl = jndiUrl.replace("1099", port);
            }

            String hostname = configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
            if (hostname != null)
            {
                jndiUrl = jndiUrl.replace("localhost", hostname);
            }
        }

        if (mejbJndiPath == null)
        {
            mejbJndiPath = DEFAULT_JNDI_MEJB_PATH;
        }

        if (initialContextFactory == null)
        {
            initialContextFactory = DEFAULT_JNDI_INITIAL_CTX_FACTORY;
        }

        Hashtable props = new Hashtable();
        props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        props.put(Context.PROVIDER_URL, jndiUrl);

        if (username != null && username.trim().length() > 0 && password != null
            && password.trim().length() > 0)
        {
            this.username = username;
            this.password = password;
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, password);
            logger.debug("Credentials will be used when doing JNDI lookups", this.getClass()
                .getName());
        }
        else
        {
            logger.debug("No credentials will be used when doing JNDI lookups", this.getClass()
                .getName());
        }

        jaasFile = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JAAS_FILE);
        jaasEntry = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY);

        if (jaasEntry == null)
        {
            if (jaasFile == null)
            {
                logger.debug("No JAAS options will be used when doing EJB calls", this.getClass()
                    .getName());
            }
            else
            {
                throw new IllegalArgumentException("The " + JonasPropertySet.JONAS_MEJB_JAAS_FILE
                    + " option has been set without any " + JonasPropertySet.JONAS_MEJB_JAAS_ENTRY);
            }
        }
        else
        {
            if (jaasFile == null)
            {
                jaasFile = DEFAULT_JAAS_FILE;
            }
            logger.debug("JAAS options will be used when doing EJB calls, based on the file \""
                + jaasFile + "\" and entry \"" + jaasEntry + "\"", this.getClass().getName());
        }

        return props;
    }

    /**
     * Create the MBean server proxy connection.
     * 
     * @param props Properties to use for the InitialContext.
     * 
     * @throws Exception If anything fails.
     * 
     * @return The MEJB proxy.
     */
    protected MBeanServerConnection createConnection(Hashtable props) throws Exception
    {
        if (jaasEntry != null)
        {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            System.setProperty("java.security.auth.login.config", jaasFile);
            new LoginContext(jaasEntry, this).login();
        }

        jndiContext = new InitialContext(props);
        Object objref = jndiContext.lookup(mejbJndiPath);
        ManagementHome home = (ManagementHome) PortableRemoteObject.narrow(objref,
            javax.management.j2ee.ManagementHome.class);

        final Management mejb = home.create();
        return new MEJBProxy(mejb, this);

        // TODO: think about logout ?
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory#destroy()
     */
    public void destroy()
    {
        if (jndiContext != null)
        {
            try
            {
                jndiContext.close();
            }
            catch (Exception e)
            {
                e.getMessage();
            }
            jndiContext = null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(Callback[])
     */
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++)
        {
            Callback callback = callbacks[i];
            if (callback instanceof NameCallback)
            {
                if (this.username == null)
                {
                    throw new NullPointerException("User name not set. Please set it using the \""
                        + RemotePropertySet.USERNAME + "\" option.");
                }

                ((NameCallback) callback).setName(this.username);
                logger.debug("Responded to a NameCallback", this.getClass().getName());
            }
            else if (callback instanceof PasswordCallback)
            {
                if (this.password == null)
                {
                    throw new NullPointerException("Password not set. Please set it using the \""
                        + RemotePropertySet.PASSWORD + "\" option.");
                }

                ((PasswordCallback) callback).setPassword(this.password.toCharArray());
                logger.debug("Responded to a PasswordCallback", this.getClass().getName());
            }
            else
            {
                throw new UnsupportedCallbackException(callback, "Unsupported callback type: "
                    + callback.getClass().getName());
            }
        }
    }
}
