package org.simulation.utils;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralTest {

    @Test
    public void calculateMeanTest() {
        assertEquals(0.7268117422595363, General.calculateMean(List.of(
                0.7696220706954784, 0.7684110626052383, 0.7637366602608476, 0.7779142402366792,
                0.9306678063466316, 0.9279173707148145, 0.9177734502766002, 0.9042029547285024, 0.8697097102491369,
                0.8578567151525288, 0.12687596221616237, 0.10705290363181486)));
    }

    @Test
    public void calculateConfidenceIntervalTest() {
        assertEquals(List.of(
                0.7696220706954784, 0.7684110626052383), General.calculateConfidenceInterval(List.of(
                0.7696220706954784, 0.7684110626052383, 0.7637366602608476, 0.7779142402366792,
                0.9306678063466316, 0.9279173707148145, 0.9177734502766002, 0.9042029547285024, 0.8697097102491369,
                0.8578567151525288, 0.12687596221616237, 0.10705290363181486), 80));
    }
}
