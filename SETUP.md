# DE4A PackageTracker setup instructions

## Prerequisits

- A server accessible from the internet running Docker and docker-compose
- Basic understanding of docker, networking and commandline

How actual backend infrastructure looks varies. You will have to adapt this setup to work on your backend.

### Zookeeper and Kafka setup

These two are the foundation of your setup that takes the passed in messages and makes them available to query for consumers. The Kafka instance is the one that might be the hardest to understand the setup for and to get it working. There are a couple of key parameters that I will try to explain in a bit more detail. Please look in the supplied example [docker-compose.yml](docker/docker-compose.yml)

#### KAFKA_LISTENERS

This environment variable is a comma separated list with the configured listeners that Kafka listens to, the syntax is 
```
KAFKA_LISTENERS=<arbitrary name>://<hostname or ip>:<port>,...
```
- **arbitrary name** can be anything you choose as long as it is unique, this is what you will reference in other parameters
- **hostname or ip** needs to point to a hostname or ip accessible in the docker environment. if you omit this it basically means **localhost**
- **port** is the port that kafka will listen to on the supplied hostname or ip

In a Docker environment where the Kafka should be accessible from outside of Docker (ie the Internet...) you will most likely need two listeners, one accessible from inside the Docker environment and one accessible from outside of Docker. In the supplied [docker-compose.yml](docker/docker-compose.yml) they are named INTERNAL and EXTERNAL respectivley

INTERNAL is using the docker-compose container name (kafka) as the host/ip since in the docker environment you can reference containers by name and port 29092. The EXTERNAL one listens on localhost and port 9092

#### KAFKA_ADVERTISED_LISTENERS

This environment variable is a comma separated list with the listeners **ADVERTISED** by Kafka to clients, the syntax is
```
KAFKA_ADVERTISED_LISTENERS=<any name from KAFKA_LISTENERS>://<hostname or ip>:<port>,....
```

This one is where it might get a little tricky. KAFKA_ADVERTISED_LISTENERS is what Kafka advertises to clients how to reach it. For the INTERNAL in the supplied [docker-compose.yml](docker/docker-compose.yml) that means that Kafka advertises a listener that is reachable on kafka:29092. This will of course not be reachable outside of the Docker environment so Kafka needs to advertise one more. However the EXTERNAL one we configured above is listening on localhost:9092. If we advertise that any client wanting to talk to Kafka will be talking to itself on port 9092 which will be wrong. So, we need to tell Kafka to advertise EXTERNAL to something accessible to the clients which should be a server accessible via normal DNS and Internet, ie \<externally-accessible-hostname\>:\<externally-accessible-port\>. Also, Kafka only speaks TCP so no http or some such in front of the hostname.

So when a client sends something to Kafka from the outside of your infrastructure to \<externally-accessible-hostname\>:\<externally-accessible-port\> the Kafka instance listens on localhost:9092 and picks up the message. Make sure to map the \<externally-accessible-port\> on the host to the correct **KAFKA\_LISTENER** port. 

### Package Tracker setup

The PackageTracker is where others can see in realtime what gets passed to Kafka categorized on topics. Besides deploying it the only setting that needs to be done is in the [application.properties](src/main/resources/application.properties) file and here we need to point to where it can find the Kafka instance. If the PT is deployed with the example docker-compose.yml file Kafka is reachable through the Docker environment meaning we reference the INTERNAL listener, in this case kafka:29092.

### Optional REST Interface

If clients are sitting behind a proxy that only allows HTTP traffic to go out they will not be able to reach Kafka since it only speaks TCP. The solution is to deploy an **OPTIONAL** REST interface for Kafka. In the [docker-compose.yml](docker/docker-compose.yml) this is the rest-kafka service. The vital configuration parameters are:
```
KAFKA_REST_HOST_NAME=rest-kafka
KAFKA_REST_LISTENERS=http://0.0.0.0:8082
KAFKA_REST_BOOTSTRAP_SERVERS=kafka:29092
KAFKA_REST_SASL_MECHANISM=PLAIN
KAFKA_REST_CLIENT_BOOTSTRAP_SERVERS=kafka:29092
KAFKA_REST_CLIENT_SASL_MECHANISM=PLAIN
```

Here the KAFKA\_REST\_LISTENERS define where the REST interface listens for HTTP traffic. In this case it is **localhost**:8082 in the Docker environment. The internal Docker port needs to be mapped to the host environment port where the HTTP traffic enters. The **\*_BOOTSTRAP_SERVERS** point to the **INTERNAL** ADVERTISED\_LISTENER since the REST interface runs in the Docker environment. The clients should now configure there traffic to go to http(s)://\<externally-accessible-hostname\>:\<externally-accessible-port (if the REST interface is running on anything but 80|443)\>. Also be advised that the clients also will have to comply to the REST interface structure of sending messages.

This REST interface is also deployable anywhere, ie locally at a client who then points its traffic to the REST interface and the REST interface in turn needs to change the \*_BOOTSTRAP_SERVERS to point to the **EXTERNAL** ADVERTISED\_LISTENER defined above in the Kafka setup.

## Test the REST Interface setup

Run the following curl command from a server that cannot access the TCP endpoint for the Kafka server to see if it successfully sends messages. Go to the PackageTracker interface to see if the messages are received. Change the \<externally-accessible-hostname\> and \<any-topic-you-want\> to fit your setup.

```
curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" -H "Accept: application/vnd.kafka.v2+json" --data '{"records":[{"key":"alice","value": "working?"}]}' "https://<externally-accessible-hostname>/topics/<any-topic-you-want>"
```

## Test the TCP Interface setup

Install the [kafkacat](https://github.com/edenhill/kafkacat) tool in your local machine environment (ie not the server you have the entire stack on) and run:
```
seq 1 5 | kafkacat -P -b <externally-accessible-hostname>:<externally-accessible-port> -t <any-topic-you-want>
```
In your PackageTracker you should now see 5 messages showing up after each other for the marked topic you chose.

## Resources

- [Confluents REST Docker image](https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/rest-proxy.html)
- [Confluent detailed explanation of Kafka LISTENERS](https://www.confluent.io/blog/kafka-listeners-explained/)

