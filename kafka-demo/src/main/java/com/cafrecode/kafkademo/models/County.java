package com.cafrecode.kafkademo.models;

// Create one of the objects to be published

public class County {  

    private String governor; 
    private String name;
    private Integer population; 

    public County(String governor, String name, Integer population) {
        this.governor = governor;
        this.name = name;
        this.population = population;
    }

}