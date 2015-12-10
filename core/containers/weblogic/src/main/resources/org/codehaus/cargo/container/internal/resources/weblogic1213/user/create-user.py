cd('/SecurityConfiguration/@cargo.weblogic.domain.name@/Realms/myrealm/AuthenticationProviders/DefaultAuthenticator')

if not cmo.userExists('@cargo.weblogic.user.name@'):
    cmo.createUser('@cargo.weblogic.user.name@','@cargo.weblogic.user.password@','@cargo.weblogic.user.name@')