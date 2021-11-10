import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class EA extends Observable implements Runnable {

	private static final Object lock = new Object();
//	String filename = "dj38.tsp";
//	String filename = "dsj1000.tsp";// optimal 18,659,688 or 1.8 E7 Concorde gets 18659
//	String filename = "rat99.tsp";// optimal 1211
//	String filename = "burma14.tsp";
	String filename = "berlin52.tsp";// 7542
	Problem problem = new Problem(filename);
	static Random random = new Random();
	ArrayList<Individual> population;
	Individual best;
	int popSize = 40;
	int tournamentSize = 2;
	int maxGenerations = 50000;
	int generation;
	int pause = 0;// set to zero for max speed
	double mutationRate = 0.5;
	private ArrayList<EA> islands;

	@Override
	public void run() {
		population = new ArrayList<>();
		

		// initialise population. The Individual constructor generates a random
		// permutation of customers (Locations)
		for (int i = 0; i < popSize; i++) {
			Individual individual = new Individual(problem);
			population.add(individual);
			// System.out.println(individual);
		}
		best = getBest();
		generation = 0;

		while (generation < maxGenerations) {
			generation++;
			
			
			
			ArrayList<Individual> pop2 = new ArrayList<>();
			if(generation % 10 == 0) {//100
//				System.out.println("do the hussle " + islands.indexOf(this));
				synchronized (lock) {
					//same island or in loop different .... original was same island and popsize / 10
					int idx = random.nextInt(islands.size());
					for(int i = 0; i < popSize / 100; i++) {
//						int idx = random.nextInt(islands.size());
						pop2.add(islands.get(idx).population.get(random.nextInt(popSize)).copy());
					}
					//add best 
//					pop2.add(islands.get(idx).best.copy());
					
					//add 1 from each?
//					for(EA ea : islands) {
//						pop2.add(ea.population.get(random.nextInt(popSize)));
//					}
				}				
			}
			// elitism
			
			pop2.add(best.copy());
			while (pop2.size() < popSize) {
				Individual parent1 = select();
				Individual parent2 = select();
				ArrayList<Individual> children = null;
				if (random.nextBoolean()) {
					children = crossover(parent1, parent2);
				} else {
					children = pmxCrossover(parent1, parent2);
				}

				if (random.nextDouble() < mutationRate) {
					if (random.nextBoolean()) {
						children = mutate2Opt(children);
					} else {
						children = mutate(children);
					}
				}

				for (Individual child : children) {
					child.evaluate();
					pop2.add(child);
					// replace(child);
				}

				// pause so can see the effect
				try {
					Thread.sleep(pause);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			synchronized (lock) {
				population = pop2;
			}
			Individual bestCandidate = getBest();
			if (bestCandidate.fitness < best.fitness) {
				best = bestCandidate;
			}
//			printStats(generation);
			setChanged();
			notifyObservers(bestCandidate);
		}
		Individual theIslandBest = null;
		for(EA ea : islands) {
			if(theIslandBest == null || ea.best.fitness < theIslandBest.fitness) {
				theIslandBest = ea.best;
			}
		}
		setChanged();
		notifyObservers(best);
		System.out.println(theIslandBest.fitness);
	}

	private ArrayList<Individual> mutate2Opt(ArrayList<Individual> children) {
		ArrayList<Individual> result = new ArrayList<>();
		for (Individual child : children) {
			int cut1 = random.nextInt(child.chromosome.size() - 1);
			int cut2 = cut1 + random.nextInt(child.chromosome.size() - cut1);
			Individual individual = new Individual(problem);
			int i = 0;
			for (i = 0; i < cut1; i++) {
				individual.chromosome.set(i, child.chromosome.get(i));
			}
			for (int k = cut2; k >= cut1; k--) {
				individual.chromosome.set(i, child.chromosome.get(k));
				i++;
			}
			for (i = cut2 + 1; i < individual.chromosome.size(); i++) {
				individual.chromosome.set(i, child.chromosome.get(i));
			}
			result.add(individual);
		}
		return result;
	}

	private void printStats(int generation) {
		System.out.println(generation + "\t" + best.fitness);
	}

	private Individual getBest() {
		Individual bestInPop = null;
		for (Individual individual : population) {
			if (bestInPop == null || individual.fitness < bestInPop.fitness) {
				bestInPop = individual;
			}
		}
		return bestInPop.copy();
	}

	private Individual getWorst() {
		Individual worstInPop = null;
		for (Individual individual : population) {
			if (worstInPop == null || individual.fitness > worstInPop.fitness) {
				worstInPop = individual;
			}
		}
		return worstInPop;
	}

	private void replace(Individual child) {
		Individual worst = getWorst();
		if (child.fitness < worst.fitness) {
			population.set(population.indexOf(worst), child);
		}
	}

	// swap two locations
	private ArrayList<Individual> mutate(ArrayList<Individual> children) {
		
		for (Individual child : children) {
			Location temp;
			int idx1 = random.nextInt(child.chromosome.size());
			int idx2 = random.nextInt(child.chromosome.size());

			temp = child.chromosome.get(idx1);
			child.chromosome.set(idx1, child.chromosome.get(idx2));
			child.chromosome.set(idx2, temp);
		}
		return children;
	}

	private ArrayList<Individual> pmxCrossover(Individual parent1, Individual parent2) {
		int xPoint1 = random.nextInt(parent1.chromosome.size());
		int xPoint2 = random.nextInt(parent1.chromosome.size());
		if (xPoint1 > xPoint2) {
			int temp = xPoint1;
			xPoint1 = xPoint2;
			xPoint2 = temp;
		}

		Individual child1 = new Individual();
		child1.depot = parent1.depot;
		child1.chromosome = new ArrayList<>();
		Individual child2 = new Individual();
		child2.depot = parent1.depot;
		child2.chromosome = new ArrayList<>();
		for (int i = 0; i < parent1.chromosome.size(); i++) {
			child1.chromosome.add(null);
			child2.chromosome.add(null);
		}

		// crossover between cut points
		for (int i = xPoint1; i <= xPoint2; i++) {
			child1.chromosome.set(i, parent2.chromosome.get(i));
			child2.chromosome.set(i, parent1.chromosome.get(i));
		}

		// fill up to xpoint1 from original parent if possible
		for (int i = 0; i < xPoint1; i++) {
			if (!child1.contains(parent1.chromosome.get(i))) {
				child1.chromosome.set(i, parent1.chromosome.get(i));
			}
			if (!child2.contains(parent2.chromosome.get(i))) {
				child2.chromosome.set(i, parent2.chromosome.get(i));
			}
		}

		// fill from after xpoint2 from original parent if possible
		for (int i = xPoint2 + 1; i < parent1.chromosome.size(); i++) {
			if (!child1.contains(parent1.chromosome.get(i))) {
				child1.chromosome.set(i, parent1.chromosome.get(i));
			}
			if (!child2.contains(parent2.chromosome.get(i))) {
				child2.chromosome.set(i, parent2.chromosome.get(i));
			}
		}

		// fill in remainder of child1 based on map;
		for (int i = 0; i < parent1.chromosome.size(); i++) {
			if (child1.chromosome.get(i) == null) {
				Boolean filled = false;
				// this value is already in the child as it wasnt set in previous steps
				Location locFromParent = parent1.chromosome.get(i);
				while (!filled) {
					// look up the corresponding mapped value (the value at the same position in the
					// other child where valFromParent exists)
					Location locFromOtherChild = getMappedVal(child1, child2, locFromParent);
					if (!child1.contains(locFromOtherChild)) {
						child1.chromosome.set(i, locFromOtherChild);
						filled = true;
					} else {
						// continue while loop until we find a value not in the child
						locFromParent = locFromOtherChild;
					}
				}

			}
		}

		// do the same for child2
		for (int i = 0; i < parent1.chromosome.size(); i++) {
			if (child2.chromosome.get(i) == null) {
				Boolean filled = false;
				// this value is already in the child as it wasnt set in previous steps
				Location locFromParent = parent2.chromosome.get(i);
				while (!filled) {
					// look up the corresponding mapped value (the value at the same position in the
					// other child where valFromParent exists)
					Location locFromOtherChild = getMappedVal(child2, child1, locFromParent);
					if (!child2.contains(locFromOtherChild)) {
						child2.chromosome.set(i, locFromOtherChild);
						filled = true;
					} else {
						// continue while loop until we find a value not in the child
						locFromParent = locFromOtherChild;
					}
				}

			}
		}
		for (Location l : child1.chromosome) {
			if (l == null) {
				System.err.println();
			}
		}
		for (Location l : child2.chromosome) {
			if (l == null) {
				System.err.println();
			}
		}
		ArrayList<Individual> children = new ArrayList<>();
		children.add(child1);
		children.add(child2);
		return children;
	}

	/**
	 * Return the customer in the same position in child2 as the customer defined by
	 * locFromParent is in child1
	 * 
	 * @param child1
	 * @param child2
	 * @param locFromParent
	 * @return
	 */
	private Location getMappedVal(Individual child1, Individual child2, Location locFromParent) {

		for (int i = 0; i < child1.chromosome.size(); i++) {
			if (child1.chromosome.get(i) != null) {
				if (child1.chromosome.get(i).idx == locFromParent.idx) {
					return child2.chromosome.get(i);
				}
			}
		}

		return null;

	}

	// simple crossover. Probably not very good. Long winded with customer indices
	private ArrayList<Individual> crossover(Individual parent1, Individual parent2) {
		Individual child = new Individual();
		child.depot = parent1.depot.copy();
		child.chromosome = new ArrayList<>();
		int cutPoint = random.nextInt(parent1.chromosome.size());

		// add from parent1 up to cutpoint
		for (int i = 0; i < cutPoint; i++) {
			child.chromosome.add(parent1.chromosome.get(i).copy());
		}

		// 1 is depot. Add all location indices
		ArrayList<Integer> locationIndexesNotUsed = new ArrayList<>();
		for (int i = 2; i < problem.customers.size() + 2; i++) {
			locationIndexesNotUsed.add(i);
		}

		// remove indices copied from parent1
		for (int i = 0; i < child.chromosome.size(); i++) {
			int idx = child.chromosome.get(i).idx;
			int usedIdx = locationIndexesNotUsed.indexOf(idx);
			locationIndexesNotUsed.remove(usedIdx);
		}

		// add locations not used from cutpoint to end of parent2
		for (int i = cutPoint; i < parent2.chromosome.size(); i++) {
			Location loc = parent2.chromosome.get(i);
			if (locationIndexesNotUsed.contains(loc.idx)) {
				child.chromosome.add(loc.copy());
				int usedIdx = locationIndexesNotUsed.indexOf(loc.idx);
				locationIndexesNotUsed.remove(usedIdx);
			}
		}

		// add remaining locations not in child
		for (int i : locationIndexesNotUsed) {
			// Problem has locations in order starting with location 2
			Location loc = problem.customers.get(i - 2).copy();
			child.chromosome.add(loc);
		}

		// check 1
		if (child.chromosome.size() != parent1.chromosome.size()) {
			System.err.println("Error in crossover wrong size " + child.chromosome.size());
			System.exit(-1);
		}

		// check 2. All indices from 2 .. end should be included
		ArrayList<Integer> indexCheck = new ArrayList<>();
		for (int i = 2; i < problem.customers.size() + 2; i++) {
			indexCheck.add(i);
		}
		for (Location loc : child.chromosome) {
			int usedIdx = indexCheck.indexOf(loc.idx);
			indexCheck.remove(usedIdx);
		}
		if (indexCheck.size() != 0) {
			System.err.println("Unused indices " + child);
			System.exit(-1);
		}
		ArrayList<Individual> children = new ArrayList<>();
		children.add(child);
		return children;
	}

	private Individual select() {
		Individual winner = population.get(random.nextInt(popSize));
		for (int i = 1; i < tournamentSize; i++) {
			Individual candidate2 = population.get(random.nextInt(popSize));
			if (candidate2.fitness < winner.fitness) {
				winner = candidate2;
			}
		}
		return winner.copy();
	}

	public static void main(String[] args) {
		
		ArrayList<EA> islands = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			Gui gui = new Gui(i);
			EA ea = new EA();
			islands.add(ea);
			ea.addObserver(gui);			
		}
		for(EA ea : islands) {
			ea.islands = islands;
			Thread t = new Thread(ea);
			t.start();		
		}			
	}

}
