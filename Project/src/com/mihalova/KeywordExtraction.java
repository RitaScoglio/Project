package com.mihalova;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Phaser;

public class KeywordExtraction {

    public static void main(String[] args) {

        Date start, end;

        ConcurrentHashMap<String, Word> globalVoc = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Double> globalCommonWords = new ConcurrentHashMap<>();

        start = new Date();
        File source = new File("data");
        //get array of files with suffix .txt (our documents)
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
        //number of hardware threads available to JVM
        int numTasks = factor * Runtime.getRuntime().availableProcessors();
        Phaser phaser = new Phaser();
        //create tasks with number of available processors
        Thread threads[] = new Thread[numTasks];
        KeywordExtractionTask tasks[] = new KeywordExtractionTask[numTasks];

        //create tasks
        for (int i = 0; i < numTasks; i++) {
            tasks[i] = new KeywordExtractionTask(concurrentFileListPhase1, concurrentFileListPhase2, phaser, globalVoc,
                    globalCommonWords, concurrentFileListPhase1.size(), "Task " + i, i==0);
            phaser.register();
            System.out.println(phaser.getRegisteredParties() + " tasks arrived to the Phaser.");
        }
        //start threads
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
        //information about execution
        System.out.println("Is Terminated: " + phaser.isTerminated());

        end = new Date();
        System.out.println("Execution Time: " + (end.getTime() - start.getTime()));
        System.out.println("Vocabulary Size: " + globalVoc.size());
        System.out.println("Number of Documents: " + numDocuments);

    }

}

