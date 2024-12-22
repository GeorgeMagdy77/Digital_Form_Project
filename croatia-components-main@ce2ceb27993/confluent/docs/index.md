# Confluent Kafka

[Confluent Kafka](https://www.confluent.io/lp/confluent-kafka) is a supported and managed deployment of [Apache Kafka](https://kafka.apache.org/) in use by Croatia as the event streaming layer of the platform

## Depends On

- [operators/core](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/operators/core)

## Overlay Overrides

There are no expected overlay overrides for Confluent Kafka

## Configuration Files

| File | Description | ArgoCD Hook? | Sync Wave |
| ---- | ----------- | ------------ | --------- |
| [kustomization](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/kustomization.yaml) | Is a [kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/#kustomize-feature-list) file, listing the resources for Kustomize to apply | N/A | N/A |
| [connect](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/connect.yaml) | A Confluent [Connect](https://docs.confluent.io/operator/current/co-api.html#tag/Connect) resource to create the `connect` pod | No | 0 |
| [controlcenter](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/controlcenter.yaml) | A Confluent [ControlCenter](https://docs.confluent.io/operator/current/co-api.html#tag/ControlCenter) resource to create the `controlcenter` pod, and it's required platform connections | No | 0 |
| [kafka](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/kafka.yaml) | A Confluent [Kafka](https://docs.confluent.io/operator/current/co-api.html#tag/Kafka) resource to create Kafka instances within the platform | No | 0 |
| [kafkarestproxy](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/kafkarestproxy.yaml) | A Confluent [KafkaRestProxy](https://docs.confluent.io/operator/current/co-api.html#tag/KafkaRestProxy) resource to create a rest proxy instance for the Kafka instances | No | 0 |
| [ksqldb](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/ksqldb.yaml) | A Confluent [KsqlDB](https://docs.confluent.io/operator/current/co-api.html#tag/KsqlDB) resource to create the Kafka SQL DB instance for the platform | No | 0 |
| [schemaregistry](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/schemaregistry.yaml) | A Confluent [SchemaRegistry](https://docs.confluent.io/operator/current/co-api.html#tag/SchemaRegistry) resource to create the schema registry component for Kafka | No | 0 |
| [zookeeper](https://bitbucket.projectcroatia.cloud/projects/DIG/repos/croatia-components/browse/confluent/zookeeper.yaml) | A Confluent [Zookeeper](https://docs.confluent.io/operator/current/co-api.html#tag/Zookeeper) resource to create the zookeeper component for Kafka | No | 0 |
