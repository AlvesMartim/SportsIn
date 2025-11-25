package org.SportsIn.model;

import java.util.List;

public class Equipe {

    private String id;          
    private String name;         
    private List<String> playerIds; 

    public Equipe() {}

    public Equipe(String id, String name, List<String> playerIds) {
        this.id = id;
        this.name = name;
        this.playerIds = playerIds;
    }

}
