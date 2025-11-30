package org.SportsIn.model.rules;

import org.SportsIn.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class FootballVictoryRule implements Rule {

    @Override
    public EvaluationResult evaluate(Session session) {
        SessionResult resultData = session.getResult();
        if (resultData == null || resultData.getMetrics() == null || resultData.getMetrics().isEmpty()) {
            return new EvaluationResult(null, "Aucune donnée de score n'est disponible pour évaluer la victoire.");
        }

        // 1. Collecter toutes les métriques de score pertinentes
        List<MetricValue> scores = resultData.getMetrics().stream()
                .filter(metric -> metric.getMetricType() == MetricType.GOALS || metric.getMetricType() == MetricType.POINTS)
                .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return new EvaluationResult(null, "Aucune métrique de type GOALS ou POINTS n'a été trouvée.");
        }

        // 2. Trouver la valeur du score maximum
        double maxScore = scores.stream()
                .mapToDouble(MetricValue::getValue)
                .max()
                .orElse(Double.NEGATIVE_INFINITY);

        // 3. Identifier tous les participants ayant atteint ce score maximum
        List<MetricValue> potentialWinners = scores.stream()
                .filter(metric -> metric.getValue() == maxScore)
                .collect(Collectors.toList());

        // 4. Vérifier s'il y a un gagnant unique
        if (potentialWinners.size() == 1) {
            MetricValue winnerMetric = potentialWinners.get(0);
            String winnerId = winnerMetric.getParticipantId();
            String message = "Le gagnant est le participant " + winnerId + " avec le score le plus élevé.";
            return new EvaluationResult(winnerId, message);
        } else {
            // S'il y a 0 ou plus d'un participant avec le score max, c'est une égalité ou un cas sans gagnant.
            String message = "Égalité. " + potentialWinners.size() + " participants ont le même score maximum de " + maxScore + ".";
            return new EvaluationResult(null, message);
        }
    }
}
