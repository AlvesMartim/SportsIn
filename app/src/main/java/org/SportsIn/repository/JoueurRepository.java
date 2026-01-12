package org.SportsIn.repository;

import org.SportsIn.model.user.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoueurRepository extends JpaRepository<Joueur, Long> {
    Optional<Joueur> findByPseudo(String pseudo);
    List<Joueur> findByEquipeId(Long equipeId);
}
