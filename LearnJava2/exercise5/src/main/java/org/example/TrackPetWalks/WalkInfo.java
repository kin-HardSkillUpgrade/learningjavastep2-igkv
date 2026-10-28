package org.example.TrackPetWalks;

import java.util.Locale;

class WalkInfo {
    private final Animal pet;
    private final double startWalk;
    private final double endWalk;


    public WalkInfo(Animal pet, double startWalk, double endWalk) {
        this.pet = pet;
        this.startWalk = startWalk;
        this.endWalk = endWalk;
    }

    public Animal getPet() {
        return pet;
    }

    public double getEndWalk() {
        return endWalk;
    }

    public double getStartWalk() {
        return startWalk;
    }

    @Override
    public String toString() {
        return pet.toString()
                + String.format(Locale.ROOT, ", start time = %.2f, end time = %.2f", startWalk, endWalk);
    }
}
