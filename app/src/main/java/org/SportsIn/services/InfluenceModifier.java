package org.SportsIn.services;

public interface InfluenceModifier {

    double apply(Long teamId, Long pointId, double currentModifier);

    int getOrder();
}
