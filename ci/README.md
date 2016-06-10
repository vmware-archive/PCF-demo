# Concourse CI Pipeline

This project provides a [Concourse](https://concourse.ci/) pipeline based on the
[Versioned S3 Artifacts](https://concourse.ci/versioned-s3-artifacts.html) example
modeled on a fairly common real-world use case of pushing tested, built artifacts
into S3 buckethttp://concourse.ci/vagrant.html end-to-end scenario is to monitor a Git repository for commits, and when new
commits are detected, run its unit tests.

If the unit tests pass, the pipeline will then create a new release candidate
artifact with automated versioning, which then will be placed in a S3 bucket. From
there, the pipeline will run integration tests against the release candidate.

These tasks comprise the automated continuous integration part of the pipeline. Once
a candidate-release is properly vetted, the "ship-it" task can be manually invoked to
put the final-release version in a different S3 bucket.

## Prerequisites

Here are a few things you'll need:

- GitHub Account (always free and you *should* already have one!)
- Concourse (see [Concourse Setup](http://concourse.ci/vagrant.html) to run locally)
- [Amazon S3](https://aws.amazon.com/s3) compatible storage (use the real thing or try [fake-s3](https://hub.docker.com/r/lphoward/fake-s3/) to run locally)
- Cloud Foundry (see [Pivotal Web Services](http://run.pivotal.io/) for a free 60
day trial, or [PCF Dev](http://pivotal.io/pcf-dev) to run locally)

## Concourse Setup

If you have an existing Concourse CI system setup, skip to the next section.

> NOTE: The pipeline and scripts used in this project have been tested on Concourse
**v1.2.0**.  If you experience any problems, please ensure you are running a current version of Concourse, and remember to `fly -t (alias) sync` !

Otherwise if you just want to quickly start up Concourse on your local machine you
can use the pre-built [Vagrant](https://www.vagrantup.com/) box:

```
$ mkdir vagrant-lite
$ cd vagrant-lite
$ vagrant init concourse/lite # creates ./Vagrantfile
$ vagrant up                  # downloads the box and spins up the VM
```

The web server will be running at http://192.168.100.4:8080

Next, download the Fly CLI from the main page where there are links to binaries
for common platforms.

> If you're on Linux or OS X, you will have to `chmod +x` the downloaded
binary and put it in your `$PATH`. This can be done in one fell swoop with
(example) `install fly /usr/local/bin`.

## Fork it

You should be working with your own [forked copy](https://help.github.com/articles/fork-a-repo/)
of the PCF-demo repository so you can do cool things like watch the pipeline kick-off
when you push changes to your repo.

```
$ git clone https://github.com/YOUR_USERNAME/PCF-demo.git
```

## Executing Tasks Locally

One of the great things about Concourse is the ability to run tasks locally before
committing your code (saves all those debug commits when something is configured
differently between your local and remote setup).

For example, you can execute the unit tests like this:

```
$ cd PCF-demo
$ fly -t lite execute -c ci/tasks/unit.yml -i pcfdemo=.
```

Your files will be uploaded and the task will be executed with them.

> Normally the working directory name will be used as the input name. Since ours
does not match (the folder is named `PCF-demo` but the input name is `pcfdemo`),
we must specify `-i pcfdemo=.` to point to the current directory.

This works very well for our `unit` task because its only required input is the project
folder.  To run the `build` task we need an additional input for the file containing
our version number.  We can mock this up by:

```
$ mkdir -p target/version
$ echo "1.0.0-rc.1" > target/version/number
```

Now we can execute our `build` task and specify the version folder as an input.

```
$ fly -t lite execute -c ci/tasks/build.yml -i pcfdemo=. -i version=target/version
```

## Configuring your pipeline

So far we have only run our tasks locally (which is pretty cool) but we still don't
have a pipeline deployed to Concourse yet.  The next section shows how to do that
but before we get there we need to configure our properties file which Concourse
will use to resolve placeholders in our `pipeline.yml` file.

The provided `ci/pcfdemo-properties-sample.yml` file will serve as a starting point
for configuring our pipeline.  You'll want to copy this file to a folder outside
of your project (*don't check it into version control!*).

```
$ mkdir ~/.concourse
$ cp ci/pcfdemo-properties-sample.yml ~/.concourse/pcfdemo-properties.yml
$ chmod 600 ~/.concourse/pcfdemo-properties.yml
```

Next, you'll modify the `~/.concourse/pcfdemo-properties.yml` file and configure it
according to your target environment.

> Be sure to create the S3 buckets and your Cloud Foundry Org and Space before running
your pipeline!

## Setting your pipeline

Now that you've got everything setup, and run your tasks locally to make sure they
work, it's time to set the pipeline in Concourse.  First, let's make sure we have
targetted and logged into our Concourse installation:

```
$ fly -t lite login -c http://192.168.100.4:8080/
```

Next we can set our pipeline:
```
$ fly -t lite set-pipeline -p pcfdemo -c ci/pipeline.yml -l ~/.concourse/pcfdemo-properties.yml
```

If you refresh the Concourse [main page](http://192.168.100.4:8080/), you should now see the `pcfdemo` pipeline. By
default, all new pipelines are in a paused state.  To unpause it, you can either
reveal the pipelines sidebar via the hamburger icon in the top left and press
"play", or run:

```
$ fly -t lite unpause-pipeline -p pcfdemo
```

After a few seconds (up to 60) you'll see the pipeline kick-off the `unit-test`
task.  Once this completes, it will move along through the rest of the pipeline.
This pipeline is fully automated from the `unit-test` through the `integration-test`
tasks.

Concourse will continue to monitor your Github fork for changes and kick-off the
pipeline accordingly!

## Shipping the final product

In this example our end goal is to produce a 'final' artifact and store this in an
S3 bucket for later distribution.  This action is performed by the manual `ship-it`
task.

To trigger this task from the UI, click on the `ship-it` task and then click the
`+` icon. Otherwise you can trigger the `ship-it` task from the command line:

```
$ fly -t lite trigger-job -j pcfdemo/ship-it
```

This will take the last release candidate artifact, rename it to a release version, and
upload it to the S3 bucket.  It will also bump the patch version number for future
builds.

## Managing version numbers

The above process completely automates the bumping of the patch version. When it
finally comes time to bump the minor or major version, you'll manually trigger the
`minor` or `major` tasks.  The next time the pipeline is triggered, the artifact
will be build using the new version values.
