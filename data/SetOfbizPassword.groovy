import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec

// Update password for OFBIZ user to be properly hashed
ec.service.sync().name("org.moqui.impl.UserServices.update#Password")
    .parameters([userId:'OFBIZ', newPassword:'heber', newPasswordVerify:'heber'])
    .call()
