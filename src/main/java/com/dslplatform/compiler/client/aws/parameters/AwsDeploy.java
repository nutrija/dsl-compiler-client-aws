package com.dslplatform.compiler.client.aws.parameters;

import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.Endpoint;
import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.aws.EB;
import com.dslplatform.compiler.client.aws.RDS;
import com.dslplatform.compiler.client.parameters.DbConnection;
import com.dslplatform.compiler.client.parameters.Migration;
import java.io.File;

public class AwsDeploy implements CompileParameter {

    public final static AwsDeploy INSTANCE = new AwsDeploy();

    public AwsDeploy() {};

    @Override
    public String getAlias() { return "aws"; }

    @Override
    public String getUsage() { return null; }

    @Override
    public boolean check(Context context) throws ExitException {
        return true;
    }

    @Override
    public String getShortDescription() {
        return "Deploy to Amazon Web Services";
    }

    @Override
    public String getDetailedDescription() {
        return "Deploy Revenj http server and Postgres to AWS.\n"+
                "1. Launches a new RDB instance and applies migration.\n"+
                "2. Launches a Container instance with Revenj running in Docker container";
    }

    @Override
    public void run(Context context) throws ExitException {
        if (!context.contains(INSTANCE))
            return ;

        DBInstance db = RDS.getInstance(context);
        if (db == null) {
            RDS.launchInstance(context);
            db = RDS.waitDbReady(context);
        }

        Endpoint endpoint = db.getEndpoint();
        String address = endpoint.getAddress();
        String port = endpoint.getPort().toString();
        String database = context.get(DbDatabase.INSTANCE);
        String user = context.get(DbUsername.INSTANCE);
        String password = context.get(DbPassword.INSTANCE);
        String connString = String.format("server=%s;port=%s;database=%s;user=%s;password=%s;encoding=unicode",
                address, port, database, user, password);
        String jdbcConnString = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",
                address, port, database, user, password);

        context.show("Database connection string:");
        context.show(connString);

        /* // offline migration needs fixing
        String sql = context.contains(DslCompiler.INSTANCE)
                ? Migration.INSTANCE.offlineMigration(context, DbConnection.getDatabaseDslAndVersion(context, connString))
                : Migration.INSTANCE.onlineMigration(context, DbConnection.getDatabaseDslAndVersion(context, connString));
        */
        String sql = Migration.INSTANCE.onlineMigration(
                context, DbConnection.getDatabaseDslAndVersion(context, jdbcConnString));
        context.show("Applying migration...");
        DbConnection.execute(context, sql, jdbcConnString);
        context.show("Database migrated.");

        File zipFile = SourcePath.packSourceBundle(context, connString);

        EB.createApplication(context);
        EB.createEnvironment(context);
        S3Location sourceBundle = EB.uploadSourceBundle(context, zipFile);
        EB.createApplicationVersion(context, sourceBundle);
    }

}
