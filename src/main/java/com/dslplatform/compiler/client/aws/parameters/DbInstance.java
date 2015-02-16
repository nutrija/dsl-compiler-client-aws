package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.util.Random;

public class DbInstance implements CompileParameter {

    public final static DbInstance INSTANCE = new DbInstance();

    @Override
    public String getAlias() {
        return "aws_db_id";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            String defaultName = context.get(ProjectName.INSTANCE) + "db";
            context.put(INSTANCE, defaultName);
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "RDS database instance ID.";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
