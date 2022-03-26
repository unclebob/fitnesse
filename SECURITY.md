# Security Policy

The FitNesse community takes security bugs seriously. Thank you for improving the security of FitNesse.
We appreciate your efforts and responsible disclosure and will make every effort to acknowledge your contributions.

Security issues will be fixed by creating a new release, in general older versions will not receive patches.

## Reporting a Vulnerability

To report a security vulnerability, please email: fitnesse-security *'at'* hsac *'dot'* nl.

Report security bugs in third-party modules to the person or team maintaining the module.

## Comments on this Policy

If you have suggestions on how FitNesse's security policy could be improved please submit a pull request.

# Using FitNesse Safely

FitNesse is intended to be used as part of a software development tool set. It allows a user to compose web pages containing any Javascript code, and to run acceptance tests which may include the execution of any runnable code. Therefore we recommend the following:
* FitNesse should be run in a secure sandboxed development environment.
* FitNesse should be available only to trusted, knowledgeable, and professional members of the development team.
* FitNesse should not be exposed on any public-facing servers.
* FitNesse should not have access to any production environments. If testing with production data is desired, a copy of the data should be made to a secure sandboxed test environment.
* When running FitNesse on a local machine, the `-lh` command line argument will ensure only connections from the same machine are accepted. 

