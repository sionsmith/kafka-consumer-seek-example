# Expanding Topic Partitions on Confluent Cloud

The steps to increase the number of partitions in a topic hosted on Confluent Cloud.

1. Create topic (optional)
```shell
$ ccloud login --save
$ ccloud kafka topic create test-topic --partitions 3
```

2. Describe topic 
```shell
$ ccloud kafka topic describe test-topic
Topic: test-topic PartitionCount: 3 ReplicationFactor: 3
    Topic    | Partition | Leader |  Replicas  |    ISR
+------------+-----------+--------+------------+------------+
  test-topic |         0 |      5 | [5 12 1]   | [5 12 1]
  test-topic |         1 |     12 | [12 13 14] | [12 13 14]
  test-topic |         2 |     13 | [13 11 6]  | [13 11 6]

Configuration

                   Name                   |        Value
+-----------------------------------------+---------------------+
  cleanup.policy                          | delete
  compression.type                        | producer
  delete.retention.ms                     |            86400000
  file.delete.delay.ms                    |               60000
  flush.messages                          | 9223372036854775807
  flush.ms                                | 9223372036854775807
  follower.replication.throttled.replicas |
  index.interval.bytes                    |                4096
  leader.replication.throttled.replicas   |
  max.compaction.lag.ms                   | 9223372036854775807
  max.message.bytes                       |             2097164
  message.downconversion.enable           | true
  message.format.version                  | 2.3-IV1
  message.timestamp.difference.max.ms     | 9223372036854775807
  message.timestamp.type                  | CreateTime
  min.cleanable.dirty.ratio               |                 0.5
  min.compaction.lag.ms                   |                   0
  min.insync.replicas                     |                   2
  preallocate                             | false
  retention.bytes                         |                  -1
  retention.ms                            |           604800000
  segment.bytes                           |           104857600
  segment.index.bytes                     |            10485760
  segment.jitter.ms                       |                   0
  segment.ms                              |           604800000
  unclean.leader.election.enable          | false
```

3. To increase number of partitions you will need some information to pass to kafka-topics. `bootstrap-server` URL to connect to. Replace `CCLOUD_API_KEY` and `CCLOUD_API_SECRET` in `client-properties/adminclient.properties` with your personal values.  

```shell
$ ccloud kafka cluster describe <<cluster-id>>
+--------------+---------------------------------------------------------+
| Id           | <<cluster-id>>                                          |
| Name         | <<removed>>                                             |
| Type         | STANDARD                                                |
| Ingress      |                                                     100 |
| Egress       |                                                     100 |
| Storage      | Infinite                                                |
| Provider     | aws                                                     |
| Availability | multi-zone                                              |
| Region       | eu-west-1                                               |
| Status       | UP                                                      |
| Endpoint     | SASL_SSL://removed.aws.confluent.cloud:9092             |
| ApiEndpoint  | https://removed.eu-west-1.aws.confluent.cloud           |
| RestEndpoint |                                                         |
+--------------+---------------------------------------------------------+

# Alter topics using
$ kafka-topics --bootstrap-server removed.aws.confluent.cloud:9092 --command-config client-properties/adminclient.properties --alter --topic test-topic --partitions 5
```

4. Check the changes
```shell
$ ccloud kafka topic describe test-topic
Topic: test-topic PartitionCount: 5 ReplicationFactor: 3
    Topic    | Partition | Leader |  Replicas  |    ISR
+------------+-----------+--------+------------+------------+
  test-topic |         0 |      5 | [5 12 1]   | [5 12 1]
  test-topic |         1 |     12 | [12 13 14] | [12 13 14]
  test-topic |         2 |     13 | [13 11 6]  | [13 11 6]
  test-topic |         3 |      8 | [8 10 3]   | [8 10 3]
  test-topic |         4 |      3 | [3 8 7]    | [3 8 7]

Configuration

                   Name                   |        Value
+-----------------------------------------+---------------------+
  cleanup.policy                          | delete
  compression.type                        | producer
  delete.retention.ms                     |            86400000
  file.delete.delay.ms                    |               60000
  flush.messages                          | 9223372036854775807
  flush.ms                                | 9223372036854775807
  follower.replication.throttled.replicas |
  index.interval.bytes                    |                4096
  leader.replication.throttled.replicas   |
  max.compaction.lag.ms                   | 9223372036854775807
  max.message.bytes                       |             2097164
  message.downconversion.enable           | true
  message.format.version                  | 2.3-IV1
  message.timestamp.difference.max.ms     | 9223372036854775807
  message.timestamp.type                  | CreateTime
  min.cleanable.dirty.ratio               |                 0.5
  min.compaction.lag.ms                   |                   0
  min.insync.replicas                     |                   2
  preallocate                             | false
  retention.bytes                         |                  -1
  retention.ms                            |           604800000
  segment.bytes                           |           104857600
  segment.index.bytes                     |            10485760
  segment.jitter.ms                       |                   0
  segment.ms                              |           604800000
  unclean.leader.election.enable          | false
```

### Important
While Kafka allows us to add more partitions, it is NOT possible to decrease number of partitions of a Topic. In order to achieve this, you need to delete and re-create your Topic.

