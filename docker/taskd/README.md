Task server container
=====================

We use the predefined [connectical/taskd](https://hub.docker.com/r/connectical/taskd) container for running a
[taskd server](https://taskwarrior.org/docs/taskserver/setup.html) for integration testing.


# Run

You can run the container from the **repo root folder** using

```sh
docker run -d --name=taskd -p 53589:53589 -v $(pwd)/docker/taskd:/var/taskd connectical/taskd
```

Task server listens on port `53589`. The command above will map the port to the same port on your `localhost`. If you
have a conflicting service listening on the same port then modify `-p` flag argument, see `run`
[documentation](https://docs.docker.com/engine/reference/run/#expose-incoming-ports)).
