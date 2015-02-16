package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class EbApplication implements CompileParameter {

    public final static EbApplication INSTANCE = new EbApplication();

    @Override
    public String getAlias() {
        return "aws_eb_app";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            String defaultName = context.get(ProjectName.INSTANCE) + "-revenj";
            context.put(INSTANCE, defaultName);
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "AWS Elastic Beanstalk application name";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
