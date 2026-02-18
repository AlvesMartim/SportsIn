package org.SportsIn.model.progression.effects;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkContext;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.user.Equipe;

import java.util.List;

public interface PerkEffectStrategy {

    String getEffectType();

    double computeInfluenceModifier(PerkContext context);

    boolean canActivate(Equipe team, PerkDefinition definition, List<ActivePerk> existingPerks);
}
