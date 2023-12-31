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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.jboss.internal.AbstractJBoss5xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * JBoss 5.1.x standalone local configuration.
 */
public class JBoss51xStandaloneLocalConfiguration
    extends AbstractJBoss5xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see AbstractJBoss5xStandaloneLocalConfiguration#AbstractJBoss5xStandaloneLocalConfiguration(String)
     */
    public JBoss51xStandaloneLocalConfiguration(String dir)
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

        // Unfortunately, in JBoss 5.1 the jboss.web:service=WebServer for HTTP doesn't have
        // a specific bindingName; as a result we need to look for port 8080
        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss.web:service=WebServer']/parent::bean"
                + "/property[@name='port' and text()='8080']", null,
            ServletPropertySet.PORT);

        addXmlReplacement(
            "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml",
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' "
                    + "and text()='jboss:service=invoker,type=pooled']/parent::bean"
                + "/property[@name='port']", null,
            JBossPropertySet.JBOSS_INVOKER_POOL_PORT);

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

}
