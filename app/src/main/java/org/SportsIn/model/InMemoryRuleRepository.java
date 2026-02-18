package org.SportsIn.model;

import org.SportsIn.model.rules.FootballVictoryRule;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryRuleRepository implements RuleRepository {

    private final Map<Long, Rule> rules = new HashMap<>();

    public InMemoryRuleRepository() {
        // Enregistrement de notre nouvelle règle de football
        rules.put(101L, new FootballVictoryRule());

        // Vous pouvez ajouter d'autres règles ici à l'avenir
        // rules.put(102L, new BasketballVictoryRule());
        // rules.put(201L, new TennisScoringRule());
    }

    @Override
    public Rule findRuleById(Long id) {
        Rule rule = rules.get(id);
        if (rule == null) {
            // C'est une bonne pratique de retourner une règle "par défaut"
            // qui ne fait rien, pour éviter les NullPointerExceptions.
            return session -> new EvaluationResult(null, "Aucune règle trouvée pour l'ID " + id);
        }
        return rule;
    }
}
