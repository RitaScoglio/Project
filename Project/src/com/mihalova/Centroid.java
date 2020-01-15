package com.mihalova;

import java.util.*;

public class Centroid {

    private final HashMap<String, Double> coordinates;

    public Centroid(HashMap<String, Double> coordinates) {
        this.coordinates = coordinates;
    }

    public HashMap<String, Double> getCoordinates() {
        return coordinates;
    }

}



