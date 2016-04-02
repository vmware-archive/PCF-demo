# Concourse CI Pipeline

This project provides a [Concourse](https://concourse.ci/) pipeline based on the
[Versioned S3 Artifacts](https://concourse.ci/versioned-s3-artifacts.html) example
modeled on a fairly common real-world use case of pushing tested, built artifacts
into S3 buckets.

The end-to-end scenario is to monitor a Git repository for commits, and when new
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
- Concourse (see [Concourse Setup](#concourse-setup) to run locally)
- [Amazon S3](https://aws.amazon.com/s3/pricing/) compatible storage (use the real thing or try [fake-s3](https://hub.docker.com/r/lphoward/fake-s3/) to run locally)
- Cloud Foundry (see [Pivotal Web Services](http://run.pivotal.io/) for a free 60
day trial, or [PCF Dev](http://pcfdev.io) to run locally)

## Concourse Setup

If you have an existing Concourse CI system setup, skip to the next section.

> NOTE: The pipeline and scripts used in this project have been tested on Concourse
**v1.0.0**.  If you experience any problems, please ensure you are running a current version of Concourse, and remember to `fly -t (alias) sync` !

Otherwise if you just want to quickly start up Concourse on your local machine you
can use the pre-built [Vagrant](https://www.vagrantup.com/) box:

```
vagrant init concourse/lite # creates ./Vagrantfile
vagrant up                  # downloads the box and spins up the VM
```

The web server will be running at http://192.168.100.4:8080

Next, download the Fly CLI from the main page where there are links to binaries
for common platforms.

> If you're on Linux or OS X, you will have to ```chmod +x``` the downloaded
binary and put it in your ```$PATH```. This can be done in one fell swoop with
(example) ```install fly /usr/local/bin```.

## Fork it

You should be working with your own [forked copy](https://help.github.com/articles/fork-a-repo/) of the PCF-demo repository so you
can do cool things like watch the pipeline kick-off when you push changes to your
repo.

## Configuring

The ```pipeline.yml``` file uses placeholders for external resources (ex: ```{{github-uri}}```) and credentials (ex: ```{{s3-secret-access-key}}```). These
values need to be provided to Concourse when you create or update a pipeline's
configuration.

The ```pcfdemo-properties-sample.yml``` file provides a convenient means of setting
these values.  This file should be copied to a folder outside of your project
source code:

```
mkdir ~/.concourse
cp ci/pcfdemo-properties-sample.yml ~/.concourse/pcfdemo-properties.yml
chmod 600 ~/.concourse/pcfdemo-properties.yml
```

Next, you'll modify the ```pcfdemo-properties.yml``` file and configure it
according to your target environment.

> Be sure to create the S3 buckets and your Cloud Foundry Org and Space before running your pipeline!

## Setting your pipeline

Now that you've got everything setup, and run your tasks locally to make sure they
work, it's time to set the pipeline in Concourse.  First, let's make sure we have
targetted and logged into our Concourse installation:

```
fly -t lite login -c http://192.168.100.4:8080/
```

Next we can set our pipeline:
```
fly -t lite set-pipeline -p pcfdemo -c ci/pipeline.yml -l ~/.concourse/pcfdemo-properties.yml
```

If you refresh the Concourse [main page](http://192.168.100.4:8080/), you should now see the ```pcfdemo``` pipeline. By
default, all new pipelines are in a paused state.  To unpause it, you can either
reveal the pipelines sidebar via the hamburger icon in the top left and press
"play", or run:

```
fly -t lite unpause-pipeline -p pcfdemo
```

## Executing Tasks Locally

So now you have a working pipeline, but you'd probably like to make changes to it.

One of the great things about Concourse is the ability to run tasks locally before
committing your code (saves all those debug commits when something is configured
differently between your local and remote setup).

For example, you can execute the unit tests like this:

```
fly -t lite execute -c ci/tasks/unit.yml -i pcfdemo=.
```

Your files will be uploaded and the task will be executed with them. The working
directory name will be used as the input name. If they do not match, you must
specify ```-i name=.``` instead, where name is the input name from the task
configuration.

Of course, this works very well for tasks that run isolated without requiring any
input from a previously executed task.  To run this next example, we need to mock
up a few things that would normally be performed by other tasks in the pipeline:

```
~ » cd git
~/git » mkdir ci-mock
~/git » mkdir -p ci-mock/build/pcfdemo/target && touch ci-mock/build/pcfdemo/target/pcf-demo.war
~/git » mkdir -p ci-mock/version && echo "1.0.0-rc.1" > ci-mock/version/number
~/git » fly execute -c PCF-demo/ci/tasks/prepare-build.yml -i pcfdemo=PCF-demo -i build=ci-mock/build -i version=ci-mock/version
executing build 69
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 12.1M    0 12.1M    0     0  22.8M      0 --:--:-- --:--:-- --:--:-- 22.8M  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 58.8M    0 58.8M    0     0  26.7M      0 --:--:--  0:00:02 --:--:-- 26.7M
100 10240    0 10240    0     0  11078      0 --:--:-- --:--:-- --:--:-- 11070
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 10240    0 10240    0     0  5047k      0 --:--:-- --:--:-- --:--:--  9.7M
initializing with docker:///java#8
running pcfdemo/ci/tasks/rename-artifact.sh -d build/pcfdemo/target -v version/number
srcDir=build/pcfdemo/target
Renaming build/pcfdemo/target/pcf-demo.war to pcf-demo-1.0.0-rc.1.war
succeeded
```
