package com.don;

import com.don.functions.CarTelemetryTrailProcessor;
import com.don.model.CarTrail;
import com.don.telemetry.CarTelemetry;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class JobExample {

    private static final Logger log = LoggerFactory.getLogger(JobExample.class);
    private static final String KAFKA_BROKERS_URL = "localhost:19092",
            SCHEMA_REGISTRY_URL = "http://localhost:8081",
            POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/flink",
            POSTGRESQL_USER = "flink",
            POSTGRESQL_PASSWORD = "1234",
            CAR_TELEMETRY_TOPIC = "car-telemetry",
            INSERT_SQL = """
                    insert into CAR_TRAIL(PLATE, START_LATITUDE,START_LONGITUDE,END_LATITUDE,END_LONGITUDE,AVG_SPEED)
                    	values (?,?,?,?,?,?)
                    """;


    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        final DeserializationSchema<CarTelemetry> deserializer = ConfluentRegistryAvroDeserializationSchema.forSpecific(CarTelemetry.class,
                SCHEMA_REGISTRY_URL);

        final var pgConnection = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder().withDriverName("org.postgresql.Driver")
                .withUrl(POSTGRESQL_URL)
                .withUsername(POSTGRESQL_USER)
                .withPassword(POSTGRESQL_PASSWORD)
                .build();

        final var pgExecutionOptions = JdbcExecutionOptions.builder()
                .withBatchSize(1000)
                .withBatchIntervalMs(200)
                .withMaxRetries(3)
                .build();

        final KafkaSource<CarTelemetry> source = KafkaSource.<CarTelemetry>builder()
                .setBootstrapServers(KAFKA_BROKERS_URL)
                .setTopics(CAR_TELEMETRY_TOPIC)
                .setGroupId("flink-example-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(deserializer)
                .build();

        var sink = JdbcSink.<CarTrail>builder()
                .withQueryStatement(INSERT_SQL, (JdbcStatementBuilder<CarTrail>) (ps, trail) -> {
                    try {
                        trail.getStatment(ps);
                    } catch (Exception e) {
                        log.error("Error on process JdbcSink: {}", e.getMessage());
                    }
                }).withExecutionOptions(pgExecutionOptions).buildAtLeastOnce(pgConnection);

        var stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "CarTelemetry to Postgres CarTrail")
                .keyBy(CarTelemetry::getPlate)
                .window(TumblingProcessingTimeWindows.of(Duration.ofSeconds(10)))
                .process(new CarTelemetryTrailProcessor());


        stream.sinkTo(sink);

        env.execute("Example Flink Job");


    }

}
