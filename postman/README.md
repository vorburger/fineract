# Apache Fineract Postman Collection

This [Postman](https://www.postman.com) Collection serves several purposes:

1. Illustrate the Fineract REST API by collecting example HTTP requests that are
   very easy to try out and inspect - for _humans_ to learn about using Fineract
   using the Postman UI for API testing.

1. Automatically run this (same!) collection in the project's continous integration (CI) pipeline
   ([currently on Travis CI](https://travis-ci.org/github/apache/fineract)) - for _machines_  to
   provide non-regressing testing that these examples really do and keep working.  (You can very
   easily locally run it on the CLI as well; see below.)

1. _TODO [FINERACT-1238](https://issues.apache.org/jira/browse/FINERACT-1238)_:
   For _"bulk data"_ generation, useful both for local development environments,
   as well as demo servers such as e.g. our https://www.fineract.dev.

1. _TODO [FINERACT-1170](https://issues.apache.org/jira/browse/FINERACT-1170): **Maybe**
   for load/scale testing_.


## Import into Postman UI

Use _[Import > Folder](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-data-into-postman)_
to import the Postman JSON files from this directory into your local Postman tool.


## Export from Postman UI

_TODO_


## Running on command line

We use [Newman](https://github.com/postmanlabs/newman), the Postman CLI runner.  Try it like this:

    [`./test`](test) ENVIRONMENT

where _ENVIRONMENT_ is either [`localhost`](localhost.postman_environment.json) or [`fineract.dev`](fineract.dev.postman_environment.json) (or the prefix of any other
[`*.postman_environment.json` file](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#exporting-environments)
in this directory).

This same script is also what [runs Postman on Travis](../.travis.yml).
[We run Newman in a container](https://learning.postman.com/docs/running-collections/using-newman-cli/newman-with-docker/)
as that's simplest (using [the `postman/newman` image from Docker Hub](https://hub.docker.com/r/postman/newman/).


## PS: ACK

We were inspired by our friends from https://github.com/mojaloop/postman when we originally set this up!
