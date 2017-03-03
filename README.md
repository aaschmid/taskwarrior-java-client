[![Travis CI](https://travis-ci.org/aaschmid/taskwarrior-java-client.png?branch=master)](https://travis-ci.org/aaschmid/taskwarrior-java-client)
[![CircleCI](https://circleci.com/gh/aaschmid/taskwarrior-java-client.svg?style=svg)](https://circleci.com/gh/aaschmid/taskwarrior-java-client)
[![codecov](https://codecov.io/gh/aaschmid/taskwarrior-java-client/branch/master/graph/badge.svg)](https://codecov.io/gh/aaschmid/taskwarrior-java-client)
[![codebeat](https://codebeat.co/badges/90f3d360-88bb-4040-b8b6-2e3e684f11f4)](https://codebeat.co/projects/github-com-aaschmid-taskwarrior-java-client-master)
[![License](https://img.shields.io/github/license/aaschmid/taskwarrior-java-client.svg)](https://github.com/aaschmid/taskwarrior-java-client/blob/master/LICENSE.md)
[![Issues](https://img.shields.io/github/issues/aaschmid/taskwarrior-java-client.svg)](https://github.com/aaschmid/taskwarrior-java-client/issues)

taskwarrior-java-client
=======================

#### Table of Contents
* [What is it](#what-is-it)
* [Motivation and distinction](#motivation-and-distinction)
* [Requirements](#requirements)
* [Download](#download)
* [Usage example](#usage-example)
* [Release notes](#release-notes)


What is it
----------

A Java client to communicate with a [taskwarrior][] server (=
[taskd](https://taskwarrior.org/docs/taskserver/why.html)).

[taskwarrior]: https://taskwarrior.org/


Motivation and distinction
--------------------------

The current taskwarrior Android app does not satisfy my requirements. Therefore
I created this client library to integrate it into my prefered task app.  And I
also want to share it with everybody who will love to use it.


Requirements
-----------

* JDK 8
* [Jackson](https://github.com/FasterXML/jackson) via Gradle to (un-)marshal JSON


Download
--------

Currently there is no released version available but feel free to clone / fork
and build it yourself. If you would love to see this on [Maven
Central](http://search.maven.org/) feel free to create an issue.


Usage example
-------------

For example using it with [Java](https://www.java.com/):


```java
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.aaschmid.taskwarrior.config.*;
import de.aaschmid.taskwarrior.internal.ManifestHelper;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;

public class Taskwarrior {

    private static final URL PROPERTIES_TASKWARRIOR = Taskwarrior.class.getResource("/taskwarrior.properties");

    public static void main(String[] args) throws Exception {
        if (PROPERTIES_TASKWARRIOR == null) {
            throw new IllegalStateException(
                    "No 'taskwarrior.properties' found on Classpath. Create it by copy and rename 'taskwarrior.properties.template'. Also fill in proper values.");
        }
        TaskwarriorConfiguration config = new TaskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);

        TaskwarriorClient client = new TaskwarriorClient(config);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TYPE, "statistics");
        headers.put(HEADER_PROTOCOL, "v1");
        headers.put(HEADER_CLIENT, "taskwarrior-java-client " + ManifestHelper.getImplementationVersionFromManifest("local-dev"));

        TaskwarriorMessage response = client.sendAndReceive(new TaskwarriorMessage(headers));
        System.out.println(response);
    }
}
```

or

```java
import java.net.URL;

import de.aaschmid.taskwarrior.config.*;
import de.aaschmid.taskwarrior.message.*;
import de.aaschmid.taskwarrior.message.TaskwarriorRequest.*;

public class Taskwarrior {

    private static final URL PROPERTIES_TASKWARRIOR = Taskwarrior.class.getResource("/taskwarrior.properties");

    public static void main(String[] args) throws Exception {
        if (PROPERTIES_TASKWARRIOR == null) {
            throw new IllegalStateException(
                    "No 'taskwarrior.properties' found on Classpath. Create it by copy and rename 'taskwarrior.properties.template'. Also fill in proper values.");
        }
        TaskwarriorConfiguration config = new TaskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);

        TaskwarriorClient client = new TaskwarriorClient(config);

        TaskwarriorMessage request = TaskwarriorMessageFactory.messageFor(new TaskwarriorRequest(Type.STATISTICS, Protocol.V1));
        TaskwarriorResponse response = TaskwarriorMessageFactory.responseFor(client.sendAndReceive(request));
        System.out.println(response);
    }
}
```

Used `taskwarrior.properties` can be created by copying and adjusting
[`src/main/resources/taskwarrior.properties.template`](https://github.com/aaschmid/taskwarrior-java-client/tree/master/src/main/resources/taskwarrior.properties.template).



Testing
-------

To run tests manually you will need to build and run taskwarrior server container.
[See here how](docker/README.md)


Keys formats
------------

Unfortunately Java only has an encoded key spec for a private key in [PKCS#8](https://en.wikipedia.org/wiki/PKCS_8)
format. However, [taskd](https://taskwarrior.org/docs/taskserver/setup.html) generates the private key in
[PKCS#1](https://en.wikipedia.org/wiki/PKCS_1) format if you follow the
[documentation](https://taskwarrior.org/docs/taskserver/user.html). The tranformation command below also converts the
key from [PEM](https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail) to
[DER](https://en.wikipedia.org/wiki/X.690#DER_encoding) format which does not need any further transformation as
handling of the base64 encoded PEM keys.

```sh
openssl pkcs8 -topk8 -nocrypt -in $TASKD_GENERATED_KEY.key.pem -inform PEM -out $KEY_NAME.key.pkcs8.der -outform DER
```

Release notes
-------------

Releases and Release Notes are availabe [here](/aaschmid/taskwarrior-java-client/releases).
