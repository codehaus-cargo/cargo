security = AdminConfig.getid('/Security:/')
prop = AdminConfig.getid('/Security:/Property:@cargo.websphere.property.name@/')
if prop:
    AdminConfig.modify(prop, [['value', '@cargo.websphere.property.value@']])
else:
    AdminConfig.create('Property', security, [['name','@cargo.websphere.property.name@'], ['value','@cargo.websphere.property.value@']])
