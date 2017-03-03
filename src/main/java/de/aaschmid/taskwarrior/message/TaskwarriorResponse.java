package de.aaschmid.taskwarrior.message;

import java.util.List;
import java.util.UUID;

import de.aaschmid.taskwarrior.task.TaskwarriorTask;

public class TaskwarriorResponse {

    private final String server;
    private final int code;
    private final String status;

    private final UUID syncKey;
    private final List<TaskwarriorTask> tasks;

    public TaskwarriorResponse(String server, int code, String status, UUID syncKey, List<TaskwarriorTask> tasks) {
        this.server = server;
        this.code = code;
        this.status = status;
        this.syncKey = syncKey;
        this.tasks = tasks;
    }

    public String getServer() {
        return server;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public UUID getSyncId() {
        return syncKey;
    }

    public List<TaskwarriorTask> getTasks() {
        return tasks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + code;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((syncKey == null) ? 0 : syncKey.hashCode());
        result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
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
        TaskwarriorResponse other = (TaskwarriorResponse) obj;
        if (server == null) {
            if (other.server != null) {
                return false;
            }
        } else if (!server.equals(other.server)) {
            return false;
        }
        if (code != other.code) {
            return false;
        }
        if (status != other.status) {
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
        return true;
    }

    @Override
    public String toString() {
        return "TaskwarriorSyncResponse [server=" + server + ", code=" + code + ", status=" + status + ", syncKey=" + syncKey + ", tasks="
                + tasks + "]";
    }
}
