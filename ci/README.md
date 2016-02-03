# Concourse CI Pipeline

This project provides a [Concourse](https://concourse.ci/) pipeline based on the
[Versioned S3 Artifacts](https://concourse.ci/versioned-s3-artifacts.html) example
modeled on a fairly common real-world use case of pushing tested, built artifacts
into S3 buckets.

The end-to-end scenario is to monitor a Git repository for commits, and when new
commits are detected, run its unit tests.

If the unit tests pass, the pipeline will then create a new release candidate
artifact with automated versioning, which then will be placed in a S3 bucket.

From there, the pipeline will run integration tests against the release candidate,
and if those pass, it will create a final artifact and "ship it" by putting it in
a different S3 bucket.

## Prerequisites

Here are a few things you'll need:

- GitHub Account (always free and you *should* already have one!)
- Concourse (see [Concourse Setup](#concourse-setup) to run locally)
- [Amazon S3](https://aws.amazon.com/s3/pricing/) compatible storage (use the real thing or try [fake-s3](https://hub.docker.com/r/lphoward/fake-s3/) to run locally)
- Cloud Foundry (see [Pivotal Web Services](http://run.pivotal.io/) for a free 60
day trial, or [MicroPCF](https://micropcf.io) to run locally)

## Concourse Setup

If you have an existing Concourse CI system setup, skip to the next section.
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

You should be working with your own forked copy of the PCF-demo repository so you
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
cp pcfdemo-properties-sample.yml ~/.concourse/pcfdemo-properties.yml
chmod 600 ~/.concourse/pcfdemo-properties.yml
```

Next, you'll modify the ```pcfdemo-properties.yml``` file and configure it
according to your target environment.

> Be sure to create the S3 buckets and your Cloud Foundry Org and Space before running your pipeline!

## Setting your pipeline

Now that you've got everything setup, it's time to set the pipeline in Concourse:

```
todo...
```

## Executing Tasks Locally

One of the great things about Concourse is the ability to run tasks locally before
committing your code (saves all those debug commits when something is configured
differently between your local and remote setup).

For example, you can execute the unit tests like this:

```
fly execute -c ci/tasks/unit.yml -i pcfdemo=.
```
