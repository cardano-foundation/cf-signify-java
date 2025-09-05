package org.cardanofoundation.signify.cesr;

import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SithTest {

    @Test
    @DisplayName("Should create StringSith correctly")
    void shouldCreateStringSith() {
        Sith sith = Sith.fromString("2");
        assertTrue(sith instanceof Sith.StringSith);
        assertEquals("2", ((Sith.StringSith) sith).getValue());
        assertEquals("2", sith.toString());
    }

    @Test
    @DisplayName("Should create IntegerSith correctly")
    void shouldCreateIntegerSith() {
        Sith sith = Sith.fromInteger(3);
        assertTrue(sith instanceof Sith.IntegerSith);
        assertEquals(3, ((Sith.IntegerSith) sith).getValue());
        assertEquals("3", sith.toString());
    }

    @Test
    @DisplayName("Should create WeightedSith from fractions correctly")
    void shouldCreateWeightedSithFromFractions() {
        List<List<Fraction>> weightedThreshold = new ArrayList<>();
        List<Fraction> clause1 = Arrays.asList(
                new Fraction(1, 2),
                new Fraction(1, 2),
                new Fraction(1, 4)
        );
        List<Fraction> clause2 = Arrays.asList(
                new Fraction(1, 3),
                new Fraction(1, 3),
                new Fraction(1, 3)
        );
        weightedThreshold.add(clause1);
        weightedThreshold.add(clause2);

        Sith sith = Sith.fromFractionWeighted(weightedThreshold);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        assertEquals(weightedThreshold, weightedSith.getWeightedFractionValue());
    }
    
    @Test
    @DisplayName("Should create WeightedSith from string list correctly")
    void shouldCreateWeightedSithFromStringList() {
        List<String> weightedClause = Arrays.asList("1/2", "1/2", "1/4");
        
        Sith sith = Sith.fromStringWeighted(weightedClause);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        List<List<Fraction>> expected = new ArrayList<>();
        expected.add(Arrays.asList(new Fraction(1, 2), new Fraction(1, 2), new Fraction(1, 4)));
        
        List<List<Fraction>> actual = weightedSith.getWeightedFractionValue();
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            List<Fraction> expectedClause = expected.get(i);
            List<Fraction> actualClause = actual.get(i);
            assertEquals(expectedClause.size(), actualClause.size());
            
            for (int j = 0; j < expectedClause.size(); j++) {
                assertEquals(expectedClause.get(j), actualClause.get(j));
            }
        }
    }
    
    @Test
    @DisplayName("Should create WeightedSith from string list of lists correctly")
    void shouldCreateWeightedSithFromStringListOfLists() {
        List<List<String>> weightedThreshold = new ArrayList<>();
        weightedThreshold.add(Arrays.asList("1/2", "1/2", "1/4"));
        weightedThreshold.add(Arrays.asList("1/3", "1/3", "1/3"));
        
        Sith sith = Sith.fromStringWeightedLst(weightedThreshold);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        List<List<Fraction>> expected = new ArrayList<>();
        expected.add(Arrays.asList(new Fraction(1, 2), new Fraction(1, 2), new Fraction(1, 4)));
        expected.add(Arrays.asList(new Fraction(1, 3), new Fraction(1, 3), new Fraction(1, 3)));
        
        List<List<Fraction>> actual = weightedSith.getWeightedFractionValue();
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            List<Fraction> expectedClause = expected.get(i);
            List<Fraction> actualClause = actual.get(i);
            assertEquals(expectedClause.size(), actualClause.size());
            
            for (int j = 0; j < expectedClause.size(); j++) {
                assertEquals(expectedClause.get(j), actualClause.get(j));
            }
        }
    }

    @Test
    @DisplayName("Should create Sith from Object - String case")
    void shouldCreateSithFromObjectString() {
        Object value = "4";
        Sith sith = Sith.fromObject(value);
        assertTrue(sith instanceof Sith.StringSith);
        assertEquals("4", ((Sith.StringSith) sith).getValue());
    }

    @Test
    @DisplayName("Should create Sith from Object - Integer case")
    void shouldCreateSithFromObjectInteger() {
        Object value = 5;
        Sith sith = Sith.fromObject(value);
        assertTrue(sith instanceof Sith.IntegerSith);
        assertEquals(5, ((Sith.IntegerSith) sith).getValue());
    }
    
    @Test
    @DisplayName("Should create Sith from Object - Number (non-Integer) case")
    void shouldCreateSithFromObjectNumber() {
        Object value = 5.0;
        Sith sith = Sith.fromObject(value);
        assertTrue(sith instanceof Sith.IntegerSith);
        assertEquals(5, ((Sith.IntegerSith) sith).getValue());
    }

    @Test
    @DisplayName("Should create Sith from Object - List of String case")
    void shouldCreateSithFromObjectListString() {
        List<List<String>> weightedThreshold = new ArrayList<>();
        weightedThreshold.add(Arrays.asList("1/2", "1/2", "1/4"));
        weightedThreshold.add(Arrays.asList("1/3", "1/3", "1/3"));
        
        Sith sith = Sith.fromObject(weightedThreshold);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        List<List<Fraction>> expected = new ArrayList<>();
        expected.add(Arrays.asList(new Fraction(1, 2), new Fraction(1, 2), new Fraction(1, 4)));
        expected.add(Arrays.asList(new Fraction(1, 3), new Fraction(1, 3), new Fraction(1, 3)));
        
        List<List<Fraction>> actual = weightedSith.getWeightedFractionValue();
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            List<Fraction> expectedClause = expected.get(i);
            List<Fraction> actualClause = actual.get(i);
            assertEquals(expectedClause.size(), actualClause.size());
            
            for (int j = 0; j < expectedClause.size(); j++) {
                assertEquals(expectedClause.get(j), actualClause.get(j));
            }
        }
    }
    
    @Test
    @DisplayName("Should create Sith from Object - List of Numbers case")
    void shouldCreateSithFromObjectListNumbers() {
        List<List<Integer>> weightedThreshold = new ArrayList<>();
        weightedThreshold.add(Arrays.asList(1, 2, 3));
        weightedThreshold.add(Arrays.asList(4, 5, 6));
        
        Sith sith = Sith.fromObject(weightedThreshold);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        List<List<Fraction>> expected = new ArrayList<>();
        expected.add(Arrays.asList(new Fraction(1), new Fraction(2), new Fraction(3)));
        expected.add(Arrays.asList(new Fraction(4), new Fraction(5), new Fraction(6)));
        
        List<List<Fraction>> actual = weightedSith.getWeightedFractionValue();
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            List<Fraction> expectedClause = expected.get(i);
            List<Fraction> actualClause = actual.get(i);
            assertEquals(expectedClause.size(), actualClause.size());
            
            for (int j = 0; j < expectedClause.size(); j++) {
                assertEquals(expectedClause.get(j), actualClause.get(j));
            }
        }
    }
    
    @Test
    @DisplayName("Should create Sith from Object - Flat list case")
    void shouldCreateSithFromObjectFlatList() {
        List<String> flatList = Arrays.asList("1/2", "1/2", "1/4");
        
        Sith sith = Sith.fromObject(flatList);
        assertTrue(sith instanceof Sith.WeightedSith);
        
        Sith.WeightedSith weightedSith = (Sith.WeightedSith) sith;
        List<List<Fraction>> expected = new ArrayList<>();
        expected.add(Arrays.asList(new Fraction(1, 2), new Fraction(1, 2), new Fraction(1, 4)));
        
        List<List<Fraction>> actual = weightedSith.getWeightedFractionValue();
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            List<Fraction> expectedClause = expected.get(i);
            List<Fraction> actualClause = actual.get(i);
            assertEquals(expectedClause.size(), actualClause.size());
            
            for (int j = 0; j < expectedClause.size(); j++) {
                assertEquals(expectedClause.get(j), actualClause.get(j));
            }
        }
    }

    @Test
    @DisplayName("Should throw exception for null Sith value")
    void shouldThrowExceptionForNullSithValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Sith.fromObject(null);
        });
        assertEquals("Sith value cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for unsupported Sith value type")
    void shouldThrowExceptionForUnsupportedSithValueType() {
        Object value = new Object();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Sith.fromObject(value);
        });
        assertTrue(exception.getMessage().contains("Unsupported Sith value type"));
    }
    
    @Test
    @DisplayName("Should throw exception for mixed list structure")
    void shouldThrowExceptionForMixedListStructure() {
        List<Object> mixedList = new ArrayList<>();
        mixedList.add(Arrays.asList("1/2", "1/2"));
        mixedList.add("not a list");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Sith.fromObject(mixedList);
        });
        assertTrue(exception.getMessage().contains("Expected list of lists, but found mixed structure"));
    }
    
}