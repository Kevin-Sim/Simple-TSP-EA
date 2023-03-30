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
		Problem p = new Problem("berlin52.tsp");
		int[] tour = new int[53];
		Individual ind = new Individual(p);		
		tour[0] = 1;
		tour[52] = 1;
		for(int i = 1; i < 52; i++) {
			tour[i] = ind.chromosome.get(i-1).idx;
		}
		System.out.println();

	}

	public static double linKernighan(int[] tour, double[][] dist) {
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
				if (improved)
					break;
			}
		}

		return bestDist;
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
			length += dist[tour[i]][tour[i + 1]];
		}
		length += dist[tour[tour.length - 1]][tour[0]];
		return length;
	}
}
