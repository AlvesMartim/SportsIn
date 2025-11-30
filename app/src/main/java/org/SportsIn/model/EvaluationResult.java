package org.SportsIn.model;

public class EvaluationResult {
    private final String winnerParticipantId;
    private final String message;

    public EvaluationResult(String winnerParticipantId, String message) {
        this.winnerParticipantId = winnerParticipantId;
        this.message = message;
    }

    public String getWinnerParticipantId() {
        return winnerParticipantId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "EvaluationResult{" +
                "winnerParticipantId='" + winnerParticipantId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
