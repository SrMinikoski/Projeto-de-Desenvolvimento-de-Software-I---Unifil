package br.com.harmonia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MusicTheory {
    public static final List<String> NOTES = List.of(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");

    private static final Map<String, int[]> SCALES = new LinkedHashMap<>();

    static {
        SCALES.put("Maior", new int[]{0, 2, 4, 5, 7, 9, 11});
        SCALES.put("Menor natural", new int[]{0, 2, 3, 5, 7, 8, 10});
        SCALES.put("Menor harmônica", new int[]{0, 2, 3, 5, 7, 8, 11});
        SCALES.put("Pentatônica maior", new int[]{0, 2, 4, 7, 9});
        SCALES.put("Pentatônica menor", new int[]{0, 3, 5, 7, 10});
        SCALES.put("Blues menor", new int[]{0, 3, 5, 6, 7, 10});
    }

    private MusicTheory() {
    }

    public static List<String> scaleNames() {
        return List.copyOf(SCALES.keySet());
    }

    public static int[] intervals(String scale) {
        int[] intervals = SCALES.get(scale);
        if (intervals == null) {
            throw new IllegalArgumentException("Escala desconhecida: " + scale);
        }
        return intervals.clone();
    }

    public static List<String> scaleNotes(String root, String scale) {
        int rootIndex = NOTES.indexOf(root);
        if (rootIndex < 0) {
            throw new IllegalArgumentException("Nota fundamental desconhecida: " + root);
        }
        List<String> notes = new ArrayList<>();
        for (int interval : intervals(scale)) {
            notes.add(NOTES.get((rootIndex + interval) % NOTES.size()));
        }
        return notes;
    }

    public static List<Chord> harmonicField(String root, String scale) {
        int rootIndex = NOTES.indexOf(root);
        if (rootIndex < 0) {
            throw new IllegalArgumentException("Nota fundamental desconhecida: " + root);
        }
        int[] scaleIntervals = intervals(scale);
        List<Chord> chords = new ArrayList<>();

        for (int degree = 0; degree < scaleIntervals.length; degree++) {
            int rootOffset = scaleIntervals[degree];
            int thirdOffset = degreeOffset(scaleIntervals, degree + 2);
            int fifthOffset = degreeOffset(scaleIntervals, degree + 4);
            int third = thirdOffset - rootOffset;
            int fifth = fifthOffset - rootOffset;
            String quality = quality(third, fifth);
            String degreeLabel = formatDegree(degree + 1);
            chords.add(new Chord(
                    degreeLabel,
                    NOTES.get((rootIndex + rootOffset) % NOTES.size()) + qualitySuffix(quality),
                    quality,
                    List.of(
                            NOTES.get((rootIndex + rootOffset) % NOTES.size()),
                            NOTES.get((rootIndex + thirdOffset) % NOTES.size()),
                            NOTES.get((rootIndex + fifthOffset) % NOTES.size()))));
        }
        return chords;
    }

    private static int degreeOffset(int[] intervals, int degree) {
        return intervals[degree % intervals.length] + 12 * (degree / intervals.length);
    }

    private static String quality(int third, int fifth) {
        return switch (third + ":" + fifth) {
            case "3:6" -> "diminuto";
            case "4:8" -> "aumentado";
            case "4:7" -> "maior";
            case "3:7" -> "menor";
            case "2:7" -> "sus2";
            case "5:7" -> "sus4";
            default -> "não triádico";
        };
    }

    private static String qualitySuffix(String quality) {
        return switch (quality) {
            case "menor" -> "m";
            case "diminuto" -> "dim";
            case "aumentado" -> "aug";
            case "sus2" -> "sus2";
            case "sus4" -> "sus4";
            case "não triádico" -> " (não triádico)";
            default -> "";
        };
    }

    public static String formatDegree(int number) {
        return Integer.toString(number);
    }

    public static String formatDegrees(List<Integer> degrees) {
        return degrees.stream().map(MusicTheory::formatDegree).reduce((left, right) -> left + " – " + right)
                .orElse("");
    }

    public record Chord(String degree, String name, String quality, List<String> notes) {
    }
}
