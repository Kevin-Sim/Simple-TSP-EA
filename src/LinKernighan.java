import java.util.ArrayList;

/**
 * This implementation assumes that the distance between each pair of cities is
 * represented in a 2D dist array, where dist[i][j] is the distance between
 * cities i and j. The input tour is represented as an array of city indices.
 * The method returns the length of the optimized tour.
 * 
 * The implementation starts by creating an index array inx that maps each city
 * to its index in the input tour array. The algorithm then iteratively tries to
 * improve the tour by performing two-opt moves, which swap pairs of edges to
 * create a new tour. Specifically, the algorithm iterates over all pairs of
 * edges (i, i+1) and (j, j+1), and computes the gain in tour length that would
 * result from reversing the order
 * 
 * @author ChatGTP
 *
 */
public class LinKernighan {

	private static final double MAX_DIST = 1e9;

	public static void main(String[] args) {
		Problem p = new Problem("dsj1000.tsp");
		Individual ind = linKernighan(p, false);		
		Gui gui = new Gui(9999);
		gui.individual = ind;		
		System.out.println(ind.fitness + "\t" + ind);				 
	}

	private static int calcDistance(Problem p, int idx1, int idx2) {
//		System.out.println(loc1.idx + "\t" + loc2.idx);	
		Location loc1 = null;
		Location loc2 = null;
		if (idx1 == 0 || idx1 == p.customers.size() + 1) {
			loc1 = p.depot;
		} else {
			loc1 = p.customers.get(idx1 - 1);
		}
		if (idx2 == 0 || idx2 == p.customers.size() + 1) {
			loc2 = p.depot;
		} else {
			loc2 = p.customers.get(idx2 - 1);
		}
//	
		double euclideanDistance = Math.sqrt(Math.pow(loc1.x - loc2.x, 2) + Math.pow(loc1.y - loc2.y, 2));
		return (int)Math.round(euclideanDistance);		
	}

	public static Individual linKernighan(int[] tour, double[][] dist, Problem p, Boolean breakEarly) {
		int n = tour.length;
		int[] inx = new int[n];
		for (int i = 0; i < n; i++) {
			inx[tour[i]] = i;
		}

		boolean improved = true;
		double bestDist = tourLength(tour, dist);

		while (improved) {
			improved = false;

			for (int i = 0; i < n - 3; i++) {
				for (int j = i + 2; j < n - 1; j++) {
					int a = tour[i], b = tour[i + 1];
					int c = tour[j], d = tour[j + 1];

					double ab = dist[a][b], cd = dist[c][d];
					double ac = dist[a][c], bd = dist[b][d];
					double ad = dist[a][d], bc = dist[b][c];
					double gain1 = cd + ab - ac - bd;
					double gain2 = bc + ad - ac - bd;

					if (gain1 > 1e-10 || gain2 > 1e-10) {
						int[] tmpTour = reverse(tour, i + 1, j);
						double tmpDist = tourLength(tmpTour, dist);
						if (tmpDist < bestDist) {
							tour = tmpTour;
							bestDist = tmpDist;
							for (int k = 0; k < n; k++) {
								inx[tour[k]] = k;
							}
							improved = true;
							break;
						}
					}
				}
				if(breakEarly && EA.random.nextDouble() < 0.01) {
					improved = false;
				}
				if (improved)
					break;
			}
		}
		ArrayList<Location> chromoCopy = new ArrayList<>();
		Individual ind = new Individual(p);
		for(int i = 1; i <= tour.length - 2; i++) {
//			System.out.println(tour[i]);
			int idx = tour[i] - 2;
			Location l = p.customers.get(tour[i] - 2);
			Location locCopy  = new Location(l.idx, l.x, l.y);
			chromoCopy.add(locCopy);
		}
		ind.chromosome = chromoCopy;
		
		Individual.dist = new int[p.customers.size() + 1][p.customers.size() + 1];		
		ind.evaluate();		
	
		return ind;
	}

	private static int[] reverse(int[] arr, int i, int j) {
		int[] rev = new int[arr.length];
		System.arraycopy(arr, 0, rev, 0, i);
		for (int k = i, l = j; k <= j; k++, l--) {
			rev[k] = arr[l];
		}
		System.arraycopy(arr, j + 1, rev, j + 1, arr.length - j - 1);
		return rev;
	}

	private static double tourLength(int[] tour, double[][] dist) {
		double length = 0;
		for (int i = 0; i < tour.length - 1; i++) {
			length += dist[tour[i] - 1][tour[i + 1] - 1];
//			System.out.println(dist[tour[i]][tour[i + 1]]);
		}
		length += dist[tour[tour.length - 1]][tour[0]];
		return length;
	}

	public static Individual linKernighan(Problem p, boolean breakEarly) {
		int[] tour = new int[p.customers.size() + 2];
		Individual ind = new Individual(p);
		tour[0] = 1;
		tour[p.customers.size() + 1] = 1;
		for (int i = 1; i < p.customers.size() + 1; i++) {
			tour[i] = ind.chromosome.get(i - 1).idx;
		}
		double[][] dist = new double[p.customers.size() + 2][p.customers.size() + 2];
		for (int i = 0; i < p.customers.size() + 2; i++) {
			for (int j = 0; j < p.customers.size() + 2; j++) {
				dist[i][j] = calcDistance(p, i, j);
			}
		}
		
		ind = linKernighan(tour, dist, p, breakEarly);
		return ind;
	}
}
