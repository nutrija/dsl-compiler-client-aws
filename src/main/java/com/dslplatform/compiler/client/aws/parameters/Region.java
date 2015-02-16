package com.dslplatform.compiler.client.aws.parameters;

import com.amazonaws.regions.Regions;
import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

public class Region implements CompileParameter {

    public final static Region INSTANCE = new Region();

    private static final String CACHE_KEY = "AWS_REGION";

    @Override
    public String getAlias() {
        return "aws_region";
    }

    @Override
    public String getUsage() {
        return "value";
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (!context.contains(AwsDeploy.INSTANCE))
            return true;

        String defaultRegion = Regions.EU_WEST_1.getName();
        String region = context.get(INSTANCE);
        if (region == null)
            region = context.load(CACHE_KEY);
        if (region == null)
            region = defaultRegion;

        while (true) {
            try {
                Regions.fromName(region);
                context.log("Using region " + region);
                context.put(CACHE_KEY, region);
                return true;
            } catch (IllegalArgumentException e) {
                context.error("Unrecognized region!");
                region = context.ask(String.format("Enter region [%s]", defaultRegion));
                if (region.isEmpty())
                    region = defaultRegion;
            }
        }
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "AWS region";
    }

    @Override
    public String getDetailedDescription() {
        return null;
    }

    public static Regions getValue(Context context) {
        return Regions.fromName(context.get(INSTANCE));
    }
}
