userId = "uid=@cargo.websphere.user.name@,o=defaultWIMFileBasedRealm"

users = AdminTask.searchUsers('[-uid *]')

if users.find(userId) < 0:
    AdminTask.createUser('[-uid @cargo.websphere.user.name@ -password @cargo.websphere.user.password@ -sn test -cn test]')