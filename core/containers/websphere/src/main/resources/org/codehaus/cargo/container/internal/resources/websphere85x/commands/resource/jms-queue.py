scope = AdminConfig.getid('/Node:@cargo.websphere.node@')
sibQueueName='sib.@cargo.resource.id@'

createSIBQueue('none', '@cargo.websphere.node@', '@cargo.websphere.server@', sibQueueName, '@cargo.resource.jms.sibus.id@')
createSIBJMSQueue('@cargo.resource.id@', '@cargo.resource.name@', '', sibQueueName, scope)

# create activation specification
jmsActivationName='A.@cargo.resource.id@'
jmsActivationJndiName='jms/activation/@cargo.resource.id@'
createSIBJMSActivationSpec(jmsActivationName, jmsActivationJndiName, '@cargo.resource.name@', 'Queue', '', '', '@cargo.resource.jms.sibus.id@', scope)