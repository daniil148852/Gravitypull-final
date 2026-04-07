package me.daniil148852.gravitygauntlet.extension;

import java.util.UUID;

public interface GravityGauntletExtension {
    void gravityGauntlet$setOrbiting(UUID owner, double angle);
    void gravityGauntlet$setLaunched(boolean launched);
}
