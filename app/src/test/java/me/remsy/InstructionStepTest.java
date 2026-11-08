package me.remsy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InstructionStepTest {

    @Test
    public void testParseSingleNumberedLine() {
        InstructionStep step = InstructionStep.parse(1, "1. Cook pasta in boiling water.");
        assertEquals(1, step.getStepNumber());
        assertEquals("Cook pasta in boiling water.", step.getInstructionText());
    }

    @Test
    public void testParseStepPrefix() {
        InstructionStep step = InstructionStep.parse(2, "Step 2: Blend ingredients until smooth.");
        assertEquals(2, step.getStepNumber());
        assertEquals("Blend ingredients until smooth.", step.getInstructionText());
    }

    @Test
    public void testParseLineWithoutPrefix() {
        InstructionStep step = InstructionStep.parse(3, "Drain cooked pasta reserving water.");
        assertEquals(3, step.getStepNumber());
        assertEquals("Drain cooked pasta reserving water.", step.getInstructionText());
    }

    @Test
    public void testStorageStringSerialization() {
        InstructionStep step1 = new InstructionStep(1, "Cook pasta.");
        assertEquals("1. Cook pasta.", step1.toStorageString());

        List<InstructionStep> steps = Arrays.asList(
                new InstructionStep(1, "Cook pasta."),
                new InstructionStep(2, "Blend sauce."),
                new InstructionStep(3, "Combine and serve.")
        );

        String serialized = InstructionStep.toStorageString(steps);
        assertEquals("1. Cook pasta.\n2. Blend sauce.\n3. Combine and serve.", serialized);
    }

    @Test
    public void testParseListAndRoundTrip() {
        String raw = "1. Cook pasta.\nStep 2: Blend sauce.\n3) Combine and serve.";
        List<InstructionStep> steps = InstructionStep.parseList(raw);

        assertEquals(3, steps.size());
        assertEquals(1, steps.get(0).getStepNumber());
        assertEquals("Cook pasta.", steps.get(0).getInstructionText());
        assertEquals(2, steps.get(1).getStepNumber());
        assertEquals("Blend sauce.", steps.get(1).getInstructionText());
        assertEquals(3, steps.get(2).getStepNumber());
        assertEquals("Combine and serve.", steps.get(2).getInstructionText());

        String roundTrip = InstructionStep.toStorageString(steps);
        assertEquals("1. Cook pasta.\n2. Blend sauce.\n3. Combine and serve.", roundTrip);
    }
}
