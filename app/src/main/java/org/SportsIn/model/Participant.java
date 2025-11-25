//Participant est l'entité qui participe a une session sport 

package main.java.org.SportsIn.model;

public class Participant {

    private String id;                
    private ParticipantType type;   // soit équipe soit joueur   
    private String entityId;          
    private String displayName;       

    public Participant() {}

    public Participant(String id, ParticipantType type, String entityId, String displayName) {
        this.id = id;
        this.type = type;
        this.entityId = entityId;
        this.displayName = displayName;
    }

}
