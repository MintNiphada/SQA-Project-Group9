package org.apache.commons.math3.genetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.junit.Assert;
import org.junit.Test;

public class ListPopulationTest {

    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        public DummyChromosome(final double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }
    }

    private static class DummyListPopulation extends ListPopulation {
        public DummyListPopulation(final int populationLimit) {
            super(populationLimit);
        }

        public DummyListPopulation(final List<Chromosome> chromosomes, final int populationLimit) {
            super(chromosomes, populationLimit);
        }

        @Override
        public Population nextGeneration() {
            return this;
        }

        public List<Chromosome> accessChromosomeList() {
            return super.getChromosomeList();
        }
    }

    @Test
    public void testConstructorWithLimitOnly() {
        ListPopulation pop = new DummyListPopulation(10);
        Assert.assertEquals(10, pop.getPopulationLimit());
        Assert.assertEquals(0, pop.getPopulationSize());
        Assert.assertTrue(pop.getChromosomes().isEmpty());
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorWithZeroLimit() {
        new DummyListPopulation(0);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorWithNegativeLimit() {
        new DummyListPopulation(-5);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorNullChromosomes() {
        new DummyListPopulation(null, 10);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorChromosomesNegativeLimit() {
        new DummyListPopulation(new ArrayList<Chromosome>(), -1);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorChromosomesExceedsLimit() {
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(1.0),
            new DummyChromosome(2.0),
            new DummyChromosome(3.0)
        );
        new DummyListPopulation(list, 2);
    }

    @Test
    public void testConstructorValid() {
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(1.0),
            new DummyChromosome(2.0)
        );
        DummyListPopulation pop = new DummyListPopulation(list, 2);
        Assert.assertEquals(2, pop.getPopulationLimit());
        Assert.assertEquals(2, pop.getPopulationSize());
        Assert.assertEquals(list, pop.accessChromosomeList());
    }

    @Test(expected = NullArgumentException.class)
    public void testSetChromosomesNull() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.setChromosomes(null);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testSetChromosomesTooLarge() {
        ListPopulation pop = new DummyListPopulation(2);
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(1.0),
            new DummyChromosome(2.0),
            new DummyChromosome(3.0)
        );
        pop.setChromosomes(list);
    }

    @Test
    public void testSetChromosomesValid() {
        DummyListPopulation pop = new DummyListPopulation(10);
        pop.addChromosome(new DummyChromosome(0.5));
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(1.0),
            new DummyChromosome(2.0)
        );
        pop.setChromosomes(list);
        Assert.assertEquals(2, pop.getPopulationSize());
        Assert.assertEquals(list, pop.accessChromosomeList());
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testAddChromosomesTooLarge() {
        ListPopulation pop = new DummyListPopulation(2);
        pop.addChromosome(new DummyChromosome(1.0));
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(2.0),
            new DummyChromosome(3.0)
        );
        pop.addChromosomes(list);
    }

    @Test
    public void testAddChromosomesValid() {
        ListPopulation pop = new DummyListPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        List<Chromosome> list = Arrays.<Chromosome>asList(
            new DummyChromosome(2.0),
            new DummyChromosome(3.0)
        );
        pop.addChromosomes(list);
        Assert.assertEquals(3, pop.getPopulationSize());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetChromosomesUnmodifiable() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.getChromosomes().add(new DummyChromosome(1.0));
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testAddChromosomeLimitReached() {
        ListPopulation pop = new DummyListPopulation(1);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
    }

    @Test
    public void testGetFittestChromosome() {
        Chromosome c1 = new DummyChromosome(10.0);
        Chromosome c2 = new DummyChromosome(50.0);
        Chromosome c3 = new DummyChromosome(30.0);
        Chromosome c4 = new DummyChromosome(50.0);

        ListPopulation pop = new DummyListPopulation(5);
        pop.addChromosome(c1);
        pop.addChromosome(c2);
        pop.addChromosome(c3);
        pop.addChromosome(c4);

        Assert.assertSame(c2, pop.getFittestChromosome());
    }

    @Test
    public void testGetFittestChromosomeSingle() {
        Chromosome c1 = new DummyChromosome(10.0);
        ListPopulation pop = new DummyListPopulation(1);
        pop.addChromosome(c1);
        Assert.assertSame(c1, pop.getFittestChromosome());
    }

    @Test(expected = NotPositiveException.class)
    public void testSetPopulationLimitZero() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.setPopulationLimit(0);
    }

    @Test(expected = NotPositiveException.class)
    public void testSetPopulationLimitNegative() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.setPopulationLimit(-1);
    }

    @Test(expected = NumberIsTooSmallException.class)
    public void testSetPopulationLimitSmallerThanSize() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
        pop.addChromosome(new DummyChromosome(3.0));
        pop.setPopulationLimit(2);
    }

    @Test
    public void testSetPopulationLimitValid() {
        ListPopulation pop = new DummyListPopulation(10);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.setPopulationLimit(5);
        Assert.assertEquals(5, pop.getPopulationLimit());
        pop.setPopulationLimit(1);
        Assert.assertEquals(1, pop.getPopulationLimit());
    }

    @Test
    public void testToString() {
        Chromosome c1 = new DummyChromosome(1.0);
        ListPopulation pop = new DummyListPopulation(5);
        pop.addChromosome(c1);
        Assert.assertEquals(pop.getChromosomes().toString(), pop.toString());
    }

    @Test
    public void testIterator() {
        Chromosome c1 = new DummyChromosome(1.0);
        Chromosome c2 = new DummyChromosome(2.0);
        ListPopulation pop = new DummyListPopulation(5);
        pop.addChromosome(c1);
        pop.addChromosome(c2);

        Iterator<Chromosome> it = pop.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(c1, it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(c2, it.next());
        Assert.assertFalse(it.hasNext());
    }
}
