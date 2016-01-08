groupId = "cn=@cargo.websphere.group@,o=defaultWIMFileBasedRealm"

groups = AdminTask.searchGroups('[-cn *]')

if groups.find(groupId) < 0:
    AdminTask.createGroup('[-cn @cargo.websphere.group@]')