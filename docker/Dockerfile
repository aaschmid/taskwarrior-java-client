FROM alpine:latest
MAINTAINER Ihor Antonov <ihor@antonovs.family>

# Configuration steps closely follow official taskd manual:
# https://taskwarrior.org/docs/taskserver/configure.html

# Taskwarrior server vars
ENV TASKDDATA=/var/lib/taskd
ENV TASKDLISTEN=localhost:53589
ENV TASKDLOG=/var/log/taskd.log
ENV TASKDUSER=taskd
ENV TASKDGROUP=taskd

# Taskwarrior user vars
ENV TASKORG=Public
ENV TASKUSERID=111-222-333

EXPOSE 53589

RUN apk add --no-cache taskd task \
 && mkdir -p $TASKDDATA/orgs/$TASKORG/users/$TASKUSERID \
 && touch $TASKDLOG \
 && chown $TASKDUSER:$TASKDGROUP $TASKDLOG \
 && chown -R $TASKDUSER:$TASKDGROUP $TASKDDATA

# Copy pre-created certificates
COPY --chown=$TASKDUSER:$TASKDGROUP \
  certs/client.cert.pem \
  certs/client.key.pem \
  certs/server.cert.pem \
  certs/server.key.pem \
  certs/ca.cert.pem \
  certs/ca.key.pem \
  server/config \
  $TASKDDATA/

# Put some initial data for the user
COPY --chown=$TASKDUSER:$TASKDGROUP \ 
  user/config \
  user/tx.data \
  $TASKDDATA/orgs/$TASKORG/users/$TASKUSERID/

# Put basic taskwarrior config for debugging
COPY user/taskrc /root/.taskrc 

CMD taskd server \
  --user $TASKDUSER:$TASKDGROUP \
  --data $TASKDDATA \
  --daemon \
  && tail -f $TASKDLOG


