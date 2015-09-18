package arieleditingpanel;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculate the marinas positions
 * @author hdrira
 */
public class MarinasPositionsCalculator {
    
    /**
     * Standard distance between marinas in meters 
     */
    public static int DISTANCE_BETWEEN_MARINAS = 125; //Metre
    
    /**
     * Standard duration between each step in seconds 
     */
    public static int STEP_DURATION = 10;
     
    /**
     * Standard number of nodes per group
     */
    public static int NODE_PER_GROUP = 4;
    
    //
    /**
     * Standard number of nodes per line
     */
    public static int NODE_PER_LINE = 4;
            
    // Trajectory points
    private List<Point> trajectoryPoints;
    
    // Speeds between trajectory points
    private List<Float> trajectorySpeeds;
    
    // Marinas  points to be calculated 
    private List<Map<Integer,Node>> marinasPoints;
    
    // Transitional Trajectory Marinas points : aloow tio record first Marina postion for each step
    Map<Integer,Node>  firstPoints;
    
    // Number of nodes per group
    private int nbNodePerGroup;
            
    // Number of nodes per line
    private int nbNodeperLine;
    
    // Duration between each step
    private int stepDuration;
    
    // Distance between marinas
    private int marinasDistance;
    
    /**
     * Calculate the marinas positions 
     * DISTANCE_BETWEEN_MARINAS = 125 
     * STEP_DURATION = 10 
     * NODE_PER_GROUP = 4
     * NODE_PER_LINE = 4
     * @param trajectoryPoints Trajectory points
     * @param trajectorySpeeds  Speeds between trajectory points
     */
    public MarinasPositionsCalculator(List<Point> trajectoryPoints, List<Float> trajectorySpeeds) {
        this.trajectoryPoints = trajectoryPoints;
        this.trajectorySpeeds = trajectorySpeeds;
        this.marinasPoints = new ArrayList<>();
        this.nbNodePerGroup = NODE_PER_GROUP;
        this.nbNodeperLine = NODE_PER_LINE;
        this.stepDuration = STEP_DURATION;
        this.marinasDistance = DISTANCE_BETWEEN_MARINAS;
    }
    
    /**
     * Calculate the marinas positions. 
     * Number of marinas = nbNodePerGroup * nbNodeperLine
     * @param trajectoryPoints Trajectory points
     * @param trajectorySpeeds Speeds between trajectory points
     * @param nbNodePerGroup  Number of nodes per group
     * @param nbNodeperLine Number of nodes per line
     * @param stepDuration Duration between each step
     * @param marinasDistance Distance between marinas
     */
    public MarinasPositionsCalculator(List<Point> trajectoryPoints, List<Float> trajectorySpeeds, int nbNodePerGroup, int nbNodeperLine, int stepDuration, int marinasDistance) {
        this.trajectoryPoints = trajectoryPoints;
        this.trajectorySpeeds = trajectorySpeeds;
        this.marinasPoints = new ArrayList<>();
        this.nbNodePerGroup = nbNodePerGroup;
        this.nbNodeperLine = nbNodeperLine;
        this.stepDuration = stepDuration;
        this.marinasDistance = marinasDistance;
    }
    
    /**
     *
     * @return the number of marinas
     */
    public int getNbMarinas(){
        return nbNodePerGroup * nbNodeperLine;
    }
    
    /**
     * Calculate Marinas initial positions
     */
    public void initMarinasPoints(){
        if(trajectoryPoints.size() >= 1){
            // Marinas initial positions = Transional Points at time 0
            firstPoints = new HashMap<>();
            Point p1 = trajectoryPoints.get(0);
            // formula calculating x coordinate using the number of marina per line
            Double referenceX = p1.getX() - (((nbNodePerGroup-1) * 0.5) * marinasDistance);
            // formula calculating y coordinate using the number of marina lines
            Double referenceY = p1.getY() - (((nbNodeperLine-1) * 0.5) * marinasDistance);
           
            for(int i = 0 ; i < nbNodePerGroup ; i++){
                for(int j = 0; j < nbNodeperLine; j++){
                    int x = referenceX.intValue()+ (i * marinasDistance);
                    int y = referenceY.intValue()+ (j * marinasDistance);
                    Point point = new Point(x,y);
                    Node node = new Node(point, 0);
                    firstPoints.put(i + j * (nbNodeperLine), node);
                }
            }
            
            marinasPoints.add(firstPoints);
        }
    }
    
    public void initFirstPoints(Point refPoint){
        // formula calculating x coordinate using the number of marina per line
        Double referenceX = refPoint.getX() - (((nbNodePerGroup-1) * 0.5) * marinasDistance);
        // formula calculating y coordinate using the number of marina lines
        Double referenceY = refPoint.getY() - (((nbNodeperLine-1) * 0.5) * marinasDistance);

        for(int i = 0 ; i < nbNodePerGroup ; i++){
            for(int j = 0; j < nbNodeperLine; j++){
                int x = referenceX.intValue()+ (i * marinasDistance);
                int y = referenceY.intValue()+ (j * marinasDistance);
                Point newPoint = new Point(x,y);
                Node newNode = new Node(newPoint, 0);
                firstPoints.put(i + j * (nbNodeperLine), newNode);
            }
        }
    }
    
    /**
     * Calculate Marinas positions
     */
    public void calculate(){
        //Calculate Marinas initial positions
        initMarinasPoints();
        
        //Nbr of step
        int stepIndex = 0;
        
        // Calculate the remaining marinas transitional positions
        if(trajectoryPoints.size() >= 2){
            int i = 0;
            while(i+1 < trajectoryPoints.size()){
                Point p1 = trajectoryPoints.get(i);
                Point p2 = trajectoryPoints.get(i+1);
                Float speed = trajectorySpeeds.get(i);

                //Calculate the angle between the two points
                double xDiff = p2.x - p1.x; 
                double yDiff = p2.y - p1.y; 
                double angle = Math.atan2(yDiff, xDiff);
                
                //Calculate the number of transitional point between each segment
                double distance = Math.sqrt(Math.pow((p2.getX() - p1.getX()), 2) + Math.pow((p2.getY() - p1.getY()), 2));
                double step_distance = (stepDuration * speed)/1000;
                Double xToAddByStep = step_distance * Math.cos(angle);
                Double yToAddByStep = step_distance * Math.sin(angle);
                
                //Then calculate their positions
                double nbOfTransitionalPoints = Math.floor(distance / step_distance);
                
                    
                for(int p=0;p<nbOfTransitionalPoints;p++){
                    
                    Double xToAdd = xToAddByStep * p;
                    Double yToAdd = yToAddByStep * p;
                            
                    // Marinas positions of the new step 
                    Map<Integer,Node> newMarinasPositions = new HashMap<>();
                    
                    for(int j=0; j<getNbMarinas(); j++){
                        Node firstStepNode = firstPoints.get(j);
                        Point newPoint = new Point(firstStepNode.getCoodinates().x + xToAdd.intValue() ,firstStepNode.getCoodinates().y + yToAdd.intValue());
                        Node newNode = new Node(newPoint, firstStepNode.getTime() + stepDuration);
                        
                        newMarinasPositions.put(j, newNode);
                    }
                    
                    marinasPoints.add(newMarinasPositions);
                    stepIndex++;
                }
                // init first Marina postion for next step
                initFirstPoints(p2);
                i++;
            }
        }
        
    }

    /**
     * 
     * @return the list of trajectory points
     */
    public List<Point> getTrajectoryPoints() {
        return trajectoryPoints;
    }

    /**
     * 
     * @return the list of marinas positions
     */
    public List<Map<Integer, Node>> getMarinasPoints() {
        calculate();
        return marinasPoints;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Point p1 = new Point(126897, 3082265);
        Point p2 = new Point(127380,3081793);
        //Point p3 = new Point(2000,0);
        
        List<Point> trajectoryPoints = new ArrayList<>();
        trajectoryPoints.add(p1);
        trajectoryPoints.add(p2);
        //trajectoryPoints.add(p3);
        
        List<Float> trajectorySpeeds = new ArrayList<>();
        trajectorySpeeds.add(25.0f);
        trajectorySpeeds.add(25.0f);
        //trajectorySpeeds.add(0.0f);
        
        int nbNodePerGroup = 4;
        int nbNodeperLine = 4;
        int stepDuration = 10;
        int marinasDistance = 125;
        
        MarinasPositionsCalculator marinasPositionsCalculator = new MarinasPositionsCalculator(trajectoryPoints, trajectorySpeeds, nbNodePerGroup, nbNodeperLine, stepDuration, marinasDistance);
        
        List<Map<Integer, Node>> marinasPositions = marinasPositionsCalculator.getMarinasPoints();
        
        //// TESTER LES POSITIONS 
        int i = 0;
        for(Map<Integer, Node> mpi: marinasPositions){
            System.out.println(" *********************** Step number : " +(i+1) + " ***********************");
            
            // number of marinas
            int nbrMarinas = nbNodePerGroup * nbNodeperLine;
            
            for(int j = 0; j < nbrMarinas; j++){
                Node node = mpi.get(j);
                System.out.println("Marina no " + j +" : (x,y) = (" + node.getCoodinates().getX() + "," + node.getCoodinates().getY() +") : Time = " + node.getTime()) ;
            }
            i++;
        }
        
        // TESTE LA VISTESSE
        for(int k = 0 ; k< marinasPositionsCalculator.getNbMarinas(); k++){
            for(int l =0; l<marinasPositions.size()-1;l++){
                Map<Integer, Node> mpi1 = marinasPositions.get(l);
                Map<Integer, Node> mpi2 = marinasPositions.get(l+1);
                
                Node node1 = mpi1.get(k);
                Node node2 = mpi2.get(k);
                
                double distance = Math.sqrt(Math.pow((node2.getCoodinates().getX() - node1.getCoodinates().getX()), 2) + Math.pow((node2.getCoodinates().getY() - node1.getCoodinates().getY()), 2));
                
                double vitesseN1ToN2 = distance / ((double)stepDuration);
                
                System.out.println("Marina "+ k +" Step "+ l + " To Step " + (l+1)+ " - distance: " + distance +" - Vitesse : " + vitesseN1ToN2 + " : P1 = " +node1.getCoodinates()+ " : P2 = " +node2.getCoodinates());
            }
        }
        
        // TESTE CREATION DE FICHER SEPARER POUR CHAQUE MARINAS
        System.out.println("*********************************************************************************************");
        System.out.println("*********************************************************************************************");
        for(int k = 0 ; k< marinasPositionsCalculator.getNbMarinas(); k++){
            for(int l =0; l<marinasPositions.size();l++){
                Map<Integer, Node> mpi = marinasPositions.get(l);
                Node node = mpi.get(k);
                System.out.println("Marina "+ k +" Step "+ l  + " : Coodinates = " +node.getCoodinates());
            }
        }
        
        
    }
}
