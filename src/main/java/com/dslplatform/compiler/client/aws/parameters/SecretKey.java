package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class SecretKey implements CompileParameter {

    public final static AccessKey INSTANCE = new AccessKey();

    @Override
    public String getAlias() {
        return "aws_secret";
    }

    @Override
    public String getUsage() {
        return "value";
    }

    @Override
    public boolean check(Context context) throws ExitException {
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
