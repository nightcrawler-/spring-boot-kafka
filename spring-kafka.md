# Sample Spring Boot + Apache Kafka

## Monolithic, basic producer/consumer

1. Create spring boot application from https://start.spring.io/. Include kafka and starter-web as dependencies. Gradle can be selected as initial build tool, conversion to  maven can be acheived later
2. Download, extract then run `./gradlew build`
3. Configure kafka options -- more on available options can be inferred from this gist: https://gist.github.com/nightcrawler-/287231a58ecdfa4bc6b3c395a5cb2f32

  Using application.yml(instead of application.properties) add the below options:
  ```
  spring:
    profiles:
      active: local
  ---
  spring:
    config:
      activate:
        on-profile: local
    kafka:
      template:
        default-topic: county-events
      producer:
        bootstrap-servers: localhost:9092,localhost:9093,localhost:9094
        key-serializer: org.apache.kafka.common.serialization.IntegerSerializer
        value-serializer: org.apache.kafka.common.serialization.StringSerializer
        properties:
          acks: all
          retries: 10
          retry.backoff.ms: 1000
      admin:
        properties:
          bootstrap.servers: localhost:9092,localhost:9093,localhost:9094

  ```
4. Create necessary dir/app structure (conventional?) controllers/consumer/producer (this is only for illustration)
5. Create a Producer class, which is annotated with @Service. Important members/properties/methods/body:
  ```
    // Topic name for this producer
    // Kafka template
    // Send method

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "county-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        logger.info(String.format("#### -> Producing message -> %s", message));
        this.kafkaTemplate.send(TOPIC, message);
    }
    ```
6. Create a Controller class that will accept the request and publish the message to kafka.

  ```
  @RestController
  @RequestMapping(value = "/kafka")
  public class KafkaController {
      
      private final Producer producer;

      @Autowired
      KafkaController(Producer producer) {
          this.producer = producer;
      }

      // Message can be the POJO..
      @PostMapping(value = "/publish")
      public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
          this.producer.sendMessage(message);
      }
    
}
  ```

7. Then finally create a Consumer class that will basically receive and log the results.

  ```
  @Service
  public class Consumer {

      private final Logger logger = LoggerFactory.getLogger(Producer.class);

      @KafkaListener(topics = "county-events", groupId = "group_id")
      public void consume(String message) throws IOException {
          logger.info(String.format("#### -> Consumed message -> %s", message));
      }
      
  }

  ```
8. The app can now be published and run on a server with kafka service running and it will be available on port 8080. To send a message: 
  `curl -X POST -F 'message=welcome to Nakuru' http://13.76.134.160:8080//kafka/publish`

## Microservices based approach.

Build seprate producer and consumer apps that produce and consume fully formed message bodies (JSON), with Marshalling/Unmarshalling of the same to/from regular POJOs.

### Producer

### Consumer
