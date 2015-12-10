cd('/JMSSystemResource/@cargo.resource.jms.module.id@/JmsResource/NO_NAME_0')
myq=create('@cargo.resource.id@','Queue')
myq.setJNDIName('@cargo.resource.name@')
myq.setSubDeploymentName('@cargo.resource.jms.subdeployment.id@')