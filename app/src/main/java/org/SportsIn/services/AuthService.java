package org.SportsIn.services;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.JoueurRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final JoueurRepository joueurRepository;

    public AuthService(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    /**
     * Inscription d'un nouveau joueur
     */
    public Joueur register(String pseudo, String email, String password) {
        if (joueurRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        if (joueurRepository.existsByPseudo(pseudo)) {
            throw new RuntimeException("Ce pseudo est déjà utilisé");
        }
        Joueur joueur = new Joueur(pseudo, email, password);
        return joueurRepository.save(joueur);
    }

    /**
     * Connexion par email OU pseudo
     */
    public Optional<Joueur> login(String identifier, String password) {
        Optional<Joueur> joueurOpt = joueurRepository.findByEmail(identifier.toLowerCase());
        if (joueurOpt.isEmpty()) {
            joueurOpt = joueurRepository.findByPseudo(identifier);
        }
        if (joueurOpt.isPresent() && joueurOpt.get().getPassword().equals(password)) {
            return joueurOpt;
        }
        return Optional.empty();
    }

    /**
     * Récupérer un joueur par ID
     */
    public Optional<Joueur> getById(Long id) {
        return joueurRepository.findById(id);
    }

    /**
     * Modifier pseudo et/ou mot de passe
     */
    public Optional<Joueur> updateProfile(Long id, String newPseudo, String newPassword) {
        return joueurRepository.findById(id).map(joueur -> {
            if (newPseudo != null && !newPseudo.isBlank()) {
                if (!newPseudo.equals(joueur.getPseudo()) && joueurRepository.existsByPseudo(newPseudo)) {
                    throw new RuntimeException("Ce pseudo est déjà utilisé");
                }
                joueur.setPseudo(newPseudo);
            }
            if (newPassword != null && !newPassword.isBlank()) {
                if (newPassword.length() < 6) {
                    throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
                }
                joueur.setPassword(newPassword);
            }
            return joueurRepository.save(joueur);
        });
    }
}
