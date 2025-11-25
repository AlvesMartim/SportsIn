package com.noedevops.sportengine.domain.model;

import java.util.List;

public class Equipe {

    private String id;          
    private String name;         
    private List<String> playerIds; 

    public Team() {}

    public Team(String id, String name, List<String> playerIds) {
        this.id = id;
        this.name = name;
        this.playerIds = playerIds;
    }

}
