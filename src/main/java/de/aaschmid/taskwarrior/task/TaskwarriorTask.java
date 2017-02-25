package de.aaschmid.taskwarrior.task;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

// codebeat:disable[TOO_MANY_FUNCTIONS,TOO_MANY_IVARS]
public class TaskwarriorTask {

    private static final String PATTERN_ZONED_DATE_TIME = "yyyyMMdd'T'HHmmssX";

    public static enum Status {
        @JsonProperty("pending")
        PENDING,
        @JsonProperty("completed")
        COMPLETED,
        @JsonProperty("deleted")
        DELETED,
        @JsonProperty("waiting")
        WAITING,
        @JsonProperty("recurring")
        RECURRING;
    }

    public static enum Priority {
        LOW,
        MEDIUM,
        HIGH,
    }

    public static class Annotation {
        @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
        private ZonedDateTime entry;
        private String description;

        public ZonedDateTime getEntry() {
            return entry;
        }

        public void setEntry(ZonedDateTime entry) {
            this.entry = entry;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((entry == null) ? 0 : entry.hashCode());
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
            Annotation other = (Annotation) obj;
            if (description == null) {
                if (other.description != null) {
                    return false;
                }
            } else if (!description.equals(other.description)) {
                return false;
            }
            if (entry == null) {
                if (other.entry != null) {
                    return false;
                }
            } else if (!entry.equals(other.entry)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Annotation [entry=" + entry + ", description=" + description + "]";
        }
    }

    private Status status;
    private UUID uuid;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime entry;
    private String description;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime start;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime end;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime due;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime until;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime wait;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime modified;
    @JsonFormat(pattern = PATTERN_ZONED_DATE_TIME)
    private ZonedDateTime scheduled;
    private String recur;
    private String mask;
    private int imask;
    private UUID parent;
    private String project;
    private Priority priority;
    private String depends;
    private List<String> tags;
    private List<Annotation> annotations;

    public static String getPatternZonedDateTime() {
        return PATTERN_ZONED_DATE_TIME;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ZonedDateTime getEntry() {
        return entry;
    }

    public void setEntry(ZonedDateTime entry) {
        this.entry = entry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }

    public ZonedDateTime getDue() {
        return due;
    }

    public void setDue(ZonedDateTime due) {
        this.due = due;
    }

    public ZonedDateTime getUntil() {
        return until;
    }

    public void setUntil(ZonedDateTime until) {
        this.until = until;
    }

    public ZonedDateTime getWait() {
        return wait;
    }

    public void setWait(ZonedDateTime wait) {
        this.wait = wait;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public void setModified(ZonedDateTime modified) {
        this.modified = modified;
    }

    public ZonedDateTime getScheduled() {
        return scheduled;
    }

    public void setScheduled(ZonedDateTime scheduled) {
        this.scheduled = scheduled;
    }

    public String getRecur() {
        return recur;
    }

    public void setRecur(String recur) {
        this.recur = recur;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public int getImask() {
        return imask;
    }

    public void setImask(int imask) {
        this.imask = imask;
    }

    public UUID getParent() {
        return parent;
    }

    public void setParent(UUID parent) {
        this.parent = parent;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    // codebeat:disable[ABC]
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
        result = prime * result + ((depends == null) ? 0 : depends.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((due == null) ? 0 : due.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((entry == null) ? 0 : entry.hashCode());
        result = prime * result + imask;
        result = prime * result + ((mask == null) ? 0 : mask.hashCode());
        result = prime * result + ((modified == null) ? 0 : modified.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result + ((recur == null) ? 0 : recur.hashCode());
        result = prime * result + ((scheduled == null) ? 0 : scheduled.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result + ((until == null) ? 0 : until.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        result = prime * result + ((wait == null) ? 0 : wait.hashCode());
        return result;
    }
    // codebeat:enable[ABC]

    // codebeat:disable[ABC,CYCLO,LOC]
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
        TaskwarriorTask other = (TaskwarriorTask) obj;
        if (annotations == null) {
            if (other.annotations != null) {
                return false;
            }
        } else if (!annotations.equals(other.annotations)) {
            return false;
        }
        if (depends == null) {
            if (other.depends != null) {
                return false;
            }
        } else if (!depends.equals(other.depends)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (due == null) {
            if (other.due != null) {
                return false;
            }
        } else if (!due.equals(other.due)) {
            return false;
        }
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }
        if (entry == null) {
            if (other.entry != null) {
                return false;
            }
        } else if (!entry.equals(other.entry)) {
            return false;
        }
        if (imask != other.imask) {
            return false;
        }
        if (mask == null) {
            if (other.mask != null) {
                return false;
            }
        } else if (!mask.equals(other.mask)) {
            return false;
        }
        if (modified == null) {
            if (other.modified != null) {
                return false;
            }
        } else if (!modified.equals(other.modified)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (project == null) {
            if (other.project != null) {
                return false;
            }
        } else if (!project.equals(other.project)) {
            return false;
        }
        if (recur == null) {
            if (other.recur != null) {
                return false;
            }
        } else if (!recur.equals(other.recur)) {
            return false;
        }
        if (scheduled == null) {
            if (other.scheduled != null) {
                return false;
            }
        } else if (!scheduled.equals(other.scheduled)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (tags == null) {
            if (other.tags != null) {
                return false;
            }
        } else if (!tags.equals(other.tags)) {
            return false;
        }
        if (until == null) {
            if (other.until != null) {
                return false;
            }
        } else if (!until.equals(other.until)) {
            return false;
        }
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        if (wait == null) {
            if (other.wait != null) {
                return false;
            }
        } else if (!wait.equals(other.wait)) {
            return false;
        }
        return true;
    }
    // codebeat:enable[ABC,CYCLO,LOC]

    @Override
    public String toString() {
        return "TaskwarriorTask [status=" + status + ", uuid=" + uuid + ", entry=" + entry + ", description=" + description + ", start="
                + start + ", end=" + end + ", due=" + due + ", until=" + until + ", wait=" + wait + ", modified=" + modified
                + ", scheduled=" + scheduled + ", recur=" + recur + ", mask=" + mask + ", imask=" + imask + ", parent=" + parent
                + ", project=" + project + ", priority=" + priority + ", depends=" + depends + ", tags=" + tags + ", annotations="
                + annotations + "]";
    }
}
// codebeat:enable[TOO_MANY_FUNCTIONS,TOO_MANY_IVARS]
