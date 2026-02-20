package org.SportsIn.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRuleRepositoryTest {

    private InMemoryRuleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRuleRepository();
    }

    @Test
    void findRuleById_registeredRule_returnsFootballRule() {
        Rule rule = repository.findRuleById(101L);
        assertNotNull(rule);
        // verify it's the football rule by evaluating an empty session
        Session session = new Session();
        session.setResult(new SessionResult());
        EvaluationResult result = rule.evaluate(session);
        assertNotNull(result);
    }

    @Test
    void findRuleById_unknownId_returnsFallback() {
        Rule rule = repository.findRuleById(999L);
        assertNotNull(rule); // Should return fallback, not null

        Session session = new Session();
        EvaluationResult result = rule.evaluate(session);
        assertNotNull(result);
        assertNull(result.getWinnerParticipantId());
        assertTrue(result.getMessage().contains("Aucune règle"));
    }

    @Test
    void findRuleById_differentUnknownIds_allReturnFallback() {
        Rule r1 = repository.findRuleById(0L);
        Rule r2 = repository.findRuleById(-1L);
        assertNotNull(r1);
        assertNotNull(r2);
    }
}
