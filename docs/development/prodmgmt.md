Fineract Open Product Management
================================

This document outlines some principles we use to help us prioritize the work
towards which we direct our volunteer contributors and participants in programs
such as Google Summer of Code or Outreachy.

While each contributor's personal interest (and skillset) obviously varies,
here we attempt to identify work that has the most "impact" for the project overall.

We use the ASF JIRA as a tool, and have
[a nice Dashboard for Fineract developers](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335824).

We also have [a Dashboard for Fineract Release Management](https://issues.apache.org/jira/secure/Dashboard.jspa?selectPageId=12335825),
and use it to apply the policies detailed below, specifically we watch out for:

1. No Fixed without Fix Version (more important for actually Fixed, less important for Duplicate, Invalid, etc.)
1. No Blocker New Features or Improvements, typically; also Blocker Sub-Tasks are less likely - typically Bugs
1. No Issue Types other than New Feature, Enhancement, Bug, Sub-task; no Wishes, nor Tasks, nor Tests
1. No Resolve Issues with Fix Version other than already released or next to release version
1. No Unresolved Issues with already released Fix Version

We use JIRA's Priority as single dimension, and do not use a Severity field.
Labels on issue are used informationally only, and are not an extra dimension to an issue's priority
(so avoid using tags such as "p1" etc.).

Following are our categories of issues, in order of priority for the project.


A. Clear Technical, and a few select functional, obvious bugs (Priority: Blocker, Type: Bug)
------------------------------------------------------------------------

Fixing open bug reports with stack traces clearly illustrating known problems in the code
are the project's highest priority.  (Issues with completely unclear bug reports should be
closed.  Issues that are not obvious to reproduce, but clearly indicate underlying problems
should be left to be further investigated.)

Fixing and re-activating [failing integration tests](../../README.md#pull-requests) which
had to `@Ignore`-d are considered part of this category, because they could hide bugs.

These "Category A" issues typically hold up (block) our [release process](../../README.md#releasing).
They therefore MUST be "actionable" for active contributors.


B. Making Fineract easier to contribute to
------------------------------------------

Fineract's community will scale better if we make it easier to contribute to.

Fixing (or ignoring) failing tests reduce a source of new contributors frustration about failing PRs.

Automated Code Quality Tooling that is fully integrated into the build process for each PR
can help to significantly reduce the effort required for manual code review by maintainers,
and lead to a more "self service" contribution model.

Increasing test coverage gives maintainers more confidence for proposed changed to existing code.


C. Making Fineract easier to use
---------------------------------

Proper Error handling for Operator ease.

Swagger support for easier API consumption.

https://www.fineract.dev Demo Server maintenance could be considered part of this.

Release Management.


D. New Functional Features (Priority: Major or Minor, Type: New Feature)
------------------------------------------------------------------------

Best in partnership with real end-user stakeholder!

In this project, functional New Features or Improvements will typically never have priority Blocker,
as they would never "hold up" a release (they just "slip" into future releases), like bugs possibly could.


E. Technical Maintenance
------------------------

Third-party dependencies upgrades, major Java version changes, etc.


Q. Performance
--------------

Make Fineract scale.  Less of a top priority as long as there are open lists of clear technical and functional open bugs.

First build out proper performance measurement test suite, then (gradually) improve upon initial results.

See the JIRA [Performance](https://jira.apache.org/jira/issues/?jql=project%20%3D%20FINERACT%20AND%20component%20%3D%20Performance) component.
