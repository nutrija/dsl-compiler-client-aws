package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class AccessKey implements CompileParameter {

    public final static AccessKey INSTANCE = new AccessKey();

    public AccessKey() {}

    @Override
    public String getAlias() {
        return "aws_access";
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
        return "AWS secret access key";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }
}
