# Hazelcast Cluster membership status probe
A simple probe to the membership status of a hazelcast cluster.

## deployment settings
The application depends on the following environment variables:

 * **HC_ADDRS**: Required environment variable, in the form of "HOST:PORT[,HOST:PORT]", defines the addresses of the hazelcast nodes to which the client will connect and probe the membership status.

 * **LISTEN_ADDR**: Optional environment variable, in the form of "BINDING_HOST:BINDING_PORT", defines what is the listen address of this application. Defaults to "127.0.0.1:8080"

## API endpoints
* **/hc/clusterinfo**: returns the members list of the hazelcast cluster this application is probing.
* **/hc/members**: The response body contains the member list of the hazelcast cluster. Additionally, this API can takes a **"gte"** query parameter which specifies the minimum size of the member list. If the probed member list has a size smaller than the "gte" parameter, the application will return a 412 response code, other wise returns 200.