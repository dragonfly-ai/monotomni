# Mono+Omni

Pronounced: monotony, this Scala.js library brings a common mechanism for generating Monotonically Increasing (Mono+) Omnipresent Unique Identifiers to components of distributed systems running in JavaScript and JVM environments.

Traditional relational database designs often rely on an automatically generated integer valued unique identifier for every record in a table.  Relational databases enforce the uniqueness of these identifiers by incrementing an internal counter for every row insertion.  Unfortunately, such databases require a centralized server design and usually impose a single point of failure onto an entire system.  Furthermore, any client of the system can't know a record's unique identifier until after the database has stored it.

In distributed systems with distributed databases, we can't rely on any one node in the system to have authority on unique identifier generation and might not want to persist a record before assigning an identifier to it.  Although we can't achieve sequential id generation for a distributed system as a whole, we can approximate it by generating ids that increase monotonically overall, regardless of which nodes generate them.

Mono+Omni does exactly that by synchronizing, for every node in a distributed system, local time with server time.  Server Time may refer to a centralized server or a cloud based distributed system of interconnected servers.  

Currently, Mono+Omni supports Browser and Node.js environments as well as JVM environments and synchronizes clocks via http requests.

Clock synchronization occurs through statistical analysis on TimeTrial events coordinated between a TimeServer and a TimeServerClient.

In the browser, TimeTrials can rely on an AJAX TimeServerClient to connect to TimeServers addressable by domain name that the client application runs on, or addressable by a domain allowed by the client's <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">CORS</a> configuration.  Browser clients can also rely on TimeServers running on foreign domains by using the JSONP client.

Mono+Omni also ships with default HTTP/S clients for applications running in Node.js and JVM environments.

Examples:
```scala
val id:MOI = Mono+Omni()  // Generate a Mono+Omni id for the local context.
```
MOI stands for Monotonically Increasing, Omnipresent Identifier and plays at the french word: "moi" which means "me".  Under the hood, MOI is a type alias for Long,
```scala
type MOI = Long
```
but a MOI has more components than a simple timestamp.  

Generally, server side nodes of a given distributed system will handle clock synchronization with <a href="https://en.wikipedia.org/wiki/Network_Time_Protocol">NTP</a> and generate all related ids with calls to Mono+Omni() while client nodes in browsers and mobile apps will obtain ids through local approximations of serverside id generators.  The approximation logic and its underlying time synchronization processes inhabits instances of the ```RemoteClock``` class, so client nodes can assign locally generated data structures with ids that have meaning in the context of the rest of the distributed system.

Clients can generate approximate remote ids by calling RemoteClock.ami().
```scala
val remoteId:AMI = remoteClock.ami()
```
AMI stands for Approximate Monotonically Increasing Omnipresent Identifier and plays on the French word: "ami" which means "friend".  Under the hood, Remo+Omni behaves like this:

To synchronize with TimeServer implementations, RemoteClocks can rely on defaults or choose from several existing clients: AJAX, JSONP, Node.JS, or URL.

```scala
val uri:java.net.URI = new java.net.URI("http://timeserver.domain.com/time")

// Environment Default:
implicit val r: RemoteClock = new RemoteClock(native.connection.DefaultConnection(uri))

// AJAX (for browsers only)
implicit val r: RemoteClock = new RemoteClock(ai.dragonfly.monotomni.native.connection.http.AJAX(uri))

// JSONP (for browsers only)
implicit val r: RemoteClock = new RemoteClock(ai.dragonfly.monotomni.native.connection.http.JSONP(uri))

// NodeJS (for Node.JS clients or scala.js sbt consoles)
implicit val r: RemoteClock = new RemoteClock(ai.dragonfly.monotomni.native.connection.http.NodeJS(uri))

// URL (for JVM environments)
implicit val r: RemoteClock = new RemoteClock(ai.dragonfly.monotomni.native.connection.http.URL(uri))

```

With the exception of JSONP, each client can operate with any of 4 messaging formats for TimeTrial events:
```scala
TimeTrialFormat.BINARY
TimeTrialFormat.STRING
TimeTrialFormat.JSON
TimeTrialFormat.XML
```
By default, clients use BINARY, except JSONP which has its own specific format; all clients support http and https.
```
https://whatever.time.server.com/time/BINARY -> Array(0, 0, 1, 120, 55, 107, -26, -66) // just 8 bytes.
https://whatever.time.server.com/time/STRING -> 1615837521599
https://whatever.time.server.com/time/JSON -> {"t":"1615837521600"}
https://whatever.time.server.com/time/XML -> <TimeTrial t="1615837521600"/>
```   