package org.apache.commons.math3.genetics;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

public class ElitisticListPopulationTest {

    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        public DummyChromosome(double fitness) {
            this.fitness = fitness;
        }

        public double fitness() {
            return fitness;
        }

        @Override
        public int compareTo(Chromosome another) {
            return Double.compare(this.fitness(), another.fitness());
        }
    }

    @Test
    public void testConstructorWithLimitAndRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.25);
        Assert.assertEquals(100, pop.getPopulationLimit());
        Assert.assertEquals(0.25, pop.getElitismRate(), 1e-6);
        Assert.assertEquals(0, pop.getPopulationSize());
    }

    @Test
    public void testConstructorWithListLimitAndRate() {
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(1.0));
        list.add(new DummyChromosome(2.0));
        ElitisticListPopulation pop = new ElitisticListPopulation(list, 10, 0.5);
        Assert.assertEquals(10, pop.getPopulationLimit());
        Assert.assertEquals(0.5, pop.getElitismRate(), 1e-6);
        Assert.assertEquals(2, pop.getPopulationSize());
    }

    @Test
    public void testSetElitismRateValid() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(0.0);
        Assert.assertEquals(0.0, pop.getElitismRate(), 1e-6);
        pop.setElitismRate(1.0);
        Assert.assertEquals(1.0, pop.getElitismRate(), 1e-6);
        pop.setElitismRate(0.75);
        Assert.assertEquals(0.75, pop.getElitismRate(), 1e-6);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRateTooLow() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(-0.0001);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetElitismRateTooHigh() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setElitismRate(1.0001);
    }

    @Test
    public void testNextGeneration() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        for (int i = 1; i <= 10; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population next = pop.nextGeneration();
        Assert.assertTrue(next instanceof ElitisticListPopulation);
        ElitisticListPopulation elitisticNext = (ElitisticListPopulation) next;

        Assert.assertEquals(pop.getPopulationLimit(), elitisticNext.getPopulationLimit());
        Assert.assertEquals(pop.getElitismRate(), elitisticNext.getElitismRate(), 1e-6);
        Assert.assertEquals(5, elitisticNext.getPopulationSize());

        List<Chromosome> nextChromosomes = elitisticNext.getChromosomes();
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(6.0 + i, nextChromosomes.get(i).getFitness(), 1e-6);
        }
    }

    @Test
    public void testNextGenerationZeroRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.0);
        for (int i = 1; i <= 10; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population next = pop.nextGeneration();
        Assert.assertEquals(0, next.getPopulationSize());
    }

    @Test
    public void testNextGenerationFullRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(5, 1.0);
        for (int i = 1; i <= 5; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population next = pop.nextGeneration();
        Assert.assertEquals(5, next.getPopulationSize());
        List<Chromosome> nextChromosomes = ((ElitisticListPopulation) next).getChromosomes();
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(1.0 + i, nextChromosomes.get(i).getFitness(), 1e-6);
        }
    }

    @Test
    public void testNextGenerationRoundingBoundIndex() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.25);
        for (int i = 1; i <= 10; i++) {
            pop.addChromosome(new DummyChromosome(i));
        }

        Population next = pop.nextGeneration();
        Assert.assertEquals(2, next.getPopulationSize());
        List<Chromosome> nextChromosomes = ((ElitisticListPopulation) next).getChromosomes();
        Assert.assertEquals(9.0, nextChromosomes.get(0).getFitness(), 1e-6);
        Assert.assertEquals(10.0, nextChromosomes.get(1).getFitness(), 1e-6);
    }

    @Test(expected = NotPositiveException.class)
    public void testConstructorInvalidLimit() {
        new ElitisticListPopulation(-1, 0.5);
    }

    @Test(expected = NumberIsTooLargeException.class)
    public void testConstructorOverLimitList() {
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(1.0));
        list.add(new DummyChromosome(2.0));
        new ElitisticListPopulation(list, 1, 0.5);
    }
}
