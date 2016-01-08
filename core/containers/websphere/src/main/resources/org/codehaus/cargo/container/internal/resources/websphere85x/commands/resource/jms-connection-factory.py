import socket

provEndPoint=socket.getfqdn() + ':7276:BootstrapBasicMessaging'
scope = AdminConfig.getid('/Node:@cargo.websphere.node@')

createSIBJMSConnectionFactory('none', '@cargo.websphere.server@', '@cargo.resource.id@', '@cargo.resource.name@', '', 'Queue', '@cargo.resource.jms.sibus.id@', provEndPoint, scope)