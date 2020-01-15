package com.mihalova;

import java.util.HashMap;

public class Document {

    private String fileName;

    private HashMap <String, Word> vocabulary;

    private HashMap <String, Double> commonWords;

    public Document() {
        vocabulary=new HashMap<>();
        commonWords = new HashMap<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public HashMap<String, Word> getVocabulary() {
        return vocabulary;
    }

    public HashMap<String, Double> getKeywords() {
        return commonWords;
    }

    void addKeyword(String word, Double tfIdf){
        commonWords.put(word, tfIdf);
    }

    public void clearVocabulary(){
        vocabulary.clear();
    }

    //insert word in vocabulary (if not in it) and increase its TF
    public void addWord(String string) {
        vocabulary.computeIfAbsent(string, k -> new Word(k)).addTf();
    }

    @Override
    public String toString() {
        return fileName+": "+vocabulary.size();
    }


    public void setKeywords(HashMap<String, Double> commonWords) {
        this.commonWords = commonWords;
    }
}

