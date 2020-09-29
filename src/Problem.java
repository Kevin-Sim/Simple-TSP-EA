import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Problem {
	Location depot;
	ArrayList<Location> customers;
	
	public Problem(String filename) {
		customers = new ArrayList<>();
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			for(int i = 0; i < 6; i++) {
				reader.readLine();
			}
			
			while((line = reader.readLine())!= null) {
				if(line.split(" ").length == 3) {						
					int id = Integer.parseInt(line.split(" ")[0]);
					double x = Double.parseDouble(line.split(" ")[1]);
					double y = Double.parseDouble(line.split(" ")[2]);
					if(id == 1) {
						depot = new Location(id, x, y);
					}else {
						Location loc = new Location(id, x, y);
						customers.add(loc);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String str = depot.toString() + "\r\n";
		for(Location l : customers) {
			str += l.toString() + "\r\n";
		}
		return str;
	}
		
}
