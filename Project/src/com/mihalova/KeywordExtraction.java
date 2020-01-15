package com.mihalova;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Phaser;

import static com.mihalova.KeywordsTask.allDocuments;

public class KeywordExtraction {

    public static void main(String[] args) {

        ConcurrentHashMap<String, Word> globalVoc = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Double> globalKeywords = new ConcurrentHashMap<>();

        File source = new File("blog_data_test");

        File[] files = source.listFiles(f -> f.getName().endsWith(".txt"));
        if (files == null) {
            System.err.println("The 'data' folder not found!");
            return;
        }
        ConcurrentLinkedDeque<File> concurrentFileListPhase1 = new ConcurrentLinkedDeque<>(Arrays.asList(files));
        ConcurrentLinkedDeque<File> concurrentFileListPhase2 = new ConcurrentLinkedDeque<>(Arrays.asList(files));

        int numDocuments = files.length;

        System.out.println(concurrentFileListPhase1.size());
        System.out.println(concurrentFileListPhase2.size());

        int factor = 1;
        if (args.length > 0) {
            factor = Integer.valueOf(args[0]);
        }

        int numTasks = factor * Runtime.getRuntime().availableProcessors();
        Phaser phaser = new Phaser();

        Thread threads[] = new Thread[numTasks];
        KeywordsTask tasks[] = new KeywordsTask[numTasks];


        for (int i = 0; i < numTasks; i++) {
            tasks[i] = new KeywordsTask(concurrentFileListPhase1, concurrentFileListPhase2, phaser, globalVoc,
                    globalKeywords, numDocuments, "Task " + i, i==0);
            phaser.register();
            System.out.println(phaser.getRegisteredParties() + " tasks arrived to the Phaser.");
        }

        for (int i = 0; i < numTasks; i++) {
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }

        for (int i = 0; i < numTasks; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Is Terminated: " + phaser.isTerminated());
        System.out.println("Vocabulary Size: " + globalVoc.size());
        System.out.println("Number of Documents: " + numDocuments);

        HashMap<Centroid, List<Document>> lastCentroid = new HashMap<>();
        System.out.println(allDocuments.size());

        lastCentroid = KMeans.fit(allDocuments, 5, 10);

        Collection<List<Document>> docs = lastCentroid.values();
        for (int i = 0; i<docs.size(); i++) {
            List<Document> doc = docs.iterator().next();
            System.out.println("centroid " + i +": " +  doc.size());
        }
    }
}

