# Conductor Exporter

This tool receives as input a workflow name, and a range of times, to export Conductor Workflows into any format, e.g. csv. 

Historically, this tool run to only generate CSV, but then I needed other formats, like series for each task, and decided to extend this tool for supporting custom 
external exporters, which can be added using configuration.


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

`./gradlew bootRun --args="--startTime=1621518731077 --endTime=1621518838410"`
   
endTime is optional, ignore it to use "now".

3. test with:

`./gradlew test`

# Custom exporters

Assuming the reader has cloned this repo, the cleanest way to incorporate a custom exporter is using SPI, and requires:

1. external exporters should implement com.invitae.conductor.exporter.ExportService Interface
1. in META-INF/services add one text line for each provider implementation with its canonical class name

For example, in com/invitae/conductor/exporter/summary/Summarizer.kt there is an example of exporter, which can be 
enabled by uncommenting first line in src/main/resources/META-INF/services/com.invitae.conductor.exporter.ExportService.
Afterwards, run the application and instead of the CSV files you should get a summary in standard output.

Readers with knowledge of Spring-Boot Bean Management can modify com.invitae.conductor.exporter.Configuration to inject
their implementations. 

# Contributing

1. Communicate with marco.crasso@invitae.com to avoid duplicating efforts.
1. Create an issue in the repo with: Context, Goal. The goal should be good enough to infer the acceptance criteria.
1. Checkout from main branch.
1. Install githook with `brew install githooks`.
1. Install pre-commit with: `./gradlew installGitHooks`.

