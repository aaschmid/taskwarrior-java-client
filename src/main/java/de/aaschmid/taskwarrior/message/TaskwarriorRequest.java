package de.aaschmid.taskwarrior.message;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.aaschmid.taskwarrior.internal.ManifestHelper;
import de.aaschmid.taskwarrior.task.TaskwarriorTask;

public class TaskwarriorRequest {

    public static enum Type {
        SYNC("sync"),
        STATISTICS("statistics");

        public final String value;

        private Type(String value) {
            this.value = value;
        }
    }

    public static enum Protocol {
        V1("v1");

        public final String value;

        private Protocol(String value) {
            this.value = value;
        }
    }

    public static String DEFAULT_CLIENT = "taskwarrior-java-api " + ManifestHelper.getImplementationVersionFromManifest("local-dev");

    private final Type type;
    private final Protocol protocol;
    private final String client;

    private final UUID syncKey;
    private final List<TaskwarriorTask> tasks;

    public TaskwarriorRequest(Type type, Protocol protocol, String client, UUID syncKey, List<TaskwarriorTask> tasks) {
        this.type = type;
        this.protocol = protocol;
        this.client = client;
        this.syncKey = syncKey;
        this.tasks = tasks;
    }
    public TaskwarriorRequest(Type type, Protocol protocol, UUID syncKey, List<TaskwarriorTask> tasks) {
        this(type, protocol, DEFAULT_CLIENT, syncKey, tasks);
    }

    public TaskwarriorRequest(Type type, Protocol protocol, String client) {
        this(type, protocol, client, null, Collections.emptyList());
    }

    public TaskwarriorRequest(Type type, Protocol protocol) {
        this(type, protocol, DEFAULT_CLIENT, null, Collections.emptyList());
    }

    public Type getType() {
        return type;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getClient() {
        return client;
    }

    public UUID getSyncKey() {
        return syncKey;
    }

    public List<TaskwarriorTask> getTasks() {
        return tasks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((syncKey == null) ? 0 : syncKey.hashCode());
        result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TaskwarriorRequest other = (TaskwarriorRequest) obj;
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (protocol != other.protocol) {
            return false;
        }
        if (syncKey == null) {
            if (other.syncKey != null) {
                return false;
            }
        } else if (!syncKey.equals(other.syncKey)) {
            return false;
        }
        if (tasks == null) {
            if (other.tasks != null) {
                return false;
            }
        } else if (!tasks.equals(other.tasks)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TaskwarriorRequest [type=" + type + ", protocol=" + protocol + ", client=" + client + ", syncKey=" + syncKey + ", tasks="
                + tasks + "]";
    }
}
