package org.SportsIn.model.rules;

import org.SportsIn.model.*;

import java.util.Comparator;
import java.util.Optional;

public class FootballVictoryRule implements Rule {

    @Override
    public EvaluationResult evaluate(Session session) {
        SessionResult resultData = session.getResult();
        if (resultData == null || resultData.getMetrics() == null || resultData.getMetrics().isEmpty()) {
            return new EvaluationResult(null, "Aucune donnée de score n'est disponible pour évaluer la victoire.");
        }

        // On cherche la métrique de type GOALS/SCORE avec la valeur la plus élevée.
        // La comparaison se fait directement sur le double retourné par getValue().
        Optional<MetricValue> winningMetric = resultData.getMetrics().stream()
                .filter(metric -> metric.getMetricType() == MetricType.GOALS || metric.getMetricType() == MetricType.POINTS)
                .max(Comparator.comparingDouble(MetricValue::getValue));

        if (winningMetric.isPresent() && winningMetric.get().getValue() > 0) {
            String winnerId = winningMetric.get().getParticipantId();
            String message = "Le gagnant est le participant " + winnerId + " avec le score le plus élevé.";
            return new EvaluationResult(winnerId, message);
        } else {
            return new EvaluationResult(null, "Impossible de déterminer un gagnant à partir des scores fournis.");
        }
    }
}
