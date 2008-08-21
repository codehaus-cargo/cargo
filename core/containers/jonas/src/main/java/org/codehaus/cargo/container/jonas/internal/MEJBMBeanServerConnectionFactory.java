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
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;

/**
 * Server connection factory based on the MEJB API.
 * 
 * @version $Id: MEJBMBeanServerConnectionFactory.java 14700 2008-07-30 13:25:03Z alitokmen $
 */
public class MEJBMBeanServerConnectionFactory implements MBeanServerConnectionFactory
{
    /**
     * Default MEJB jndi path.
     */
    private static final String DEFAULT_JNDI_MEJB_PATH = "ejb/mgmt/MEJB";

    /**
     * Default JOnAS initial context factory.
     */
    private static final String DEFAULT_JNDI_INITIAL_CTX_FACTORY =
        "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * Default URI.
     */
    private static final String DEFAULT_URI = "rmi://localhost:1099";

    /**
     * the context.
     */
    private Context context;

    /**
     * MEJB proxy.
     */
    protected static class MEJBProxy implements MBeanServerConnection
    {
        /**
         * Default JAAS file name.
         */
        public static final String DEFAULT_JAAS_FILE = "jaas.config";

        /**
         * Management MBean.
         */
        protected Management mejb;

        /**
         * JAAS configuration file.
         */
        protected String jaasFile;

        /**
         * JAAS security role to use.
         */
        protected String jaasRole;

        /**
         * Previous value of java.security.auth.login.config.
         */
        private String previousLoginConfig;

        /**
         * MEJBProxy Constructor.
         * 
         * @param mejb Management MBean.
         * @param configuration Runtime configuration, contains for example the JAAS settings.
         */
        public MEJBProxy(Management mejb, RuntimeConfiguration configuration)
        {
            super();
            this.mejb = mejb;
            jaasFile = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JAAS_FILE);
            jaasRole = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JAAS_ROLE);
 
            if (jaasRole == null)
            {
                jaasFile = null;
            }
            else
            {
                if (jaasFile == null)
                {
                    jaasFile = DEFAULT_JAAS_FILE;
                }

                System.setProperty("java.security.auth.login.config", jaasFile);
            }
        }

        /**
         * Sets up JAAS and logs in.
         * 
         * @throws IOException If LoginException is thrown.
         */
        private void setJAAS() throws IOException
        {
            if (jaasRole != null)
            {
                previousLoginConfig = System.setProperty("java.security.auth.login.config",
                    jaasFile);
                try
                {
                    new LoginContext(jaasRole).login();
                }
                catch (LoginException e)
                {
                    throw new IOException("Failed logging in: " + e.toString());
                }
            }
        }

        /**
         * Unsets JAAS and logs out.
         * 
         * @throws IOException If LoginException is thrown.
         */
        private void unsetJAAS() throws IOException
        {
            if (jaasRole != null)
            {
                try
                {
                    new LoginContext(jaasRole).logout();
                }
                catch (LoginException e)
                {
                    throw new IOException("Failed logging in: " + e.toString());
                }
                finally
                {
                    System.setProperty("java.security.auth.login.config", previousLoginConfig);
                }
            }
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
            setJAAS();
            try
            {
                return mejb.getAttribute(arg0, arg1);
            }
            finally
            {
                unsetJAAS();
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getAttributes(ObjectName, String[])
         */
        public AttributeList getAttributes(ObjectName arg0, String[] arg1)
            throws InstanceNotFoundException, ReflectionException, IOException
        {
            setJAAS();
            try
            {
                return mejb.getAttributes(arg0, arg1);
            }
            finally
            {
                unsetJAAS();
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getDefaultDomain()
         */
        public String getDefaultDomain() throws IOException
        {
            setJAAS();
            try
            {
                return mejb.getDefaultDomain();
            }
            finally
            {
                unsetJAAS();
            }
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
            setJAAS();
            try
            {
                return mejb.getMBeanCount();
            }
            finally
            {
                unsetJAAS();
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#getMBeanInfo(ObjectName)
         */
        public MBeanInfo getMBeanInfo(ObjectName arg0) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException
        {
            setJAAS();
            try
            {
                return mejb.getMBeanInfo(arg0);
            }
            finally
            {
                unsetJAAS();
            }
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
            setJAAS();
            try
            {
                return mejb.invoke(arg0, arg1, arg2, arg3);
            }
            finally
            {
                unsetJAAS();
            }
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
            setJAAS();
            try
            {
                return mejb.isRegistered(arg0);
            }
            finally
            {
                unsetJAAS();
            }
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
            setJAAS();
            try
            {
                return mejb.queryNames(arg0, arg1);
            }
            finally
            {
                unsetJAAS();
            }
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
            setJAAS();
            try
            {
                mejb.setAttribute(arg0, arg1);
            }
            finally
            {
                unsetJAAS();
            }
        }

        /**
         * {@inheritDoc}
         * 
         * @see javax.management.MBeanServerConnection#setAttributes(ObjectName, AttributeList)
         */
        public AttributeList setAttributes(ObjectName arg0, AttributeList arg1)
            throws InstanceNotFoundException, ReflectionException, IOException
        {
            setJAAS();
            try
            {
                return mejb.setAttributes(arg0, arg1);
            }
            finally
            {
                unsetJAAS();
            }
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

        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        String jndiUrl = configuration.getPropertyValue(RemotePropertySet.URI);
        String mejbJndiPath = configuration.getPropertyValue(JonasPropertySet.JONAS_MEJB_JNDI_PATH);
        String initialContextFactory = configuration
            .getPropertyValue(JonasPropertySet.JONAS_MEJB_JNDI_INIT_CTX_FACT);

        if (jndiUrl == null || jndiUrl.trim().length() == 0)
        {
            jndiUrl = DEFAULT_URI;
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

        if (username != null && username.trim().length() > 0)
        {
            props.put(Context.SECURITY_PRINCIPAL, username);
        }

        if (password != null && password.trim().length() > 0)
        {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }

        context = new InitialContext(props);
        Object objref = context.lookup(mejbJndiPath);
        ManagementHome home = (ManagementHome) PortableRemoteObject.narrow(objref,
            javax.management.j2ee.ManagementHome.class);

        final Management mejb = home.create();

        return new MEJBProxy(mejb, configuration);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory#destroy()
     */
    public void destroy()
    {
        if (context != null)
        {
            try
            {
                context.close();
            }
            catch (Exception e)
            {
                e.getMessage();
            }
            context = null;
        }
    }
}
