package com.mihalova;

public class Word implements Comparable<Word> {

    private String word;
    private int tf;
    private int df;
    private double tfIdf;

    //gets word and inizializes DF to 1
    public Word(String word) {
        this.word = word;
        this.df = 1;
    }

    public String getWord() {
        return word;
    }

    public int getTf() {
        return tf;
    }

    //increasing the value of TF
    public void addTf() {
        this.tf++;
    }

    public int getDf() {
        return df;
    }

    public void setDf(int df) {
        this.df = df;
    }

    //calculates TF-IDF depending on DF and total number of documents
    public void setDf(int df, int N) {
        this.df = df;
        tfIdf = tf * Math.log(Double.valueOf(N) / df);
    }

    public double getTfIdf() {
        return tfIdf;
    }

    //order words from high to low TF-IDF
    @Override
    public int compareTo(Word o) {
        return Double.compare(o.getTfIdf(), this.getTfIdf());
    }

    //merge same words from different documents in one and sums TF and DF of this words
    public Word merge(Word other) {
        if (this.word.equals(other.word)) {
            this.tf+=other.tf;
            this.df+=other.df;
        }
        return this;
    }
}

