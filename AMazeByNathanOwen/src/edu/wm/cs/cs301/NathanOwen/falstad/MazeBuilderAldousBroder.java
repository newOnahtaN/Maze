package edu.wm.cs.cs301.NathanOwen.falstad;

public class MazeBuilderAldousBroder extends MazeBuilder{
	
	
	public MazeBuilderAldousBroder() {
		super();
		System.out.println("MazeBuilderPrim uses Aldous-Broder's algorithm to generate maze.");
	}
	public MazeBuilderAldousBroder(boolean det) {
		super(det);
		System.out.println("MazeBuilderPrim uses Aldous-Broder's algorithm to generate maze.");
	}
	
	@Override
	protected void generatePathways() {
		
		// pick initial position (x,y) at some random position on the maze
		int x = random.nextIntWithinInterval(0, width-1);
		int y = random.nextIntWithinInterval(0, height - 1);

		cells.setCellAsVisited(x, y);
		int cellsVisited = 1;
		int dx = 0;
		int dy = 0;
		
		int direction = random.nextIntWithinInterval(0, 3);
		
		while (cellsVisited < (width * height)){
			
			if (direction == 0){ //move up
				dx = 0;
				dy = -1;

				
				// enter the random cell, if we have visited before, continue the proces
				// if we haven't entered it before, delete the wall between 
				if (canGoandFirst(x + dx, y + dy)){
					
					//delete the wall between the two, set cell as visited, and increment cells visited
					cells.deleteWall(x, y, dx, dy);
					cells.setCellAsVisited(x + dx, y + dy);
					cellsVisited ++;
					
			}
			}
			else if (direction == 1){ //move right
				dx = 1;
				dy = 0;
				
				// enter the random cell, if we have visited before, continue the proces
				// if we haven't entered it before, delete the wall between 
				if (canGoandFirst(x + dx, y + dy)){
					
					//delete the wall between the two, set cell as visited, and increment cells visited
					cells.deleteWall(x, y, dx, dy);
					cells.setCellAsVisited(x + dx, y + dy);
					cellsVisited ++;
			}
			}
			else if (direction == 2){ //move down
				dx = 0;
				dy = 1;
				
				// enter the random cell, if we have visited before, continue the proces
				// if we haven't entered it before, delete the wall between 
				if (canGoandFirst(x + dx, y + dy)){
					
					//delete the wall between the two, set cell as visited, and increment cells visited
					cells.deleteWall(x, y, dx, dy);
					cells.setCellAsVisited(x + dx, y + dy);
					cellsVisited ++;
			}
			}
			else if (direction == 3){ //move left
				dx = -1;
				dy = 0;
				
				// enter the random cell, if we have visited before, continue the proces
				// if we haven't entered it before, delete the wall between 
				if (canGoandFirst(x + dx, y + dy)){
					
					//delete the wall between the two, set cell as visited, and increment cells visited
					cells.deleteWall(x, y, dx, dy);
					cells.setCellAsVisited(x + dx, y + dy);
					cellsVisited ++;
			}
			}
			
			direction = random.nextIntWithinInterval(0, 3);
			
			//Set current cell to the adjacent cell we moved to.
			if (canGo(x + dx, y + dy)){
				x += dx;
				y += dy;
			}
		
		}
		
	}
	
	
	
	private boolean canGo(int ix, int iy) {
		if ((ix < width) && (ix >= 0) && (iy < height) && (iy >= 0))
			return true;
		else
			return false;
	}
	
	public boolean canGoandFirst(int ix, int iy){
		if ((ix < width) && (ix >= 0) && (iy < height) && (iy >= 0))
			if (isFirstVisit(ix, iy)){
				return true;
			}
			else
				return false;
		else
			return false;
				
	}
	private boolean isFirstVisit(int ix, int iy) {
		return ((cells.getValueOfCell(ix, iy) & Constants.CW_VISITED) != 0);
	}
	


}
