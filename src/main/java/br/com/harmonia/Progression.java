package br.com.harmonia;

public record Progression(int id, String name, String key, String scale, String chords) {
    @Override
    public String toString() {
        return name + "  ·  " + key + " " + scale;
    }
}
