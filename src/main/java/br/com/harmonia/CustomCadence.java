package br.com.harmonia;

import java.util.List;

public record CustomCadence(int id, String name, List<Integer> degrees) {
    public CustomCadence {
        degrees = List.copyOf(degrees);
    }

    @Override
    public String toString() {
        return name + " · " + MusicTheory.formatDegrees(degrees);
    }
}
