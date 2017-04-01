/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visPrep;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import api.PartialOrderGraph;

/**
 *
 * @author ariane
 */
public class POAG {
    
    PartialOrderGraph poag; 
    PathGen pathGen;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dotPath = "/home/ariane/Downloads/testPOGraphSmall.dot"; //"/home/ariane/NetBeansProjects/POAG/src/poag/testPOGraphLarge.dot"; //new PartialOrderGraph("/home/ariane/Documents/stemformatics/biojs_alignment/data/defaultMSA.dot");
        POAG pg = new POAG(dotPath);
        HashMap<Integer, List<Integer>> pathDepths = pg.getPathDepths();
        
    }
    
    
    public POAG (String dotPath) {
        poag = new PartialOrderGraph(dotPath);
        pathGen = new PathGen(poag);
       
    }
    
    /**
     * Returns a map of paths and the depth at which these occur
     * @return paths
     */
    public HashMap<Integer, List<Integer>>  getPathDepths() {
        Integer[] nodes = poag.getNodeIDs();
        int numNodes = nodes.length;
        HashMap<Integer, List<Integer>> paths = new HashMap<>();
        int depth = 0;
        
        // This gets the main path which will be centered
        pathGen.resetSearchList();
        pathGen.initAStarSearch(pathGen.startID, pathGen.goalID);
        List<Integer> path = pathGen.getMainPath();
        paths.put(depth, path);
        
        // Now we need to get each of the subsequent paths
        // A depth will be associated for each itteration - this will be
        // visualised as further away from the central line.
        depth ++;
        
        int prevGapStart = 10000000; // Something large (note this needs to be done better)
        int gapEnd = 0;
        int gapStart = 0;
        while (!pathGen.gaps.isEmpty()) {
            // Get the next gap from the FIFO gap queue set up in the POAG
            Integer[] gap = pathGen.gaps.remove();
            gapStart = gap[0];
            gapEnd = gap[1];
            
            // Set up the AStar search environment with the start and end 
            // This also clears the searched nodes in the search map
            pathGen.resetSearchList();
            pathGen.initAStarSearch(gapStart, gapEnd);
            path = pathGen.getMainPath();
            
            // If the path was null we don't want to add it - see code below
            // for alternative method (ugly with nested while loops to step
            // through the gap.
            if (path == null) {
                System.err.println("depth: " + depth + " Path: was none for gap: " + Arrays.toString(gap));
                continue;
            }
            System.err.println("Depth: " + depth + ", Path: " + path);  
            paths.put(depth, path);
            
            // Check if we have reached the end of the first itteration of gaps
            // We can tell this because the gaps will start again from the 
            // End of the path.
            if (gapStart > prevGapStart) {
                depth ++;
            }
            
            prevGapStart = gapStart;
        }
        return paths;
    }
    
   
    
    
    
    
}


//            while (path == null) {
//                gapend ++; //pg.getNextNodeIDs(prev); //
//                if (gapend == pg.goalID) {
//                    System.err.println("goal id: " + pg.goalID + " Path: was none for gap: " + gapend);
//                    //gapend = gap[1];
//                    gapstart = curr;
//                    while (path == null) {
//                        gapstart ++;
//                        if (gapstart == gapend) {
//                            return;
//                        }
//                        pog.initAStarSearch(gapstart, gapend, pg);
//                        path = pog.getMainPath();
//                        System.err.println("depth: " + depth + " Path: was none for gap: " + gapstart);
//                    }
//                }
//                pog.initAStarSearch(curr, gapend, pg);
//                path = pog.getMainPath();
//                System.err.println("depth: " + depth + " Path: was none for gap: " + gapend);
//
//            }
