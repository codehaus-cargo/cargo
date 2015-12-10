cd('/JMSSystemResource/@cargo.resource.jms.module.id@/JmsResource/NO_NAME_0')
cf=create('@cargo.resource.id@','ConnectionFactory')
cf.setJNDIName('@cargo.resource.name@')
cf.setSubDeploymentName('@cargo.resource.jms.subdeployment.id@')

cd('ConnectionFactories/@cargo.resource.id@')
tp=create('@cargo.resource.id@','TransactionParams')
tp.setXAConnectionFactoryEnabled(true)