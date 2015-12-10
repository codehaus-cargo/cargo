cd('/')
create('@cargo.resource.id@', 'JMSServer')
cd('/')
assign('JMSServer', '@cargo.resource.id@', 'Target', '@cargo.weblogic.server@')