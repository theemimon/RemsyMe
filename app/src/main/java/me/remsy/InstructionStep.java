package me.remsy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class InstructionStep {
    private int stepNumber;
    private String instructionText;

    public InstructionStep() {
        this(1, "");
    }

    public InstructionStep(int stepNumber, String instructionText) {
        this.stepNumber = stepNumber;
        this.instructionText = instructionText != null ? instructionText.trim() : "";
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getInstructionText() {
        return instructionText;
    }

    public void setInstructionText(String instructionText) {
        this.instructionText = instructionText != null ? instructionText.trim() : "";
    }

    public String toStorageString() {
        if (instructionText.isEmpty()) return "";
        return stepNumber + ". " + instructionText;
    }

    public static String toStorageString(List<InstructionStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (InstructionStep step : steps) {
            if (step == null) continue;
            String text = step.getInstructionText();
            if (!text.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(count).append(". ").append(text);
                count++;
            }
        }
        return sb.toString();
    }

    private static final Pattern STEP_PREFIX_PATTERN = Pattern.compile("^(?:Step|step)?\\s*\\d+[.:)]\\s*");

    public static InstructionStep parse(int stepNumber, String rawLine) {
        if (rawLine == null || rawLine.trim().isEmpty()) {
            return new InstructionStep(stepNumber, "");
        }

        String cleaned = rawLine.replaceAll("^[•\\-*\\s]+", "").trim();
        cleaned = STEP_PREFIX_PATTERN.matcher(cleaned).replaceAll("").trim();

        return new InstructionStep(stepNumber, cleaned);
    }

    public static List<InstructionStep> parseList(String rawInstructions) {
        List<InstructionStep> result = new ArrayList<>();
        if (rawInstructions == null || rawInstructions.trim().isEmpty()) {
            return result;
        }

        String[] lines = rawInstructions.split("\r?\n");
        int stepNum = 1;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                InstructionStep step = parse(stepNum, trimmed);
                if (!step.getInstructionText().isEmpty()) {
                    result.add(step);
                    stepNum++;
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstructionStep that = (InstructionStep) o;
        return stepNumber == that.stepNumber && Objects.equals(instructionText, that.instructionText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepNumber, instructionText);
    }
}
