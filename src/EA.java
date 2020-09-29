import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class EA extends Observable implements Runnable{

	String filename = "berlin52.tsp";// optimal 7544
	Problem problem = new Problem(filename);
	static Random random = new Random();
	ArrayList<Individual> population;
	Individual best;
	int popSize = 1000;
	int tournamentSize;
	int maxGenerations = 1000000;		
	int generation;
	int pause = 0;//set to zero for max speed
	
	@Override
	public void run() {		
		population = new ArrayList<>();
		Gui gui = new Gui();
		addObserver(gui);
		
		//initialise population. The Individual constructor generates a random permutation of customers (Locations)
		for(int i = 0; i < popSize; i++) {			
			Individual individual = new Individual(problem);
			population.add(individual);
			//System.out.println(individual);
		}
		
		generation = 0;
		while(generation < maxGenerations) {
			generation++;
			Individual parent1 = select();
			Individual parent2 = select();
			Individual child = crossover(parent1, parent2);
			child = mutate(child);
			child.evaluate();
			replace(child);
			best = getBest();
			printStats(generation);
			setChanged();
			notifyObservers(best);
			//pause so can see the effect
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void printStats(int generation) {
		System.out.println(generation + "\t" + best.fitness);
	}

	private Individual getBest() {
		Individual bestInPop = null;
		for(Individual individual : population) {
			if(bestInPop == null || individual.fitness < bestInPop.fitness) {
				bestInPop = individual;
			}
		}
		return bestInPop;
	}

	private Individual getWorst() {
		Individual worstInPop = null;
		for(Individual individual : population) {
			if(worstInPop == null || individual.fitness > worstInPop.fitness) {
				worstInPop = individual;
			}
		}
		return worstInPop;
	}
	
	
	private void replace(Individual child) {
		Individual worst = getWorst();
		if(child.fitness < worst.fitness) {
			population.set(population.indexOf(worst), child);
		}
	}

	//swap two locations
	private Individual mutate(Individual child) {	
		Location temp;
		int idx1 = random.nextInt(child.chromosome.size());
		int idx2 = random.nextInt(child.chromosome.size());
		
		temp = child.chromosome.get(idx1);
		child.chromosome.set(idx1, child.chromosome.get(idx2));
		child.chromosome.set(idx2, temp);
		return child;
	}

	//simple crossover. Probably not very good
	private Individual crossover(Individual parent1, Individual parent2) {
		Individual child = new Individual();
		child.depot = parent1.depot.copy();
		child.chromosome = new ArrayList<>();
		int cutPoint = random.nextInt(parent1.chromosome.size());
		
		//add from parent1 up to cutpoint
		for(int i = 0; i < cutPoint; i++) {
			child.chromosome.add(parent1.chromosome.get(i).copy());
		}

		//1 is depot. Add all location indices 
		ArrayList<Integer> locationIndexesNotUsed = new ArrayList<>();
		for(int i = 2; i < problem.customers.size() + 2; i++) {
			locationIndexesNotUsed.add(i);
		}
		
		//remove indices copied from parent1
		for(int i = 0; i < child.chromosome.size(); i++) {
			int idx = child.chromosome.get(i).idx;
			int usedIdx = locationIndexesNotUsed.indexOf(idx);
			locationIndexesNotUsed.remove(usedIdx);
		}
		
		//add locations not used from cutpoint to end of parent2
		for(int i = cutPoint; i < parent2.chromosome.size(); i++) {
			Location loc = parent2.chromosome.get(i); 
			if(locationIndexesNotUsed.contains(loc.idx)) {
				child.chromosome.add(loc.copy());
				int usedIdx = locationIndexesNotUsed.indexOf(loc.idx);
				locationIndexesNotUsed.remove(usedIdx);
			}
		}
		
		//add remaining locations not in child 
		for(int i : locationIndexesNotUsed) {
			//Problem has locations in order starting with location 2 
			Location loc = problem.customers.get(i - 2).copy();
			child.chromosome.add(loc);			
		}
		
		//check 1
		if(child.chromosome.size() != parent1.chromosome.size()) {
			System.err.println("Error in crossover wrong size " + child.chromosome.size());
			System.exit(-1);
		}
		
		//check 2. All indices from 2 .. end should be included
		ArrayList<Integer> indexCheck = new ArrayList<>();
		for(int i = 2; i < problem.customers.size() + 2; i++) {
			indexCheck.add(i);
		}		
		for(Location loc : child.chromosome) {
			int usedIdx = indexCheck.indexOf(loc.idx);
			indexCheck.remove(usedIdx);
		}
		if(indexCheck.size() != 0) {
			System.err.println("Unused indices " + child);
			System.exit(-1);
		}		
		
		return child;
	}

	private Individual select() {
		Individual winner = population.get(random.nextInt(popSize));
		for(int i = 1; i < tournamentSize; i++) {
			Individual candidate2 = population.get(random.nextInt(popSize));
			if(candidate2.fitness < winner.fitness) {
				winner = candidate2;
			}
		}
		return winner.copy();
	}

	public static void main(String[] args) {
		EA ea = new EA();
		Thread t = new Thread(ea);
		t.start();				
	}
	
}
