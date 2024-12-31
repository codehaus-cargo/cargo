/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.AbstractJBoss5xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jboss.internal.JBoss6xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * JBoss 6.x standalone local configuration.
 */
public class JBoss6xStandaloneLocalConfiguration
    extends AbstractJBoss5xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss6xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractJBoss5xStandaloneLocalConfiguration#AbstractJBoss5xStandaloneLocalConfiguration(String)
     */
    public JBoss6xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and text()='jboss:service=Naming']/parent::bean"
                + "/property[@name='bindingName' and text()='Port']/parent::bean"
                + "/property[@name='port']", null,
            GeneralPropertySet.RMI_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and text()='jboss:service=Naming']/parent::bean"
                + "/property[@name='bindingName' and text()='RmiPort']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_NAMING_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss:service=WebService']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_CLASSLOADING_WEBSERVICE_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='UnifiedInvokerConnector']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss:service=invoker,type=jrmp']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_JRMP_INVOKER_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and "
                    + "text()='jboss.remoting:service=JMXConnectorServer,protocol=rmi']"
                        + "/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_JRMP_PORT);

        setProperty(JBossPropertySet.JBOSS_JMX_PORT, "1091");
        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and "
                    + "text()='jboss.remoting:service=JMXConnectorServer,protocol=rmiServer']"
                        + "/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_JMX_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss.web:service=WebServer']/parent::bean"
                + "/property[@name='bindingName' and text()='HttpConnector']/parent::bean"
                + "/property[@name='port']", null,
            ServletPropertySet.PORT);

        setProperty(JBossPropertySet.JBOSS_AJP_PORT, "8009");
        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss.web:service=WebServer']/parent::bean"
                + "/property[@name='bindingName' and text()='AjpConnector']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_AJP_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and text()='TransactionManager']/parent::bean"
                + "/property[@name='bindingName' and text()='recoveryManager']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and text()='TransactionManager']/parent::bean"
                + "/property[@name='bindingName' "
                    + "and text()='transactionStatusManager']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT);

        addXmlReplacement(
            "deploy/ejb3-connectors-jboss-beans.xml",
            "//deployment/bean[@name='org.jboss.ejb3.RemotingConnector']/property/value-factory"
                + "/parameter[last()]", null,
            JBossPropertySet.JBOSS_EJB3_REMOTING_PORT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

}
