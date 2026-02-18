package org.SportsIn.services;

public interface InfluenceModifier {

    double apply(Long teamId, String pointId, double currentModifier);

    int getOrder();
}
