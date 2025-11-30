package org.SportsIn.model;

/**
 * Participant est l'entité qui participe à une session de sport.
 * Il peut s'agir d'un joueur individuel ou d'une équipe.
 */
public class Participant {

    private String id;                // Identifiant unique du participant dans la session
    private String name;              // Nom affiché (ex: "Équipe A", "Roger Federer")
    private ParticipantType type;     // JOUEUR ou EQUIPE

    // --- Constructeurs ---

    public Participant() {}

    /**
     * Constructeur complet.
     */
    public Participant(String id, String name, ParticipantType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // --- Getters / Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParticipantType getType() {
        return type;
    }

    public void setType(ParticipantType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
