package sionsmith.demo.kafka.services.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Slf4j
public abstract class ProducerMetricsReporter<V extends GenericRecord> implements Runnable {
    private final KafkaTemplate<String, V> template;

    //Used to Filter just the metrics we want
    private final Set<String> metricsNameFilter = new HashSet<String>(
            Arrays.asList("record-send-total", "record-queue-time-avg", "record-send-rate", "records-per-request-avg",
                    "request-size-max", "network-io-rate", "record-queue-time-avg",
                    "incoming-byte-rate", "batch-size-avg", "request-total", "response-rate", "requests-in-flight"));

    public ProducerMetricsReporter(
            final KafkaTemplate<String, V> template) {
        this.template = template;
    }

    @Override
    public void run() {
        while (true) {
            template.executeInTransaction(kafkaOperations -> {
                final Map<MetricName, ? extends Metric> metrics
                        = template.metrics();
                displayMetrics(metrics);
                return true;
            });
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                log.warn("metrics interrupted");
                Thread.interrupted();
                break;
            }
        }
    }

    static class MetricPair {
        private final MetricName metricName;
        private final Metric metric;
        MetricPair(MetricName metricName, Metric metric) {
            this.metricName = metricName;
            this.metric = metric;
        }
        public String toString() {
            return metricName.group() + "." + metricName.name();
        }
    }

    private void displayMetrics(Map<MetricName, ? extends Metric> metrics) {
        final Map<String, MetricPair> metricsDisplayMap = metrics.entrySet().stream()
                //Filter out metrics not in metricsNameFilter
                .filter(metricNameEntry ->
                        metricsNameFilter.contains(metricNameEntry.getKey().name()))
                //Filter out metrics not in metricsNameFilter
                .filter(metricNameEntry ->
                        !Double.isInfinite(metricNameEntry.getValue().value()) &&
                                !Double.isNaN(metricNameEntry.getValue().value()) &&
                                metricNameEntry.getValue().value() != 0
                )
                //Turn Map<MetricName,Metric> into TreeMap<String, MetricPair>
                .map(entry -> new MetricPair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(
                        MetricPair::toString, it -> it, (a, b) -> a, TreeMap::new
                ));


        //Output metrics
        final StringBuilder builder = new StringBuilder(255);
        builder.append("\n---------------------------------------\n");
        metricsDisplayMap.forEach((name,metricPair) -> {
            builder.append(String.format(Locale.US, "%50s%25s\t\t%,-10.2f\t\t%s\n",
                    name,
                    metricPair.metricName.name(),
                    metricPair.metric.metricValue(),
                    metricPair.metricName.description()
            ));
        });
        builder.append("\n---------------------------------------\n");
        log.info(builder.toString());
    }
}