package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class DbUsername implements CompileParameter {

    public final static DbUsername INSTANCE = new DbUsername();

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            context.put(INSTANCE, "revenj");
        }
        return true;
    }

    @Override
    public String getAlias() {
        return "aws_db_user";
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "RDS database username.";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
