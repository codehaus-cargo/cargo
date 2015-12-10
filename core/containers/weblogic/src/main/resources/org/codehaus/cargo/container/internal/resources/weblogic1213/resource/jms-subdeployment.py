cd('/JMSSystemResource/@cargo.resource.jms.module.id@')
create('@cargo.resource.id@', 'SubDeployment')
cd('/')
assign('JMSSystemResource.SubDeployment', '@cargo.resource.id@', 'Target', '@cargo.resource.jms.server.id@')