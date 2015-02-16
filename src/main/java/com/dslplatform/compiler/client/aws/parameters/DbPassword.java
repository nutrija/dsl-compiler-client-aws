package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;
import java.util.Random;

public class DbPassword implements CompileParameter {

    public final static DbPassword INSTANCE = new DbPassword();

    @Override
    public String getAlias() {
        return "aws_db_password";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            context.put(INSTANCE, getRandomPassword(10));
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "AWS RDS database instance master password.";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }

    private String getRandomPassword(int length) {
        final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for(int i=0; i<length; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
