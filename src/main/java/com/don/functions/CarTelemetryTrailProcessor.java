package com.don.functions;

import com.don.model.CarTrail;
import com.don.telemetry.CarTelemetry;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Objects;

public class CarTelemetryTrailProcessor extends ProcessWindowFunction<CarTelemetry, CarTrail, String, TimeWindow> {

    @Override
    public void process(String s, ProcessWindowFunction<CarTelemetry, CarTrail, String, TimeWindow>.Context context, Iterable<CarTelemetry> iterable, Collector<CarTrail> collector) throws Exception {
        CarTelemetry firstElement = null;
        CarTelemetry lastElement = null;
        var avgSpeed = 0.0;
        var count = 0;

        for (CarTelemetry carTelemetry : iterable) {
            if (Objects.isNull(firstElement)) {
                firstElement = carTelemetry;
            }
            lastElement = carTelemetry;
            count++;
            avgSpeed += carTelemetry.getSpeed();
        }
        if (!Objects.isNull(firstElement)) {
            collector.collect(new CarTrail(firstElement, lastElement, avgSpeed / count));
        }
    }

}
