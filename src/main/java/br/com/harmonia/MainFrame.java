package br.com.harmonia;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class MainFrame extends JFrame {
    private static final Color BACKGROUND = new Color(20, 24, 31);
    private static final Color PANEL = new Color(30, 36, 45);
    private static final Color ACCENT = new Color(103, 221, 175);
    private static final Color MUTED = new Color(161, 174, 190);
    private static final List<ProgressionPreset> PRESETS = List.of(
            new ProgressionPreset("Pop · 1–5–6–4", "Pop", "C", "Maior", "C · G · Am · F"),
            new ProgressionPreset("Clássica · 1–6–4–5", "Clássica", "C", "Maior", "C · Am · F · G"),
            new ProgressionPreset("Menor · 1–6–3–7", "Menor", "A", "Menor natural", "Am · F · C · G"),
            new ProgressionPreset("Blues · 12 compassos", "Blues", "C", "Maior",
                    "C7 · C7 · C7 · C7 · F7 · F7 · C7 · C7 · G7 · F7 · C7 · G7"),
            new ProgressionPreset("Jazz · 2–5–1", "Jazz 2–5–1", "C", "Maior", "Dm7 · G7 · Cmaj7"));
    private static final List<CadencePreset> SEVEN_DEGREE_CADENCES = List.of(
            new CadencePreset("Pop · 1–5–6–4", List.of(1, 5, 6, 4)),
            new CadencePreset("Clássica · 1–6–4–5", List.of(1, 6, 4, 5)),
            new CadencePreset("Pop · 6–4–1–5", List.of(6, 4, 1, 5)),
            new CadencePreset("Plagal · 1–4–1–5", List.of(1, 4, 1, 5)),
            new CadencePreset("Autêntica · 2–5–1", List.of(2, 5, 1)),
            new CadencePreset("Autêntica · 1–4–5–1", List.of(1, 4, 5, 1)));
    private static final List<CadencePreset> FIVE_DEGREE_CADENCES = List.of(
            new CadencePreset("Pop pentatônica · 1–5–4–1", List.of(1, 5, 4, 1)),
            new CadencePreset("Rock pentatônico · 1–4–5–4", List.of(1, 4, 5, 4)),
            new CadencePreset("Plagal pentatônica · 1–4–1–5", List.of(1, 4, 1, 5)),
            new CadencePreset("Ascendente · 1–2–4–5", List.of(1, 2, 4, 5)));
    private static final List<CadencePreset> SIX_DEGREE_CADENCES = List.of(
            new CadencePreset("Pop · 1–5–4–1", List.of(1, 5, 4, 1)),
            new CadencePreset("Rock · 1–4–5–4", List.of(1, 4, 5, 4)),
            new CadencePreset("Plagal · 1–4–1–5", List.of(1, 4, 1, 5)),
            new CadencePreset("Ascendente · 1–2–4–5", List.of(1, 2, 4, 5)));
    private final Database database;
    private final JComboBox<String> keyBox = new JComboBox<>(MusicTheory.NOTES.toArray(String[]::new));
    private final JComboBox<String> scaleBox = new JComboBox<>(MusicTheory.scaleNames().toArray(String[]::new));
    private final JComboBox<String> scaleKeyBox = new JComboBox<>(MusicTheory.NOTES.toArray(String[]::new));
    private final JComboBox<String> scaleScaleBox = new JComboBox<>(MusicTheory.scaleNames().toArray(String[]::new));
    private final JComboBox<String> instrumentBox = new JComboBox<>(new String[]{"Violão / guitarra", "Baixo"});
    private final JPanel chordPanel = new JPanel(new GridLayout(0, 4, 10, 10));
    private final JComboBox<CadenceChoice> cadenceBox = new JComboBox<>();
    private final JLabel cadenceHeading = new JLabel();
    private final JPanel cadenceChordPanel = new JPanel(new GridLayout(0, 4, 10, 10));
    private final DefaultListModel<CustomCadence> customCadenceModel = new DefaultListModel<>();
    private final JList<CustomCadence> customCadenceList = new JList<>(customCadenceModel);
    private final JPanel scaleNotesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel fretboardPanel = new JPanel(new GridLayout(0, 14, 2, 2));
    private final DefaultListModel<Progression> progressionModel = new DefaultListModel<>();
    private final JList<Progression> progressionList = new JList<>(progressionModel);
    private boolean syncingSelectors;

    public MainFrame() throws IOException, SQLException {
        super("Harmonia Studio");
        database = new Database();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setSize(1100, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(0, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);

        keyBox.setSelectedItem("C");
        scaleBox.setSelectedItem("Maior");
        scaleKeyBox.setSelectedItem("C");
        scaleScaleBox.setSelectedItem("Maior");
        keyBox.addActionListener(event -> syncSelectors(scaleKeyBox, keyBox.getSelectedItem()));
        scaleBox.addActionListener(event -> syncSelectors(scaleScaleBox, scaleBox.getSelectedItem()));
        scaleKeyBox.addActionListener(event -> syncSelectors(keyBox, scaleKeyBox.getSelectedItem()));
        scaleScaleBox.addActionListener(event -> syncSelectors(scaleBox, scaleScaleBox.getSelectedItem()));
        instrumentBox.addActionListener(event -> refreshFretboard());
        cadenceBox.addActionListener(event -> showSelectedCadence());
        refreshTheory();
        refreshProgressions();
        refreshCustomCadences();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(22, 28, 16, 28));
        JLabel title = new JLabel("HARMONIA STUDIO");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Teoria musical para instrumentos de corda");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BACKGROUND);
        tabs.setForeground(BACKGROUND);
        tabs.addTab("  Campo harmônico  ", createHarmonyTab());
        tabs.addTab("  Escalas e braço  ", createScalesTab());
        tabs.addTab("  Progressões salvas  ", createProgressionsTab());
        return tabs;
    }

    private JPanel createHarmonyTab() {
        JPanel content = pagePanel();
        JPanel controls = cardPanel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        controls.add(label("Tonalidade"));
        controls.add(scaleKeyBox);
        controls.add(label("Escala"));
        controls.add(scaleScaleBox);
        content.add(controls, BorderLayout.NORTH);

        JPanel result = cardPanel();
        result.setLayout(new BorderLayout(0, 12));
        JLabel heading = sectionTitle("Acordes diatônicos");
        result.add(heading, BorderLayout.NORTH);
        chordPanel.setOpaque(false);
        result.add(chordPanel, BorderLayout.CENTER);

        JPanel cadenceControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        cadenceControls.setOpaque(false);
        cadenceControls.add(label("Cadência"));
        cadenceControls.add(cadenceBox);
        cadenceHeading.setForeground(ACCENT);
        cadenceHeading.setFont(new Font("SansSerif", Font.BOLD, 15));
        cadenceChordPanel.setOpaque(false);
        JPanel cadenceCard = cardPanel();
        cadenceCard.setLayout(new BorderLayout(0, 10));
        JPanel cadenceHeader = new JPanel(new BorderLayout());
        cadenceHeader.setOpaque(false);
        cadenceHeader.add(sectionTitle("Acordes da cadência"), BorderLayout.NORTH);
        cadenceHeader.add(cadenceControls, BorderLayout.CENTER);
        cadenceCard.add(cadenceHeader, BorderLayout.NORTH);
        JPanel cadenceResult = new JPanel(new BorderLayout(0, 8));
        cadenceResult.setOpaque(false);
        cadenceResult.add(cadenceHeading, BorderLayout.NORTH);
        cadenceResult.add(cadenceChordPanel, BorderLayout.CENTER);
        cadenceCard.add(cadenceResult, BorderLayout.CENTER);

        customCadenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customCadenceList.setBackground(PANEL);
        customCadenceList.setForeground(Color.WHITE);
        customCadenceList.setSelectionBackground(new Color(48, 91, 77));
        customCadenceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel cell = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                cell.setText(value == null ? "" : value.toString());
                cell.setBorder(new EmptyBorder(6, 8, 6, 8));
                return cell;
            }
        });
        JScrollPane customListScroll = new JScrollPane(customCadenceList);
        customListScroll.setPreferredSize(new Dimension(200, 125));
        customListScroll.setBorder(BorderFactory.createLineBorder(new Color(55, 64, 77)));

        JPanel cadenceActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        cadenceActions.setOpaque(false);
        JButton addCadence = button("Nova cadência");
        JButton editCadence = button("Editar");
        JButton deleteCadence = button("Excluir");
        addCadence.addActionListener(event -> editCustomCadence(null));
        editCadence.addActionListener(event -> {
            CustomCadence selected = customCadenceList.getSelectedValue();
            if (selected == null) {
                showMessage("Selecione uma cadência personalizada para editar.");
            } else {
                editCustomCadence(selected);
            }
        });
        deleteCadence.addActionListener(event -> deleteCustomCadence());
        cadenceActions.add(addCadence);
        cadenceActions.add(editCadence);
        cadenceActions.add(deleteCadence);

        JPanel customCard = cardPanel();
        customCard.setLayout(new BorderLayout(0, 8));
        customCard.add(sectionTitle("Cadências personalizadas"), BorderLayout.NORTH);
        customCard.add(customListScroll, BorderLayout.CENTER);
        customCard.add(cadenceActions, BorderLayout.SOUTH);
        customCadenceList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            CustomCadence selected = customCadenceList.getSelectedValue();
            if (selected != null) {
                for (int index = 0; index < cadenceBox.getItemCount(); index++) {
                    CadenceChoice choice = cadenceBox.getItemAt(index);
                    if (choice.customId() != null && choice.customId() == selected.id()) {
                        cadenceBox.setSelectedIndex(index);
                        break;
                    }
                }
            }
        });

        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBackground(BACKGROUND);
        body.add(result);
        body.add(javax.swing.Box.createVerticalStrut(12));
        body.add(cadenceCard);
        body.add(javax.swing.Box.createVerticalStrut(12));
        body.add(customCard);
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel createScalesTab() {
        JPanel content = pagePanel();
        JPanel controls = cardPanel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        controls.add(label("Tonalidade"));
        controls.add(keyBox);
        controls.add(label("Escala"));
        controls.add(scaleBox);
        controls.add(label("Instrumento"));
        controls.add(instrumentBox);

        JPanel notesCard = cardPanel();
        notesCard.setLayout(new BorderLayout(0, 8));
        notesCard.add(sectionTitle("Notas da escala"), BorderLayout.NORTH);
        scaleNotesPanel.setOpaque(false);
        notesCard.add(scaleNotesPanel, BorderLayout.CENTER);

        JPanel fretCard = cardPanel();
        fretCard.setLayout(new BorderLayout(0, 10));
        fretCard.add(sectionTitle("Mapa de notas · afinação padrão"), BorderLayout.NORTH);
        fretboardPanel.setOpaque(false);
        fretCard.add(new JScrollPane(fretboardPanel), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(notesCard, BorderLayout.NORTH);
        center.add(fretCard, BorderLayout.CENTER);
        content.add(controls, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        return content;
    }

    private JPanel createProgressionsTab() {
        JPanel content = pagePanel();
        progressionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        progressionList.setBackground(PANEL);
        progressionList.setForeground(Color.WHITE);
        progressionList.setSelectionBackground(new Color(48, 91, 77));
        progressionList.setFixedCellHeight(38);
        JScrollPane listScroll = new JScrollPane(progressionList);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(55, 64, 77)));

        JTextArea details = new JTextArea(5, 30);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBackground(PANEL);
        details.setForeground(MUTED);
        details.setFont(new Font("Monospaced", Font.PLAIN, 15));
        details.setBorder(new EmptyBorder(12, 12, 12, 12));
        progressionList.addListSelectionListener(event -> {
            Progression selected = progressionList.getSelectedValue();
            details.setText(selected == null ? "Selecione uma progressão para ver os detalhes."
                    : selected.key() + " · " + selected.scale() + "\n\n" + selected.chords());
        });

        JPanel left = cardPanel();
        left.setLayout(new BorderLayout(0, 10));
        left.add(sectionTitle("Minhas progressões"), BorderLayout.NORTH);
        left.add(listScroll, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton create = button("Nova");
        JButton usePreset = button("Usar modelo");
        JButton edit = button("Editar");
        JButton delete = button("Excluir");
        create.addActionListener(event -> editProgression(null));
        usePreset.addActionListener(event -> selectProgressionPreset());
        edit.addActionListener(event -> {
            Progression selected = progressionList.getSelectedValue();
            if (selected == null) {
                showMessage("Selecione uma progressão para editar.");
            } else {
                editProgression(selected);
            }
        });
        delete.addActionListener(event -> deleteSelected());
        actions.add(create);
        actions.add(usePreset);
        actions.add(edit);
        actions.add(delete);
        left.add(actions, BorderLayout.SOUTH);

        JPanel right = cardPanel();
        right.setLayout(new BorderLayout(0, 10));
        right.add(sectionTitle("Detalhes"), BorderLayout.NORTH);
        right.add(details, BorderLayout.CENTER);
        JPanel split = new JPanel(new GridLayout(1, 2, 12, 0));
        split.setOpaque(false);
        split.add(left);
        split.add(right);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    private void refreshTheory() {
        String key = (String) keyBox.getSelectedItem();
        String scale = (String) scaleBox.getSelectedItem();
        if (key == null || scale == null) {
            return;
        }

        chordPanel.removeAll();
        for (MusicTheory.Chord chord : MusicTheory.harmonicField(key, scale)) {
            JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
            card.setBackground(PANEL);
            card.setBorder(new EmptyBorder(12, 14, 12, 14));
            JLabel degree = new JLabel(chord.degree(), SwingConstants.LEFT);
            degree.setForeground(ACCENT);
            degree.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel chordName = new JLabel(chord.name());
            chordName.setForeground(Color.WHITE);
            chordName.setFont(new Font("SansSerif", Font.BOLD, 20));
            JLabel notes = new JLabel(String.join(" · ", chord.notes()));
            notes.setForeground(MUTED);
            notes.setFont(new Font("SansSerif", Font.PLAIN, 12));
            card.add(degree);
            card.add(chordName);
            card.add(notes);
            chordPanel.add(card);
        }
        chordPanel.revalidate();
        chordPanel.repaint();

        scaleNotesPanel.removeAll();
        List<String> notes = MusicTheory.scaleNotes(key, scale);
        for (String note : notes) {
            JLabel chip = new JLabel(note, SwingConstants.CENTER);
            chip.setForeground(Color.WHITE);
            chip.setBackground(note.equals(key) ? new Color(48, 91, 77) : PANEL);
            chip.setOpaque(true);
            chip.setBorder(new EmptyBorder(9, 14, 9, 14));
            chip.setFont(new Font("SansSerif", Font.BOLD, 14));
            scaleNotesPanel.add(chip);
        }
        scaleNotesPanel.revalidate();
        scaleNotesPanel.repaint();
        refreshCadenceChoices();
        refreshFretboard();
    }

    private void refreshCadenceChoices() {
        CadenceChoice previous = (CadenceChoice) cadenceBox.getSelectedItem();
        String scale = (String) scaleScaleBox.getSelectedItem();
        if (scale == null) {
            return;
        }

        int degreeCount = MusicTheory.intervals(scale).length;
        List<CadencePreset> presets = switch (degreeCount) {
            case 5 -> FIVE_DEGREE_CADENCES;
            case 6 -> SIX_DEGREE_CADENCES;
            default -> SEVEN_DEGREE_CADENCES;
        };
        List<CadenceChoice> choices = new ArrayList<>();
        for (CadencePreset preset : presets) {
            choices.add(new CadenceChoice(preset.name(), preset.degrees(), null));
        }
        try {
            for (CustomCadence cadence : database.findAllCadences()) {
                if (cadence.degrees().stream().allMatch(degree -> degree >= 1 && degree <= degreeCount)) {
                    choices.add(new CadenceChoice(
                            "Personalizada · " + cadence.name(), cadence.degrees(), cadence.id()));
                }
            }
        } catch (SQLException exception) {
            showError("Não foi possível carregar as cadências salvas.", exception);
        }

        cadenceBox.removeAllItems();
        for (CadenceChoice choice : choices) {
            cadenceBox.addItem(choice);
        }
        if (previous != null) {
            cadenceBox.setSelectedItem(choices.stream().filter(previous::equals).findFirst().orElse(null));
        }
        if (cadenceBox.getSelectedIndex() < 0 && cadenceBox.getItemCount() > 0) {
            cadenceBox.setSelectedIndex(0);
        }
        showSelectedCadence();
    }

    private void showSelectedCadence() {
        CadenceChoice selected = (CadenceChoice) cadenceBox.getSelectedItem();
        String key = (String) scaleKeyBox.getSelectedItem();
        String scale = (String) scaleScaleBox.getSelectedItem();
        cadenceChordPanel.removeAll();
        if (selected == null || key == null || scale == null) {
            cadenceHeading.setText("Selecione uma cadência");
            cadenceChordPanel.revalidate();
            cadenceChordPanel.repaint();
            return;
        }

        List<MusicTheory.Chord> field = MusicTheory.harmonicField(key, scale);
        cadenceHeading.setText(selected.name() + "  ·  " + MusicTheory.formatDegrees(selected.degrees()));
        for (int degree : selected.degrees()) {
            if (degree < 1 || degree > field.size()) {
                continue;
            }
            MusicTheory.Chord chord = field.get(degree - 1);
            JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
            card.setBackground(PANEL);
            card.setBorder(new EmptyBorder(10, 12, 10, 12));
            JLabel roman = new JLabel(MusicTheory.formatDegree(degree));
            roman.setForeground(ACCENT);
            roman.setFont(new Font("SansSerif", Font.BOLD, 14));
            JLabel name = new JLabel(chord.name());
            name.setForeground(Color.WHITE);
            name.setFont(new Font("SansSerif", Font.BOLD, 18));
            JLabel notes = new JLabel(String.join(" · ", chord.notes()));
            notes.setForeground(MUTED);
            notes.setFont(new Font("SansSerif", Font.PLAIN, 12));
            card.add(roman);
            card.add(name);
            card.add(notes);
            cadenceChordPanel.add(card);
        }
        cadenceChordPanel.revalidate();
        cadenceChordPanel.repaint();
    }

    private void refreshCustomCadences() {
        try {
            customCadenceModel.clear();
            for (CustomCadence cadence : database.findAllCadences()) {
                customCadenceModel.addElement(cadence);
            }
        } catch (SQLException exception) {
            showError("Não foi possível carregar as cadências personalizadas.", exception);
        }
        refreshCadenceChoices();
    }

    private void editCustomCadence(CustomCadence existing) {
        int degreeCount = MusicTheory.intervals((String) scaleScaleBox.getSelectedItem()).length;
        JTextField nameField = new JTextField(existing == null ? "" : existing.name(), 24);
        DefaultListModel<Integer> degreesModel = new DefaultListModel<>();
        if (existing != null) {
            existing.degrees().forEach(degreesModel::addElement);
        }
        JList<Integer> degreesList = new JList<>(degreesModel);
        degreesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        degreesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel cell = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                cell.setText(value == null ? "" : MusicTheory.formatDegree((Integer) value));
                cell.setBorder(new EmptyBorder(4, 8, 4, 8));
                return cell;
            }
        });
        JComboBox<String> degreeBox = new JComboBox<>();
        for (int degree = 1; degree <= degreeCount; degree++) {
            degreeBox.addItem(MusicTheory.formatDegree(degree) + " (" + degree + ")");
        }
        JButton addDegree = button("Adicionar grau");
        addDegree.addActionListener(event -> {
            int degree = degreeBox.getSelectedIndex() + 1;
            degreesModel.addElement(degree);
            degreesList.setSelectedIndex(degreesModel.size() - 1);
        });
        JButton removeDegree = button("Remover");
        removeDegree.addActionListener(event -> {
            int index = degreesList.getSelectedIndex();
            if (index >= 0) {
                degreesModel.remove(index);
                if (!degreesModel.isEmpty()) {
                    degreesList.setSelectedIndex(Math.min(index, degreesModel.size() - 1));
                }
            }
        });
        JButton moveUp = button("↑");
        moveUp.addActionListener(event -> moveCadenceDegree(degreesModel, degreesList, -1));
        JButton moveDown = button("↓");
        moveDown.addActionListener(event -> moveCadenceDegree(degreesModel, degreesList, 1));

        JPanel sequenceActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        sequenceActions.add(degreeBox);
        sequenceActions.add(addDegree);
        sequenceActions.add(removeDegree);
        sequenceActions.add(moveUp);
        sequenceActions.add(moveDown);
        JPanel sequence = new JPanel(new BorderLayout(0, 6));
        sequence.add(new JScrollPane(degreesList), BorderLayout.CENTER);
        sequence.add(sequenceActions, BorderLayout.SOUTH);
        JScrollPane sequenceScroll = new JScrollPane(sequence);
        sequenceScroll.setPreferredSize(new Dimension(420, 230));

        JPanel form = new JPanel(new BorderLayout(0, 8));
        JPanel namePanel = new JPanel(new BorderLayout(0, 4));
        namePanel.add(new JLabel("Nome da cadência"), BorderLayout.NORTH);
        namePanel.add(nameField, BorderLayout.CENTER);
        form.add(namePanel, BorderLayout.NORTH);
        JPanel degreesPanel = new JPanel(new BorderLayout(0, 4));
        degreesPanel.add(new JLabel("Monte a sequência dos graus (a ordem define a cadência):"),
                BorderLayout.NORTH);
        degreesPanel.add(sequenceScroll, BorderLayout.CENTER);
        form.add(degreesPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Nova cadência personalizada" : "Editar cadência",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        List<Integer> degrees = new ArrayList<>();
        for (int index = 0; index < degreesModel.size(); index++) {
            degrees.add(degreesModel.get(index));
        }
        if (name.isEmpty() || degrees.isEmpty()) {
            showMessage("Informe um nome e adicione pelo menos um grau à cadência.");
            return;
        }
        if (degrees.stream().anyMatch(degree -> degree < 1 || degree > degreeCount)) {
            showMessage("Todos os graus precisam existir na escala selecionada.");
            return;
        }

        try {
            int cadenceId;
            if (existing == null) {
                cadenceId = database.createCadence(name, degrees);
            } else {
                database.updateCadence(existing.id(), name, degrees);
                cadenceId = existing.id();
            }
            refreshCustomCadences();
            selectCustomCadence(cadenceId);
        } catch (SQLException exception) {
            showError("Não foi possível salvar a cadência.", exception);
        }
    }

    private void moveCadenceDegree(DefaultListModel<Integer> model, JList<Integer> list, int offset) {
        int index = list.getSelectedIndex();
        int destination = index + offset;
        if (index < 0 || destination < 0 || destination >= model.size()) {
            return;
        }
        Integer degree = model.remove(index);
        model.add(destination, degree);
        list.setSelectedIndex(destination);
    }

    private void selectCustomCadence(int id) {
        for (int index = 0; index < customCadenceModel.size(); index++) {
            CustomCadence cadence = customCadenceModel.get(index);
            if (cadence.id() == id) {
                customCadenceList.setSelectedIndex(index);
                customCadenceList.ensureIndexIsVisible(index);
                for (int choiceIndex = 0; choiceIndex < cadenceBox.getItemCount(); choiceIndex++) {
                    CadenceChoice choice = cadenceBox.getItemAt(choiceIndex);
                    if (choice.customId() != null && choice.customId() == id) {
                        cadenceBox.setSelectedIndex(choiceIndex);
                        return;
                    }
                }
                return;
            }
        }
    }

    private void deleteCustomCadence() {
        CustomCadence selected = customCadenceList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecione uma cadência personalizada para excluir.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Excluir a cadência \"" + selected.name() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            database.deleteCadence(selected.id());
            refreshCustomCadences();
        } catch (SQLException exception) {
            showError("Não foi possível excluir a cadência.", exception);
        }
    }

    private void syncSelectors(JComboBox<String> other, Object value) {
        if (syncingSelectors) {
            return;
        }
        syncingSelectors = true;
        other.setSelectedItem(value);
        syncingSelectors = false;
        refreshTheory();
    }

    private void refreshFretboard() {
        String key = (String) keyBox.getSelectedItem();
        String scale = (String) scaleBox.getSelectedItem();
        if (key == null || scale == null || fretboardPanel == null) {
            return;
        }
        fretboardPanel.removeAll();
        String[] guitarStrings = {"E4", "B3", "G3", "D3", "A2", "E2"};
        String[] bassStrings = {"G2", "D2", "A1", "E1"};
        String[] tuning = instrumentBox.getSelectedIndex() == 1 ? bassStrings : guitarStrings;
        addFretCell(fretboardPanel, "Corda", true);
        for (int fret = 0; fret <= 12; fret++) {
            addFretCell(fretboardPanel, fret == 0 ? "Solta" : Integer.toString(fret), true);
        }

        List<String> scaleNotes = MusicTheory.scaleNotes(key, scale);
        for (String string : tuning) {
            int openPitch = pitch(string);
            addFretCell(fretboardPanel, string, true);
            for (int fret = 0; fret <= 12; fret++) {
                String note = MusicTheory.NOTES.get((openPitch + fret) % 12);
                boolean inScale = scaleNotes.contains(note);
                boolean root = note.equals(key);
                JLabel cell = new JLabel(inScale ? note : "", SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(root ? new Color(48, 91, 77) : inScale ? new Color(47, 57, 68) : PANEL);
                cell.setForeground(root ? ACCENT : Color.WHITE);
                cell.setFont(new Font("SansSerif", root ? Font.BOLD : Font.PLAIN, 11));
                cell.setBorder(BorderFactory.createLineBorder(new Color(55, 64, 77)));
                String position = fret == 0 ? "Corda solta" : "Traste " + fret;
                cell.setToolTipText(position + " · " + note + (inScale ? " · na escala" : ""));
                fretboardPanel.add(cell);
            }
        }
        fretboardPanel.revalidate();
        fretboardPanel.repaint();
    }

    private int pitch(String noteWithOctave) {
        String note = noteWithOctave.substring(0, noteWithOctave.length() - 1);
        int octave = Character.digit(noteWithOctave.charAt(noteWithOctave.length() - 1), 10);
        return (octave + 1) * 12 + MusicTheory.NOTES.indexOf(note);
    }

    private void addFretCell(JPanel panel, String text, boolean heading) {
        JLabel cell = new JLabel(text, SwingConstants.CENTER);
        cell.setOpaque(true);
        cell.setBackground(heading ? new Color(41, 49, 60) : PANEL);
        cell.setForeground(heading ? ACCENT : Color.WHITE);
        cell.setFont(new Font("SansSerif", Font.BOLD, 11));
        cell.setBorder(BorderFactory.createLineBorder(new Color(55, 64, 77)));
        panel.add(cell);
    }

    private void refreshProgressions() {
        try {
            progressionModel.clear();
            for (Progression progression : database.findAll()) {
                progressionModel.addElement(progression);
            }
        } catch (SQLException exception) {
            showError("Não foi possível carregar as progressões.", exception);
        }
    }

    private void editProgression(Progression existing) {
        editProgression(existing, null);
    }

    private void selectProgressionPreset() {
        List<ProgressionPreset> presets = new ArrayList<>(PRESETS);
        try {
            for (Progression progression : database.findAll()) {
                presets.add(new ProgressionPreset(
                        "Salva · " + progression.name(),
                        progression.name() + " (cópia)",
                        progression.key(),
                        progression.scale(),
                        progression.chords()));
            }
        } catch (SQLException exception) {
            showError("Não foi possível carregar as progressões para usar como modelo.", exception);
            return;
        }

        Object selected = JOptionPane.showInputDialog(
                this,
                "Escolha uma progressão pronta ou salva para usar como ponto de partida:",
                "Usar progressão como modelo",
                JOptionPane.QUESTION_MESSAGE,
                null,
                presets.toArray(),
                presets.get(0));
        if (selected instanceof ProgressionPreset preset) {
            editProgression(null, preset);
        }
    }

    private void editProgression(Progression existing, ProgressionPreset preset) {
        String initialName = existing != null ? existing.name() : preset != null ? preset.name() : "";
        JTextField nameField = new JTextField(initialName, 24);
        JComboBox<String> keyField = new JComboBox<>(MusicTheory.NOTES.toArray(String[]::new));
        JComboBox<String> scaleField = new JComboBox<>(MusicTheory.scaleNames().toArray(String[]::new));
        String initialChords = existing != null ? existing.chords() : preset != null ? preset.chords() : "";
        JTextField chordsField = new JTextField(initialChords, 24);
        if (existing != null) {
            keyField.setSelectedItem(existing.key());
            scaleField.setSelectedItem(existing.scale());
        } else if (preset != null) {
            keyField.setSelectedItem(preset.key());
            scaleField.setSelectedItem(preset.scale());
        } else {
            keyField.setSelectedItem(keyBox.getSelectedItem());
            scaleField.setSelectedItem(scaleBox.getSelectedItem());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        if (preset != null) {
            form.add(new JLabel("Modelo: " + preset.label() + " — o original será preservado."));
        }
        form.add(new JLabel("Nome"));
        form.add(nameField);
        form.add(new JLabel("Tonalidade"));
        form.add(keyField);
        form.add(new JLabel("Escala"));
        form.add(scaleField);
        form.add(new JLabel("Acordes (ex.: Am · F · C · G)"));
        form.add(chordsField);
        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Nova progressão" : "Editar progressão",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String name = nameField.getText().trim();
        String chords = chordsField.getText().trim();
        if (name.isEmpty() || chords.isEmpty()) {
            showMessage("Preencha o nome e os acordes da progressão.");
            return;
        }
        try {
            if (existing == null) {
                database.create(name, (String) keyField.getSelectedItem(),
                        (String) scaleField.getSelectedItem(), chords);
            } else {
                database.update(existing.id(), name, (String) keyField.getSelectedItem(),
                        (String) scaleField.getSelectedItem(), chords);
            }
            refreshProgressions();
        } catch (SQLException exception) {
            showError("Não foi possível salvar a progressão.", exception);
        }
    }

    private void deleteSelected() {
        Progression selected = progressionList.getSelectedValue();
        if (selected == null) {
            showMessage("Selecione uma progressão para excluir.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Excluir a progressão \"" + selected.name() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            database.delete(selected.id());
            refreshProgressions();
        } catch (SQLException exception) {
            showError("Não foi possível excluir a progressão.", exception);
        }
    }

    private record ProgressionPreset(String label, String name, String key, String scale, String chords) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record CadencePreset(String name, List<Integer> degrees) {
    }

    private record CadenceChoice(String name, List<Integer> degrees, Integer customId) {
        @Override
        public String toString() {
            return name + " · " + MusicTheory.formatDegrees(degrees);
        }
    }

    private JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(14, 22, 22, 22));
        return panel;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        return label;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        return button;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Harmonia Studio", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message, Exception exception) {
        JOptionPane.showMessageDialog(this, message + "\n" + exception.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
