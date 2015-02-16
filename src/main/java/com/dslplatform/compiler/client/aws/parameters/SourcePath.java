package com.dslplatform.compiler.client.aws.parameters;

import com.amazonaws.util.IOUtils;
import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.parameters.TempPath;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import org.zeroturnaround.zip.ZipUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SourcePath implements CompileParameter {

    public final static SourcePath INSTANCE = new SourcePath();

    @Override
    public String getAlias() {
        return "aws_src";
    }

    @Override
    public String getUsage() {
        return "path";
    }

    @Override
    public boolean check(Context context) throws ExitException {
        if (!context.contains(AwsDeploy.INSTANCE))
            return true;
        if (!context.contains(INSTANCE)) {
            context.error("No path to Revenj folder provided. Use -aws_src=path to specify path.");
        }
        File file = new File(context.get(INSTANCE));

        if (!file.exists()) {
            context.error("Revenj not found at: " + context.get(INSTANCE));
            return false;
        }
        if (!file.isDirectory()) {
            context.error("Revenj path was not a directory: " + context.get(INSTANCE));
            return false;
        }
        return true;
    }

    @Override
    public void run(Context context) throws ExitException {
    }

    @Override
    public String getShortDescription() {
        return "Path to Revenj folder, which must contain Revenj.Http.exe.config.";
    }

    @Override
    public String getDetailedDescription() {
        return "";
    }

    public static File packSourceBundle(Context context, String connString) throws ExitException {
        String srcPath = context.get(SourcePath.INSTANCE);
        File tempPath = TempPath.getTempProjectPath(context);
        File zipFile = new File(tempPath.getAbsolutePath(), context.get(ProjectName.INSTANCE) + "-source.zip");

        if (zipFile.exists()) {
            context.log("Found previous zip file at: " + zipFile.getAbsolutePath());
            if (!zipFile.isFile()) {
                context.error("Previous zip path is not a file!");
                throw new ExitException();
            }
            try {
                context.log("Deleting previous zip file");
                zipFile.delete();
            } catch (SecurityException e) {
                context.error("Could not delete previous zip file, access denied.");
                throw new ExitException();
            }
        }

        ZipUtil.pack(new File(srcPath), zipFile);

        String configPath = srcPath+"/Revenj.Http.exe.config";
        File config = new File(configPath);
        if (!config.exists() || !config.isFile()) {
            context.error("Could not find Revenj config file in: " + configPath);
            throw new ExitException();
        }

        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            for (String line : Files.readAllLines(config.toPath())) {
                if (line.contains("<add key=\"ConnectionString\"")) {
                    context.log("Rewriting ConnectionString to: " + connString);
                    bytes.write(("    <add key=\"ConnectionString\" value=\"" + connString + "\" />\n").getBytes());
                }
                else
                    bytes.write((line+"\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZipUtil.replaceEntry(zipFile, "Revenj.Http.exe.config", bytes.toByteArray());

        if (ZipUtil.containsEntry(zipFile, "Dockerfile")) {
            context.log("Using existing Dockerfile");
        }
        else {
            context.log("Adding Dockerfile to zip");
            try {
                String dockerfile;
                final InputStream str = INSTANCE.getClass().getResourceAsStream("Dockerfile");
                if (str != null)
                    dockerfile = IOUtils.toString(str);
                else
                    dockerfile =
                        "FROM mono:3.12.0\n"
                        +"ADD . /revenj/\n"
                        +"WORKDIR /revenj\n"
                        +"EXPOSE 8999\n"
                        +"ENTRYPOINT [\"mono\", \"Revenj.Http.exe\"]\n";
                ZipUtil.addEntry(zipFile, "Dockerfile", dockerfile.getBytes());
            } catch (IOException e) {
                context.error("Could not read Dockerfile");
                throw new ExitException();
            }
        }

        return zipFile;
    }
}
