cd('/SecurityConfiguration/@cargo.weblogic.domain.name@/Realms/myrealm/AuthenticationProviders/DefaultAuthenticator')

if not cmo.groupExists('@cargo.weblogic.group@'):
    cmo.createGroup('@cargo.weblogic.group@','@cargo.weblogic.group@')