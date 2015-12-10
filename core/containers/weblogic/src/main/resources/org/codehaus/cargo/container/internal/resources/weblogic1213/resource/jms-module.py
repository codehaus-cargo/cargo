cd('/')
create('@cargo.resource.id@', 'JMSSystemResource')
cd('/')
assign('JMSSystemResource', '@cargo.resource.id@', 'Target', '@cargo.weblogic.server@')