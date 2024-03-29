import java.util.ArrayList;

public class Individual {
	
	Location depot;
	ArrayList<Location> chromosome;
	double fitness = -1;
	static int dist[][];
	
	public Individual() {
		
	}
	
	public Individual(Problem problem) {
		depot = problem.depot;
		chromosome = new ArrayList<>();
		ArrayList<Location> chromoCopy = new ArrayList<>();
		for(int i = 0; i < problem.customers.size(); i++) {
			Location l = problem.customers.get(i);
			Location locCopy  = new Location(l.idx, l.x, l.y);
			chromoCopy.add(locCopy);
		}
		while(chromoCopy.size() > 0) {
			int idx = EA.random.nextInt(chromoCopy.size());
			chromosome.add(chromoCopy.remove(idx));
		}
		dist = new int[chromosome.size() + 1][chromosome.size() + 1];		
		evaluate();
	}
	
	void evaluate() {
		double distance = calcDistance(depot, chromosome.get(0));
		for(int i = 0; i < chromosome.size() - 1; i++) {
			distance += calcDistance(chromosome.get(i), chromosome.get(i + 1));
//			System.out.println(calcDistance(chromosome.get(i), chromosome.get(i + 1)));
		}
		distance += calcDistance(chromosome.get(chromosome.size() - 1), depot);
		fitness = distance;
	}
	
	//nearest int after calc on floats
	private int calcDistance(Location loc1, Location loc2) {
//		System.out.println(loc1.idx + "\t" + loc2.idx);
		if(dist[loc1.idx - 1][loc2.idx - 1] != 0) {
			return dist[loc1.idx - 1][loc2.idx - 1];
		}
		double euclideanDistance = Math.sqrt(Math.pow(loc1.x - loc2.x, 2) + Math.pow(loc1.y - loc2.y, 2));
		dist[loc1.idx - 1][loc2.idx - 1] = (int) Math.round(euclideanDistance);
		dist[loc2.idx - 1][loc1.idx - 1] = (int) Math.round(euclideanDistance);
		return dist[loc1.idx - 1][loc2.idx - 1];
	}
	
	@Override
	public String toString() {
		String str = "" + depot.idx + ", ";
		for(Location l : chromosome) {
			str += l.idx + ", ";
		}
		str += fitness;
		return str;
	}

	public Individual copy() {
		Individual individual = new Individual();
		individual.depot = this.depot;
		individual.chromosome = new ArrayList<>();
		for(Location loc : this.chromosome) {
			Location locCopy = loc.copy();
			individual.chromosome.add(locCopy);
		}
		individual.fitness = this.fitness;
		return individual;
	}
	
	public boolean contains(Location location) {
		for(int i = 0; i < chromosome.size(); i++) {
			if(chromosome.get(i) != null && location != null) {
				try {
					if(chromosome.get(i).idx == location.idx) {
						return true;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
