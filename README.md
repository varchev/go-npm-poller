# GoCD Npm Registry Poller

A [GoCD](https://www.go.cd) plugin that polls a Npm registry

[![Build Status](https://travis-ci.org/varchev/go-npm-poller.svg?branch=master)](https://travis-ci.org/varchev/go-npm-poller)

Introduction
------------
This is a [package material](https://docs.go.cd/current/extension_points/package_repository_extension.html) plugin for [GoCD](https://www.go.cd). It is currently capable of polling [Npm](https://www.npmjs.com/) registries.

The behaviour and capabilities of the plugin are determined to a significant extent by that of the package material extension point in GoCD. Be sure to read the package material documentation before using this plugin.

This is a pure Java plugin. It does not need node.js or npm installed. You may however require node.js and npm on the agents.

Installation
------------
Just drop [go-npm-poller.jar](https://github.com/varchev/go-npm-poller/releases) into plugins/external directory and restart GoCD. More details [here](https://docs.go.cd/current/extension_points/plugin_user_guide.html)

Repository definition
---------------------
![Add a Npm repository][1]

Npm registry URL must be a valid http or https URL. For example, to add npmjs.org as a repository, specify the URL as http://registry.npmjs.org. The plugin will try to access the URL to report successful connection.

Package definition
------------------
Click check package to make sure the plugin understands what you are looking for. Note that the version constraints are AND-ed if both are specified.

![Define a package as material for a pipeline][2]

Published Environment Variables
-------------------------------
The following information is made available as environment variables for tasks:

    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LABEL
    GO_REPO_<REPO-NAME>_<PACKAGE-NAME>_REPO_URL
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGE_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LOCATION
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_VERSION

The LOCATION variable points to a downloadable url.

Downloading the Package
-----------------------
To download the package locally on the agent you could use [curl](http://curl.haxx.se/) (or wget) task like this:

                <exec command="cmd" >
                <arg>/c</arg>
                <arg>curl -o /path/to/package.tgz $GO_PACKAGE_REPONAME_PKGNAME_LOCATION</arg>
                </exec>

When the task executes on the agent, the environment variables get subsituted and the package gets downloaded.

Alternatively, you could choose to *npm install* the package like:

                <exec command="cmd" >
                <arg>/c</arg>
                <arg>npm install $GO_PACKAGE_REPONAME_PKGNAME_PACKAGE_ID@$GO_PACKAGE_REPONAME_PKGNAME_VERSION </arg>
                </exec>
                

[1]: doc/npm-repo.png  "Define Npm Package Repository"
[2]: doc/npm-add-pkg.png  "Define package as material for a pipeline"
