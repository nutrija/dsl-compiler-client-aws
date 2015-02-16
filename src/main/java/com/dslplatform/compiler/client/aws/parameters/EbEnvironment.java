package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class EbEnvironment implements CompileParameter {

    public final static EbEnvironment INSTANCE = new EbEnvironment();

    @Override
    public String getAlias() {
        return "aws_eb_env";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            String defaultName = context.get(EbApplication.INSTANCE)+"-env";
            context.put(INSTANCE, defaultName);
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "AWS Elastic Beanstalk environment name";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
