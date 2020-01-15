package com.mihalova;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Phaser;


public class KeywordsTask implements Runnable {

	private ConcurrentHashMap<String, Word> globalVoc;
	private ConcurrentHashMap<String, Double> globalKeywords;

	private ConcurrentLinkedDeque<File> concurrentFileListPhase1;
	private ConcurrentLinkedDeque<File> concurrentFileListPhase2;
	public static List<Document> allDocuments = new ArrayList<Document>();

	private Phaser phaser;

	private String name;
	private boolean main;

	private int parsedDocuments;
	private int numDocuments;

	public KeywordsTask(
			ConcurrentLinkedDeque<File> concurrentFileListPhase1,
			ConcurrentLinkedDeque<File> concurrentFileListPhase2,
			Phaser phaser, ConcurrentHashMap<String, Word> globalVoc,
			ConcurrentHashMap<String, Double> globalKeywords,
			int numDocuments, String name, boolean main) {
		this.concurrentFileListPhase1 = concurrentFileListPhase1;
		this.concurrentFileListPhase2 = concurrentFileListPhase2;
		this.globalVoc = globalVoc;
		this.globalKeywords = globalKeywords;
		this.phaser = phaser;
		this.name = name;
		this.main = main;
		this.numDocuments = numDocuments;
		System.out.println(name+": "+main);
	}

	@Override
	public void run() {
		File file;

		// Phase 1
		phaser.arriveAndAwaitAdvance();
		System.out.println(name + ": Phase 1");
		while ((file = concurrentFileListPhase1.poll()) != null) {
			Document doc = DocumentParser.parse(file.getAbsolutePath());
			for (Word word : doc.getVocabulary().values()) {
				globalVoc.merge(word.getWord(), word, Word::merge);
			}
			parsedDocuments++;
		}

		System.out.println(name + ": " + parsedDocuments + " parsed.");
		phaser.arriveAndAwaitAdvance();

		// Phase 2
		System.out.println(name + ": Phase 2");
		
		while ((file = concurrentFileListPhase2.poll()) != null) {
			Document doc = DocumentParser.parse(file.getAbsolutePath());
			List<Word> keywords = new ArrayList<>(doc.getVocabulary().values());

			for (Word word : keywords) {
			  Word globalWord = globalVoc.get(word.getWord());
			  word.setDf(globalWord.getDf(), numDocuments);
			}
			Collections.sort(keywords);

			//store first 10 mostKeywords from every document in globalKeywords
			if(keywords.size() > 10) keywords = keywords.subList(0, 10);
			for (Word word : keywords) {
				doc.addKeyword(word.getWord(), word.getTfIdf());
				addGlobalKeyword(word);
			}
			//save document only with common words
			doc.clearVocabulary();
			allDocuments.add(doc);

		}
		System.out.println(name + ": " + parsedDocuments + " parsed.");

		phaser.arriveAndDeregister();
		System.out.println("Thread " + name + " has finished.");
	}

	private synchronized void addGlobalKeyword(Word word) {
		if (globalKeywords.containsKey(word.getWord())){
			Double newTfIdf = (globalKeywords.get(word.getWord()) + word.getTfIdf()) / 2;
			globalKeywords.replace(word.getWord(), newTfIdf);
		} else {
			globalKeywords.put(word.getWord(), word.getTfIdf());
		}
	}

}
