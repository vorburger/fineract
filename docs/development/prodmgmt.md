Fineract Open Product Management
================================

This document outlines some principles we use to help us prioritize the work
towards which we direct our volunteer contributors and participants in programs
such as Google Summer of Code or Outreachy.

While each contributor's personal interest (and skillset) obviously varies,
here we attempt to identify work that has the most "impact" for the project overall.


A. Clear Technical, and a few select functional, obvious bugs (Priority: Blocker, Type: Bug)
------------------------------------------------------------------------

Fixing open bug reports with stack traces clearly illustrating known problems in the code
are the project's highest priority.  (Issues with completely unclear such bug reports should
be closed.  Issues that are not obvious to reproduce, but clearly indicate underlying problems
should be left to be further investigated.)

Fixing and re-activating [failing integration tests](../../README.md#pull-requests) which
had to `@Ignore`-d are considered part of this category, because they could hide bugs.


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


E. Technical Maintenance
------------------------

Third-party dependencies upgrades, major Java version changes, etc.


Q. Performance
--------------

Make Fineract scale.  Less of a top priority as long as there are open lists of clear technical and functional open bugs.

First build out proper performance measurement test suite, then (gradually) improve upon initial results.

See the JIRA [Performance](https://jira.apache.org/jira/issues/?jql=project%20%3D%20FINERACT%20AND%20component%20%3D%20Performance) component.
