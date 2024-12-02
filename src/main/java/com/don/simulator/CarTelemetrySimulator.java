package com.don.simulator;

import com.don.telemetry.CarTelemetry;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

public class CarTelemetrySimulator {

    private static final Logger log = LoggerFactory.getLogger(CarTelemetrySimulator.class);

    public static void main(String[] args) throws InterruptedException {
        // Configuração do Kafka Producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<String, CarTelemetry> producer = new KafkaProducer<>(props);

        // Coordenadas de Araçatuba e São Paulo
        final double startLatitude = -21.210370;
        final double startLongitude = -50.459954;
        final double endLatitude = -21.207313;
        final double endLongitude = -50.445962;
        final int steps = 100;
        final double latitudeIncrement = (endLatitude - startLatitude) / steps;
        final double longitudeIncrement = (endLongitude - startLongitude) / steps;

        double currentLatitude = startLatitude;
        double currentLongitude = startLongitude;

        Random random = new Random();

        try {
            for (int step = 0; step <= steps; step++) {
                // Incrementa a posição
                currentLatitude += latitudeIncrement;
                currentLongitude += longitudeIncrement;

                // Simula velocidade entre 50 e 120 km/h
                double speed = 50 + random.nextDouble() * 70;

                // Timestamp atual
                long timestamp = System.currentTimeMillis();

                // Placa do veículo
                String plate = "ABC1234";

                // Cria o objeto Avro CarTelemetry
                CarTelemetry telemetry = CarTelemetry.newBuilder()
                        .setLatitude(currentLatitude)
                        .setLongitude(currentLongitude)
                        .setSpeed(speed)
                        .setSensorTimestamp(Instant.ofEpochSecond(timestamp))
                        .setPlate(plate)
                        .build();

                // Envia a mensagem para o Kafka
                ProducerRecord<String, CarTelemetry> record = new ProducerRecord<>("car-telemetry", plate, telemetry);
                producer.send(record);

                log.info("Sent: {}", telemetry);

                // Aguarda 2 segundos antes de enviar o próximo ponto
                Thread.sleep(2000);
            }

            log.info("Simulation complete!");
        } finally {
            producer.close();
        }
    }
}
