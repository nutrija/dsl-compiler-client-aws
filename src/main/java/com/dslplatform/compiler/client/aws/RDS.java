package com.dslplatform.compiler.client.aws;

import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.*;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.aws.parameters.*;

import java.net.SocketTimeoutException;

public class RDS {

    public static DBInstance getInstance(Context context) {
        AmazonRDSClient client = new AmazonRDSClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));
        try {
            DescribeDBInstancesResult describeResponse = client.describeDBInstances(
                    new DescribeDBInstancesRequest()
                            .withDBInstanceIdentifier(context.get(DbInstance.INSTANCE)));
            return describeResponse.getDBInstances().get(0);
        } catch (DBInstanceNotFoundException e) {
            context.log("Existing database not found");
            return null;
        }
    }

    public static DBInstance launchInstance(Context context) throws ExitException {
        String instanceId = context.get(DbInstance.INSTANCE);

        AmazonRDSClient client = new AmazonRDSClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));

        // @todo ask user for options
        // lowest cost options
        CreateDBInstanceRequest createRequest = new CreateDBInstanceRequest()
                .withAllocatedStorage(5) // 5GB is minimum
                .withPort(5432)
                .withDBName(context.get(DbDatabase.INSTANCE))
                .withMasterUsername(context.get(DbUsername.INSTANCE))
                .withMasterUserPassword(context.get(DbPassword.INSTANCE))
                .withEngine("postgres")
                .withEngineVersion("9.3.3")
                .withStorageType("standard")
                .withDBInstanceClass("db.t1.micro")
                .withDBInstanceIdentifier(instanceId)
                .withPubliclyAccessible(true);

        // TODO make sure db is publicly accessible
        // 1) create appropriate security group
        // 2) a) running VPC (default, or when starting in new region) -> add VPC security group
        //    .withVpcSecurityGroupIds("group-name")
        //    b) no-VPC
        //    .withDBSecurityGroups("group-name")
        //  security group example: allow inbound TCP traffic: 0.0.0.0/0 5432

        try {
            context.log("Sending create-db-instance request for id: " + instanceId);
            DBInstance db = client.createDBInstance(createRequest);
            context.show("Creating Postgresql RDS instance, this can take a few minutes...");
            return db;
        } catch (DBInstanceAlreadyExistsException e) {
            context.error(String.format("Database instance '%s' already exists.", instanceId));
            throw new ExitException();
        }
    }

    public static DBInstance waitDbReady(Context context) throws ExitException {
        int pollInterval = 15000;
        context.log("Waiting for database instance to be ready");
        AmazonRDSClient client = new AmazonRDSClient(Credentials.getValidCredentials(context))
                .withRegion(Region.getValue(context));
        while (true) {
            DescribeDBInstancesResult describeResponse = client.describeDBInstances(
                    new DescribeDBInstancesRequest().withDBInstanceIdentifier(context.get(DbInstance.INSTANCE)));

            if (describeResponse.getDBInstances().size() == 0) {
                context.error("Database creation failed! Database instance not found.");
                throw new ExitException();
            }
            DBInstance db = describeResponse.getDBInstances().get(0);

            if (db.getDBInstanceStatus().toLowerCase().contains("available")) {
                context.show("Database instance '" + context.get(DbInstance.INSTANCE) + "' created");
                Endpoint endpoint = db.getEndpoint();
                if (endpoint != null)
                    context.show("Database available at endpoint: " + endpoint.getAddress()+":"+endpoint.getPort());
                return db;
            }
            context.log("Status: " + db.getDBInstanceStatus());

            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                context.error(e);
                throw new ExitException();
            }
        }
    }
}
