package org.SportsIn.model.territory;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une zone géographique composée de plusieurs points sportifs.
 * Règle de contrôle : Une zone est contrôlée par une équipe si elle possède au moins 3 points dans cette zone.
 */
public class Zone {

    private Long id;
    private String nom;
    private List<PointSportif> points;
    private Long controllingTeamId; // ID de l'équipe qui contrôle la zone (null si personne)

    public Zone() {
        this.points = new ArrayList<>();
    }

    public Zone(Long id, String nom, List<PointSportif> points) {
        this.id = id;
        this.nom = nom;
        this.points = points != null ? points : new ArrayList<>();
        this.controllingTeamId = null;
    }

    // --- Logique métier ---

    /**
     * Vérifie et met à jour le contrôleur de la zone en fonction des points.
     * Règle : 3 points contrôlés par la même équipe = Zone contrôlée.
     * @return true si le contrôleur de la zone a changé.
     */
    public boolean updateZoneControl() {
        // Compter les points par équipe
        // On pourrait utiliser une Map, mais restons simple pour l'instant.
        
        // On cherche s'il y a une équipe qui a >= 3 points.
        // Note : S'il y a plusieurs équipes avec >= 3 points (grandes zones), 
        // on pourrait avoir besoin d'une règle de priorité. Ici on prend la première trouvée ou celle majoritaire.
        
        // Approche simple : On itère sur les points et on compte.
        java.util.Map<Long, Integer> counts = new java.util.HashMap<>();
        
        for (PointSportif p : points) {
            Long teamId = p.getControllingTeamId();
            if (teamId != null) {
                counts.put(teamId, counts.getOrDefault(teamId, 0) + 1);
            }
        }

        Long newMaster = null;
        for (java.util.Map.Entry<Long, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 3) {
                newMaster = entry.getKey();
                // On s'arrête à la première équipe qui a 3 points (ou on pourrait chercher le max)
                break; 
            }
        }

        // Mise à jour si changement
        if (newMaster != null && !newMaster.equals(this.controllingTeamId)) {
            this.controllingTeamId = newMaster;
            return true;
        } else if (newMaster == null && this.controllingTeamId != null) {
            // Optionnel : Si l'équipe perd ses points et passe sous 3, perd-elle la zone ?
            // La règle "3 points conquis = zone contrôlée" implique souvent que c'est une condition de maintien.
            // Si on veut qu'elle perde la zone, on décommente la ligne suivante :
            this.controllingTeamId = null;
            return true;
        }
        
        return false;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<PointSportif> getPoints() {
        return points;
    }

    public void setPoints(List<PointSportif> points) {
        this.points = points;
    }

    public Long getControllingTeamId() {
        return controllingTeamId;
    }

    public void setControllingTeamId(Long controllingTeamId) {
        this.controllingTeamId = controllingTeamId;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", controllingTeamId=" + controllingTeamId +
                ", nbPoints=" + points.size() +
                '}';
    }
}
