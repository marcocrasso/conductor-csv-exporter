# Conductor CSV Exporter

This tool receives as input a workflow name, and a range of times in Epoch, to generate two CSV files, one with 
workflows execution details and another with their tasks data.

## JVM requirements

Officially supported version: 11

```
brew install java11
<<follow brew post install instructions>>
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home
```

## Quickstart
1. configure your env with:
```
export CONDUCTOR_WORKFLOW_NAME=your_wf_name
export CONDUCTOR_WORKFLOW_VERSION=your_wf_version (Optional, default is 1)
export CONDUCTOR_URL=http://[user:password]@host:port/api/ (Don't forget the /api/ )
```
2. run with:

`./gradlew bootRun --args="--startTime=1621518731077 --endTime=1621518838410 --append=false"`
   
endTime is optional, ignore it to use "now". To change target files names use --tasksFile and --workflowsFile.

3. test with:

`./gradlew test`


# Contributing

1. Communicate with marco.crasso@invitae.com to avoid duplicating efforts.
1. Create an issue in the repo with: Context, Goal. The goal should be good enough to infer the acceptance criteria.
1. Checkout from main branch.
1. Install githook with `brew install githooks`.
1. Install pre-commit with: `./gradlew installGitHooks`.
