package com.dslplatform.compiler.client.aws.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;

public class ProjectName implements CompileParameter {

    public final static ProjectName INSTANCE = new ProjectName();

    @Override
    public String getAlias() {
        return "aws_app";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (context.contains(AwsDeploy.INSTANCE) && !context.contains(INSTANCE)) {
            String currentDir = System.getProperty("user.dir");
            String defaultName = currentDir.substring(currentDir.lastIndexOf(File.separator) + 1);
            context.show("Project name was not provided (-aws_project=name). Using current directory '"+defaultName+"' as name.");
            context.put(INSTANCE, defaultName);
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "AWS project name. Used as prefix for RDS and EB instance names.";
    }

    @Override
    public String getDetailedDescription() {
        return "AWS project name. Used as prefix for RDS and EB instance names.";
    }
}
