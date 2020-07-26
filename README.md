# file-changes-build-strategy-plugin

This plugin implements build strategies that allows automatic builds to be skipped if a file has changed.
It supports Bitbucket and Github as branch sources.

Currently, it has a build strategy based on a MD5 hash of the file content. 
A user can configure the MD5 hash of a file and builds will only happen automatically if that file has not changed based on the MD5 hash.

Next step will be to add a new strategy based on a Git commit ID to identify a change on a particular file.
