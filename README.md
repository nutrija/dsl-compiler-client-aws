# AWS plugin for DSL Platform command-line client

This plugin allows easy command-line deployment of DSL Platform stack to Amazon Web Services.
Currently Revenj HTTP server and Postgres deployment are supported.

## Installation and usage

Download latest [DSL Platform command-line client (dsl-clc) release](https://github.com/ngs-doo/dsl-compiler-clientreleases).

Download latest release of [AWS plugin] (https://github.com/ngs-doo/dsl-compiler-client/releases)..

Place both jars in same folder.

From your shell, run:

$ java -jar dsl-clc.jar -aws_deploy -aws_src=[path]

Deployment is done to Elastic Beanstalk and Amazon RDS.

More details and instructions coming soon...
