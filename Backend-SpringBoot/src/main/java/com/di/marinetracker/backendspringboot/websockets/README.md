# About web sockets

The hardest parts so far have been:

- choosing between STOMP and raw sockets and going back and forth, it's tempting so please be careful if you switch
- SpringBoot actively makes raw sockets harder, when there's authentication
- the different kinds of notifications have different needs

## Simple next steps

- Error messages have mixed capitalization.
- The JSONs that can be sent should all be in one place, or it will be confusing when wiring up the front end.
- The VesselPositionsConsumer might be doing too much currently.
- Consider records instead of classes in the visible area of the sessions.

## Harder next steps

- Make it compile.
- Validate the JWT in the handshake interceptor.
- Limit the allowed origins in the web socket config.
- Hook up the database in the vessel consumer.
- Switch the front end from STMOP to raw web sockets.

## No Principal

Normally, `session.getPrincipal().getName()` returns a session's username, but it's very complicated to
set the principal. Maybe it's designed so libraries set it.

The principal is null here, the username is passed through "attribute" arguments.

This may cause problems with banning users and changing usernames.

## Handler and interceptor

Generally, web sockets appear like HTTP until the server proves it understands them and they become streams.

The JWT is in the headers, so the interceptor reads it. The session id is only known to the stream handler,
so the interceptor sends the username from the JWT to the handler, through the "attributes" argument.
That's because in order to save a session and its user, one needs to know both.
