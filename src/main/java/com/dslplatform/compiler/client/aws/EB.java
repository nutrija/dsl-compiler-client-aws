package com.dslplatform.compiler.client.aws;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.*;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.aws.parameters.EbApplication;
import com.dslplatform.compiler.client.aws.parameters.EbEnvironment;
import com.dslplatform.compiler.client.aws.parameters.Region;

import java.io.File;
import java.util.Date;

public class EB {

    public static EnvironmentDescription waitEnviromentReady(
            Context context
            ) throws ExitException {
        context.show("Waiting for Elastic Beanstalk environment to be launched. This could take a few minutes...");
        String appName = context.get(EbApplication.INSTANCE);
        String envName = context.get(EbEnvironment.INSTANCE);
        AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));
        try {
            while (true) {
                DescribeEnvironmentsResult describeRes = client.describeEnvironments(new DescribeEnvironmentsRequest()
                        .withEnvironmentNames(envName)
                        .withApplicationName(appName));
                Thread.sleep(5000);
                String status = describeRes.getEnvironments().get(0).getStatus();
                if (status.toLowerCase().contains("ready")) {
                    context.show(String.format("Environment %s/%s is ready.", appName, envName));
                    return describeRes.getEnvironments().get(0);
                }
            }
        } catch (InterruptedException e) {
            context.error(e.getMessage());
            throw new ExitException();
        }
    }


    public static ApplicationDescription createApplication(Context context) throws ExitException {
        String appName = context.get(EbApplication.INSTANCE);

        final AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));

        final DescribeApplicationsResult describeRes = client.describeApplications(
                new DescribeApplicationsRequest()
                        .withApplicationNames(appName));

        if (describeRes.getApplications().size() > 0) {
            context.show(String.format("Application '%s' found, using existing application", appName));
            return describeRes.getApplications().get(0);
        }

        try {
            CreateApplicationResult result = client.createApplication(
                    new CreateApplicationRequest()
                            .withApplicationName(appName));
            context.show("Created application: " + appName);
            return result.getApplication();
        } catch (TooManyApplicationsException e) {
            context.error("Too many applications");
            throw new ExitException();
        }
    }

    public static EnvironmentDescription createEnvironment(Context context) throws ExitException {
        String appName = context.get(EbApplication.INSTANCE);
        String envName = context.get(EbEnvironment.INSTANCE);

        final AWSElasticBeanstalkClient client = new AWSElasticBeanstalkClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));

        final DescribeEnvironmentsResult describeRes = client.describeEnvironments(new DescribeEnvironmentsRequest()
                .withEnvironmentNames(envName)
                .withApplicationName(appName));

        if (describeRes.getEnvironments().size() > 0) {
            context.show(String.format("Environment '%s/%s' found, using existing environment", appName, envName));
            final EnvironmentDescription envDesc = describeRes.getEnvironments().get(0);
            if (describeRes.getEnvironments().get(0).getStatus().toLowerCase().contains("ready"))
                return envDesc;
            return waitEnviromentReady(context);
        }

        try {
            CreateEnvironmentResult result = client.createEnvironment(
                    new CreateEnvironmentRequest()
                            .withEnvironmentName(envName)
                            .withApplicationName(appName)
                            .withSolutionStackName("64bit Amazon Linux 2014.09 v1.1.0 running Docker 1.3.3"));
            context.show("Launching Amazon Elastic Beanstalk environment. This could take a few minutes...");
        } catch (TooManyEnvironmentsException e) {
            context.error(e.getMessage());
            throw new ExitException();
        } catch (InsufficientPrivilegesException e) {
            context.error(e.getMessage());
            throw new ExitException();
        }
        return waitEnviromentReady(context);
    }

    public static String createApplicationVersion(
            Context context,
            S3Location sourceBundle) {
        String appName = context.get(EbApplication.INSTANCE);
        String envName = context.get(EbEnvironment.INSTANCE);
        final String version = Long.toString(new Date().getTime());

        AWSElasticBeanstalkClient ebClient = new AWSElasticBeanstalkClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));

        ebClient.createApplicationVersion(
                new CreateApplicationVersionRequest()
                        .withApplicationName(appName)
                        .withSourceBundle(sourceBundle)
                        .withVersionLabel(version)
                        .withAutoCreateApplication(true)
        );
        context.show("New application version created: " + version);

        ebClient.updateEnvironment(
                new UpdateEnvironmentRequest()
                        .withEnvironmentName(envName)
                        .withVersionLabel(version)
        );
        context.show("Updating environment. This could take a few minutes...");

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DescribeEnvironmentsResult describeResult = ebClient.describeEnvironments(new DescribeEnvironmentsRequest()
                    .withApplicationName(appName)
                    .withEnvironmentNames(envName)
                    .withVersionLabel(version)
            );
            String status = describeResult.getEnvironments().get(0).getStatus().toLowerCase();
            context.log(status);
            if (status.contains("ready")) {
                context.show("Environment ready at endpoint: " + describeResult.getEnvironments().get(0).getEndpointURL());
                return version;
            }
        }
    }

    public static S3Location uploadSourceBundle(Context context, File zipFile) {
        String s3key = "revenj-1";

        AWSElasticBeanstalkClient ebClient = new AWSElasticBeanstalkClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));

        CreateStorageLocationResult res = ebClient.createStorageLocation(
                new CreateStorageLocationRequest());
        String s3bucket = res.getS3Bucket();
        context.show("Created S3 storage: "+s3bucket);

        AmazonS3Client s3Client = new AmazonS3Client(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));


        context.show("Uploading source bundle...");
        context.log("Uploading from " +zipFile.getAbsolutePath());
        PutObjectResult putResult = s3Client.putObject(new PutObjectRequest(
                s3bucket, s3key, zipFile));
        context.show("Source bundle uploaded");

        return new S3Location(s3bucket, s3key);
    }
}
