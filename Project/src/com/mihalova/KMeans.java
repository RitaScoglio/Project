package com.mihalova;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class KMeans {

    private static final Random random = new Random();

    public static HashMap<Centroid, List<Document>> fit(List<Document> docs,
                                                        int k,
                                                        int maxIterations) {

        List<Centroid> centroids = randomCentroids(docs, k);
        HashMap<Centroid, List<Document>> clusters = new HashMap<>();
        HashMap<Centroid, List<Document>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (Document doc : docs) {
                Centroid centroid = nearestCentroid(doc, centroids);
                assignToCluster(clusters, doc, centroid);
            }

            // if the assignments do not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    private static List<Centroid> randomCentroids(List<Document> docs, int k) {
        List<Centroid> centroids = new ArrayList<>();
        Map<String, Double> maxs = new HashMap<>();
        Map<String, Double> mins = new HashMap<>();

        for (Document doc : docs) {
            doc.getKeywords().forEach((key, value) -> {
                // compares the value with the current max and choose the bigger value between them
                maxs.compute(key, (k1, max) -> max == null || value > max ? value : max);

                // compare the value with the current min and choose the smaller value between them
                mins.compute(key, (k1, min) -> min == null || value < min ? value : min);
            });
        }

        Set<String> attributes = docs.stream()
                .flatMap(e -> e.getKeywords().keySet().stream())
                .collect(toSet());
        for (int i = 0; i < k; i++) {
            HashMap<String, Double> coordinates = new HashMap<>();
            for (String attribute : attributes) {
                double max = maxs.get(attribute);
                double min = mins.get(attribute);
                coordinates.put(attribute, random.nextDouble() * (max - min) + min);
            }

            centroids.add(new Centroid(coordinates));
        }

        return centroids;
    }

    private static Centroid nearestCentroid(Document doc, List<Centroid> centroids) {
        double minimumDistance = 500;
        Centroid nearest = null;

        for (Centroid centroid : centroids) {
            double currentDistance = EuclideanDistance.calculate(doc.getKeywords(), centroid.getCoordinates());

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }

        return nearest;
    }

    private static void assignToCluster(Map<Centroid, List<Document>> clusters,
                                        Document doc,
                                        Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(doc);
            return list;
        });
    }

    private static Centroid average(Centroid centroid, List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return centroid;
        }

        HashMap<String, Double> average = centroid.getCoordinates();
        docs.stream().flatMap(e -> e.getKeywords().keySet().stream())
                .forEach(k -> average.put(k, 0.0));

        for (Document doc : docs) {
            doc.getKeywords().forEach(
                    (k, v) -> average.compute(k, (k1, currentValue) -> v + currentValue)
            );
        }

        average.forEach((k, v) -> average.put(k, v / docs.size()));

        return new Centroid(average);
    }

    private static List<Centroid> relocateCentroids(Map<Centroid, List<Document>> clusters) {
        return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
    }
}
