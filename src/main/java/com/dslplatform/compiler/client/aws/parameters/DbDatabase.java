package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class DbDatabase implements CompileParameter {

    public final static DbDatabase INSTANCE = new DbDatabase();

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
        return "aws_db_database";
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "RDS database name.";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
