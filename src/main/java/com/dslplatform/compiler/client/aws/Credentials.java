package com.dslplatform.compiler.client.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.dslplatform.compiler.client.Context;

public class Credentials {
    public static AWSCredentials getValidCredentials(Context context) {
        AWSCredentials credentials;
        AWSCredentialsProvider provider = new DefaultAWSCredentialsProviderChain();
        try {
            credentials = provider.getCredentials();
            if (validateCredentials(credentials))
                return credentials;
            context.log("Invalid system credentials");
        } catch (AmazonClientException e) {
            context.log("System credentials not found");
        }
        String accessKey = context.load("AWS_ACCESS_KEY_ID");
        String secretKey = context.load("AWS_SECRET_KEY");
        if (accessKey != null && secretKey != null) {
            credentials = new BasicAWSCredentials(accessKey, secretKey);
            if (validateCredentials(credentials))
                return credentials;
            context.log("Invalid cached credentials");
        }
        do {
            accessKey = context.ask("Enter your AWS access key id: ");
            secretKey = context.ask("Enter your AWS secret key: ");
            credentials = new BasicAWSCredentials(accessKey, secretKey);
        } while (!validateCredentials(credentials));

        context.cache("AWS_ACCESS_KEY_ID", accessKey);
        context.cache("AWS_SECRET_KEY", secretKey);

        return credentials;
    }

    private static boolean validateCredentials(AWSCredentials credentials) {
        try {
            AmazonS3Client client = new AmazonS3Client(credentials);
            client.getS3AccountOwner();
            return true;
        } catch (AmazonS3Exception e) {
            return false;
        }
    }

}
