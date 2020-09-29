
public class Location {
	int idx;
	double x;
	double y;
	public Location(int idx, double x, double y) {
		super();
		this.idx = idx;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "" + idx + " " + x + " " + y;
	}

	public Location copy() {
		Location location = new  Location(this.idx, this.x, this.y);
		return location;
	}
}
