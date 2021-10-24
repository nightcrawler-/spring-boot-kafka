# Setup Apache Kafka on Ubuntu 20.04

## Prerequisites

Java runtime

`sudo apt install default-jre`

## 1. Setup kafka user

Kafka handles requests over the network, so it is recommended to create a dedicated user for the serivice in order to minimize any damage incase the server is compromised. Additionally, add the new user to the `sudo` group.

`sudo adduser kafka`

Follow the prompts to setup a password, then:

`sudo adduser kafka sudo`

Finally, login to the created account with:

`sudo -l kafka`

## 2. Download and install Kafka binaries, (// TODO: Should be in an Ansible Playbook)

`mkdir ~/Downloads`
`curl "https://downloads.apache.org/kafka/2.6.2/kafka_2.13-2.6.2.tgz" -o ~/Downloads/kafka.tgz`

Create the base directory for kafka and cd into it

`mkdir ~/kafka && cd ~/kafka`

Extract the archive into it

`tar -xvzf ~/Downloads/kafka.tgz --strip 1`

We specify the --strip 1 flag to ensure that the archive’s contents are extracted in ~/kafka/ itself and not in another directory (such as ~/kafka/kafka_2.13-2.6.0/) inside of it. 

## 3. Configure the server

By default, kafka does not allow *topic* deletion. A topic is a category, group or feed name to which messages can be published. The config options are specified in the `server.properties` file. 
Add the following to the bottom of the file:

`delete.topic.enable = true`

Change the directory where log files are store:

`log.dirs=/home/kafka/logs`

Save and close the file. Now that you’ve configured Kafka, your next step is to create systemd unit files for running and enabling the Kafka server on startup.

## 4. Creating Systemd Unit files and starting the Kafka server.

In this section, you will create systemd unit files for the Kafka service. This will help you perform common service actions such as starting, stopping, and restarting Kafka in a manner consistent with other Linux services.

Zookeeper is a service that Kafka uses to manage its cluster state and configurations. It is used in many distributed systems.

Create the unit file for zookeeper:

`sudo nano /etc/systemd/system/zookeeper.service`

Enter the following unit definition into the file:

```
[Unit]
Requires=network.target remote-fs.target
After=network.target remote-fs.target

[Service]
Type=simple
User=kafka
ExecStart=/home/kafka/kafka/bin/zookeeper-server-start.sh /home/kafka/kafka/config/zookeeper.properties
ExecStop=/home/kafka/kafka/bin/zookeeper-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multi-user.target
```

The [Unit] section specifies that Zookeeper requires networking and the filesystem to be ready before it can start

The [Service] section specifies that systemd should use the kafka-server-start.sh and kafka-server-stop.sh shell files for starting and stopping the service. It also specifies that Kafka should be restarted if it exits abnormally.

Next, create the systemd service file for kafka:

`sudo nano /etc/systemd/system/kafka.service`

```
[Unit]
Requires=zookeeper.service
After=zookeeper.service

[Service]
Type=simple
User=kafka
ExecStart=/bin/sh -c '/home/kafka/kafka/bin/kafka-server-start.sh /home/kafka/kafka/config/server.properties > /home/kafka/kafka/kafka.log 2>&1'
ExecStop=/home/kafka/kafka/bin/kafka-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multi-user.target
```

The [Unit] section specifies that this unit file depends on zookeeper.service. This will ensure that zookeeper gets started automatically when the kafka service starts.

The [Service] section specifies that systemd should use the kafka-server-start.sh and kafka-server-stop.sh shell files for starting and stopping the service. It also specifies that Kafka should be restarted if it exits abnormally.

Now that you have defined the unit files, start Kafka with the following command:

`sudo systemctl start kafka`

To ensure that the server has started successfully, check the journal logs for the kafka unit:

`sudo systemctl status kafka`

Output:

```
● kafka.service
     Loaded: loaded (/etc/systemd/system/kafka.service; disabled; vendor preset: enabled)
     Active: active (running) since Wed 2021-02-10 00:09:38 UTC; 1min 58s ago
   Main PID: 55828 (sh)
      Tasks: 67 (limit: 4683)
     Memory: 315.8M
     CGroup: /system.slice/kafka.service
             ├─55828 /bin/sh -c /home/kafka/kafka/bin/kafka-server-start.sh /home/kafka/kafka/config/server.properties > /home/kafka/kafka/kafka.log 2>&1
             └─55829 java -Xmx1G -Xms1G -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -XX:MaxInlineLevel=15 -Djava.awt.headless=true -Xlog:gc*:file=>

Feb 10 00:09:38 cart-67461-1 systemd[1]: Started kafka.service.
```
You now have a Kafka server listening on port 9092.

You have started the kafka service. But if you rebooted your server, Kafka would not restart automatically. To enable the kafka service on server boot, run the following commands:

```
sudo systemctl enable zookeeper
sudo systemctl enable kafka
```

In this step, you started and enabled the kafka and zookeeper services. In the next step, you will check the Kafka installation.

## 5. Testing installation

To verify the server is behaving correctly, in this section you will publish and consume a "Hellow world" message. 

Publishing messages in Kafka requires:

	- A producer, who enables the publication of records and data to topics.
	- A consumer, who reads messages and data from topics.

Create a topic `Nakuru`:

`~/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic Nakuru`

Next, create a Kafka consumer using the kafka-console-consumer.sh script. It expects the ZooKeeper server’s hostname and port, along with a topic name as arguments.

The following command consumes messages from Nakuru. Note the use of the --from-beginning flag, which allows the consumption of messages that were published before the consumer was started:

`~/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic Nakuru --from-beginning`

To publish a message to the Nakuru topic:
`echo "Hello, World, from Nakuru" | ~/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic Nakuru > /dev/null`



## Running the sample app

The sample app is a library application that has a producer and a consumer

### Producer

Think of this as the host of your regular API endpoints, `/api/v1/county`. It receives requests from clients and publishes them to the broker. Or, has your `controllers` ?

#### 1. Create the Spring Boot app

Follow the steps at `https://start.spring.io/` to create and download the starter project.

o
o
o
<Extra content coming here soon, lazy ass writer>

`curl -X POST -F 'message=welcome to Nakuru' http://13.76.134.160:8080//kafka/publish`
