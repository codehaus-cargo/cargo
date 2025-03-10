"""
  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
"""

scope = AdminConfig.getid('/Node:@cargo.websphere.node@')
sibQueueName='sib.@cargo.resource.id@'

createSIBQueue('none', '@cargo.websphere.node@', '@cargo.websphere.server@', sibQueueName, '@cargo.resource.jms.sibus.id@')
createSIBJMSQueue('@cargo.resource.id@', '@cargo.resource.name@', '', sibQueueName, scope)

# create activation specification
jmsActivationName='A.@cargo.resource.id@'
jmsActivationJndiName='jms/activation/@cargo.resource.id@'
createSIBJMSActivationSpec(jmsActivationName, jmsActivationJndiName, '@cargo.resource.name@', 'Queue', '', '', '@cargo.resource.jms.sibus.id@', scope)
