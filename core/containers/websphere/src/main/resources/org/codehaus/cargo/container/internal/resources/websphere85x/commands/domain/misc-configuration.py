setApplicationSecurity('@cargo.websphere.security.application@')

AdminTask.configureCSIInbound('[-messageLevelAuth Supported -clientCertAuth Supported -transportLayer Supported]')
AdminTask.configureCSIOutbound('[-messageLevelAuth Supported -clientCertAuth Never -transportLayer Supported]')