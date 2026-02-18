package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Rule;
import org.SportsIn.model.RuleRepository;
import org.SportsIn.model.Session;
import org.SportsIn.model.Sport;
import org.springframework.stereotype.Service;

/**
 * Service responsable de l'évaluation des règles de victoire pour une session.
 * Extrait la logique de règles hors du modèle Sport pour respecter le pattern MVC.
 */
@Service
public class RuleEvaluationService {

    private final RuleRepository ruleRepository;

    public RuleEvaluationService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Évalue les règles de victoire pour une session donnée.
     * @param session La session à évaluer.
     * @return Le résultat de l'évaluation, ou null si pas de sport/règles.
     */
    public EvaluationResult evaluateVictory(Session session) {
        if (session == null || session.getSport() == null) {
            return null;
        }
        Sport sport = session.getSport();
        Rule rule = ruleRepository.findRuleById(sport.getVictoryRuleId());
        if (rule != null) {
            return rule.evaluate(session);
        }
        return null;
    }
}
