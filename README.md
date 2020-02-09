[![Travis CI](https://travis-ci.org/aaschmid/taskwarrior-java-client.png?branch=master)](https://travis-ci.org/aaschmid/taskwarrior-java-client)
[![CircleCI](https://circleci.com/gh/aaschmid/taskwarrior-java-client.svg?style=svg)](https://circleci.com/gh/aaschmid/taskwarrior-java-client)
[![codebeat](https://codebeat.co/badges/90f3d360-88bb-4040-b8b6-2e3e684f11f4)](https://codebeat.co/projects/github-com-aaschmid-taskwarrior-java-client-master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.aaschmid/taskwarrior-java-client/badge.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22de.aaschmid%22%20AND%20a%3A%22taskwarrior-java-client%22)
[![License](https://img.shields.io/github/license/aaschmid/taskwarrior-java-client.svg)](https://github.com/aaschmid/taskwarrior-java-client/blob/master/LICENSE)
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

A Java client to communicate with a [taskwarrior][] server (= [taskd](https://taskwarrior.org/docs/taskserver/why.html)).

[taskwarrior]: https://taskwarrior.org/


Motivation and distinction
--------------------------

The current taskwarrior Android app does not satisfy my requirements. Therefore I created this client library to
integrate it into my preferred task app. And I also want to share it with everybody who will love to use it.


Requirements
-----------

* JDK 8


Download
--------

Currently there is no released version available but feel free to clone / fork and build it yourself. If you would
love to see this on [Maven Central](http://search.maven.org/) feel free to create an issue.

Usage example
-------------

For example using it with [Java](https://www.java.com/):


```java
import java.io.IOException;
import java.net.URL;

import de.aaschmid.taskwarrior.TaskwarriorClient;
import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.MessageType;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader;

import static de.aaschmid.taskwarrior.config.TaskwarriorConfiguration.taskwarriorPropertiesConfiguration;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessage.taskwarriorMessage;
import static de.aaschmid.taskwarrior.message.TaskwarriorRequestHeader.taskwarriorRequestHeaderBuilder;

class Taskwarrior {

    private static final URL PROPERTIES_TASKWARRIOR = Taskwarrior.class.getResource("/taskwarrior.properties");

    public static void main(String[] args) {
        if (PROPERTIES_TASKWARRIOR == null) {
            throw new IllegalStateException(
                    "No 'taskwarrior.properties' found on Classpath. Create it by copy and rename 'taskwarrior.properties.template'. Also fill in proper values.");
        }
        TaskwarriorConfiguration config = taskwarriorPropertiesConfiguration(PROPERTIES_TASKWARRIOR);

        TaskwarriorRequestHeader header = taskwarriorRequestHeaderBuilder().authentication(config).type(MessageType.STATISTICS).build();
        TaskwarriorMessage message = taskwarriorMessage(header.toMap());

        TaskwarriorClient client = new TaskwarriorClient(config);

        TaskwarriorMessage response = client.sendAndReceive(message);
        System.out.println(response);
    }
}
```

Used `taskwarrior.properties` can be created by copying and adjusting
[`src/main/resources/taskwarrior.properties.template`](https://github.com/aaschmid/taskwarrior-java-client/tree/master/src/main/resources/taskwarrior.properties.template).

Testing
-------

To run tests manually you will need to build and run taskwarrior server container. [See here how](docker/taskd/README.md).


Keys formats
------------

| Key specification | [PEM]() format¹ | [DER]() format |
| ----------------- |:---------------:|:--------------:|
| [PKCS#1]()        | yes             |                |
| [PKCS#8]()        | yes             | yes            |

¹: The kind of format is currently detected by file extentions.

Note: Keys can be transformed using `openssl`, e.g. from [PKCS#8]() in [PEM]() format to [PKCS#1]() in [DER]() format:

```sh
openssl pkcs8 -topk8 -nocrypt -in $TASKD_GENERATED_KEY.key.pem -inform PEM -out $KEY_NAME.key.pkcs8.der -outform DER
```

[PKCS#1]: https://en.wikipedia.org/wiki/PKCS_1
[PKCS#8]: https://en.wikipedia.org/wiki/PKCS_8
[PEM]: https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail
[DER]: https://en.wikipedia.org/wiki/X.690#DER_encoding


Release notes
-------------

Releases and Release Notes are available [here](https://github.com//aaschmid/taskwarrior-java-client/releases).
