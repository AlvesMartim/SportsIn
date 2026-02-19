package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Rule;
import org.SportsIn.model.RuleRepository;
import org.SportsIn.model.Session;
import org.SportsIn.model.Sport;
import org.SportsIn.model.rules.FootballVictoryRule;
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
        Long ruleId = sport.getVictoryRuleId();
        if (ruleId != null) {
            Rule rule = ruleRepository.findRuleById(ruleId);
            if (rule != null) {
                return rule.evaluate(session);
            }
        }
        // Fallback : règle par défaut basée sur les scores GOALS/POINTS
        return new FootballVictoryRule().evaluate(session);
    }
}
