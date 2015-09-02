/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bn.reconstruction;

import bn.BNet;
import bn.BNode;
import bn.alg.CGTable;
import bn.alg.Query;
import bn.alg.VarElim;
import bn.ctmc.PhyloBNet;
import bn.ctmc.matrix.JTT;
import bn.prob.EnumDistrib;
import bn.prob.GammaDistrib;
import dat.EnumSeq;
import dat.EnumVariable;
import dat.Enumerable;
import dat.PhyloTree;
import dat.Variable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mikael Boden
 * @author Alex Essebier
 */
public class ASR {
    
    private PhyloTree tree;
    private List<EnumSeq.Gappy<Enumerable>> seqs;
    private EnumSeq.Alignment<Enumerable> aln;
    private PhyloBNet[] pbnets;
    private double[] R; //Rates at positions in alignment
    private EnumDistrib[] margin_distribs; //Marginal distributions for nodes
    private boolean use_sampled_rate = false;
    private String asr_root; //Reconstructed sequence 
    private GammaDistrib gd; //Calculated gamma distribution
    
    
    private List<String> indexForNodes;
    private Map<String, String> mapForNodes;
    //Joint reconstruction of tree
    private Object[][] asr_matrix; //storage of all reconstructed sequences
    private double sampled_rate = // sampled rate, copy from a previous 1.0-rate run
	0.15599004226404184;
    
    public ASR(String file_tree, String file_aln) {
        loadData(file_tree, file_aln);
        createNetworks();
        queryNetsMarg();
        queryNetsJoint();
        getSequences();
        calcGammaDistrib();
    }
    
 /**
  * Load the supplied tree and alignment files
  * Create phylogenetic tree, create node index, create node map
  * Store sequences and alignment
  * @param file_tree
  * @param file_aln 
  */
    public void loadData(String file_tree, String file_aln) {
        try {
            tree = PhyloTree.loadNewick(file_tree); //load tree - tree not in Newick?
            PhyloTree.Node[] nodes = tree.toNodesBreadthFirst(); //tree to nodes - recursive
            indexForNodes = new ArrayList<>(); // Newick string for subtree
            mapForNodes = new HashMap<>(); // Shortname --> Newick string for subtree
            for (PhyloTree.Node n : nodes) {
                //n string format internal 'N0_'
                //n string format extant (no children) 'seq_name_id'
                //n.toString() recursive Newick representation node and children
                //creates subtrees?
                indexForNodes.add(replacePunct(n.toString())); 
                mapForNodes.put(n.getLabel().toString(), replacePunct(n.toString()));
            }
            
            seqs = EnumSeq.Gappy.loadClustal(file_aln, Enumerable.aacid);
            aln = new EnumSeq.Alignment<>(seqs);
            
            } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Using the stored alignment, create a PhyloBNet representing each column
     * in alignment. These networks will be queried to reconstruct ancestral
     * sequences. Populate pbnets array with networks.
     */
    public void createNetworks(){
        //Each column/position in alignment/sequence gets a network
        PhyloBNet[] pbnets = new PhyloBNet[aln.getWidth()];
        
        String[] names = aln.getNames(); //seq_name_id - names of sequences
        List<String> labels = getLabels(names);
            
        //Create network for each column in alignment
        //Instantiate nodes
        for (int col = 0; col < aln.getWidth(); col ++) {
            Object[] gaps = aln.getGapColumn(col); // array with true for gap, false for symbol
            Object[] column = aln.getColumn(col);  // array for symbols, null for gaps
            tree.setContentByParsimony(names, gaps);
            PhyloBNet pbn;
            if (use_sampled_rate)
                pbn = PhyloBNet.create(tree, new JTT(), sampled_rate);
            else
                //creates BNet beginning with root then recursively
                //traversing subtrees
                pbn = PhyloBNet.create(tree, new JTT());
            pbnets[col] = pbn;

            // set variables according to alignment
            for (int i = 0; i < labels.size(); i ++) {
                String shortname = labels.get(i);
                String longname = mapForNodes.get(shortname);
                if (longname != null) {
                    BNode bnode = pbn.getBN().getNode(longname);
                    bnode.setInstance(column[i]);
                }
            }
        }
        this.pbnets = pbnets;
    }
    
    /**
     * Query all networks in pbnets array using marginal probability
     * Populate margin_distribs for each column in alignment
     * @param qNode
     * @param pbnets 
     */
    public void queryNetsMarg() {
        this.margin_distribs = new EnumDistrib[aln.getWidth()];
        BNode root = null;
        for (int col = 0; col < aln.getWidth(); col ++) {
            PhyloBNet pbn = pbnets[col];
            BNet bn = pbn.getBN();
            //FIXME
            root = pbn.getRoot(); //Possibly in wrong location??
            //Root can change in purge and collapse steps?
            VarElim ve = new VarElim();
            ve.instantiate(bn);

            int purged_leaves = pbn.purgeGaps(); //Remove leaves with gap (i.e. uninstantiated)
            int collapsed_nodes = pbn.collapseSingles();

            margin_distribs[col] = getMarginalDistrib(ve, root.getVariable());
        }        
    }
    
    /**
     * Query all networks in pbnets array using MPE
     * Populate asr_matrix with reconstructed sequences
     * Populate rate matrix with calculated rate for each position in alignment
     * @param qNode
     * @param pbnets 
     */
    public void queryNetsJoint() {
        // joint reconstruction for tree
        this.asr_matrix = new Object[indexForNodes.size()][aln.getWidth()];
        this.R = new double[aln.getWidth()]; //Rate matrix
        for (int col = 0; col < aln.getWidth(); col ++) {
            PhyloBNet pbn = pbnets[col];
            BNet bn = pbn.getBN();
            VarElim ve = new VarElim();
            ve.instantiate(bn);

            int purged_leaves = pbn.purgeGaps(); //Remove leaves with gap (i.e. uninstantiated)
            int collapsed_nodes = pbn.collapseSingles();
            
            for (Variable.Assignment a0 : getJointAssignment(ve)) {
                EnumVariable asr_var = (EnumVariable)a0.var;
                Object asr_val = a0.val;
                int index = indexForNodes.indexOf(replacePunct(asr_var.getName()));
                if (index >= 0) 
                    //index = current node
                    //col = position in alignment
                    asr_matrix[index][col] = asr_val;
                BNode node = bn.getNode(asr_var);
                node.setInstance(asr_val);
            }
            R[col] = pbn.getRate(); //calculates rate based on evidence provided
            //All nodes instantiated with MPE - rate across entire tree?
        }        
    }
    
    /**
     * Query a specific column in the alignment using marginal probability
     * Update marginal distribution for column
     * @param col 
     */
    public void queryNetMarg(int col) {
        BNode root = null;
        PhyloBNet pbn = pbnets[col];
        BNet bn = pbn.getBN();
        //FIXME
        root = pbn.getRoot(); //Possibly in wrong location??
        //Root can change in purge and collapse steps?
        VarElim ve = new VarElim();
        ve.instantiate(bn);

        int purged_leaves = pbn.purgeGaps(); //Remove leaves with gap (i.e. uninstantiated)
        int collapsed_nodes = pbn.collapseSingles();

        margin_distribs[col] = getMarginalDistrib(ve, root.getVariable());
    }
    
    /**
     * Query a specific column in the alignment using MPE
     * Update ASR for column
     * Update rate for column
     * @param col - column in alignment
     */
    public void queryNetJoint(int col) {
        PhyloBNet pbn = pbnets[col];
        BNet bn = pbn.getBN();
        VarElim ve = new VarElim();
        ve.instantiate(bn);

        int purged_leaves = pbn.purgeGaps(); //Remove leaves with gap (i.e. uninstantiated)
        int collapsed_nodes = pbn.collapseSingles();
            
        for (Variable.Assignment a0 : getJointAssignment(ve)) {
            EnumVariable asr_var = (EnumVariable)a0.var;
            Object asr_val = a0.val;
            int index = indexForNodes.indexOf(replacePunct(asr_var.getName()));
            if (index >= 0) 
                //index = current node
                //col = position in alignment
                asr_matrix[index][col] = asr_val;
            BNode node = bn.getNode(asr_var);
            node.setInstance(asr_val);
        }
        R[col] = pbn.getRate(); //calculates rate based on evidence provided
        //All nodes instantiated with MPE - rate across entire tree?  
    }
    
    private EnumDistrib getMarginalDistrib(VarElim ve, Variable queryNode) {
        Query q_marg = ve.makeQuery(queryNode);
        CGTable r_marg = (CGTable)ve.infer(q_marg);
        EnumDistrib d_marg = (EnumDistrib)r_marg.query(queryNode);
        return d_marg;
    }
    
    private Variable.Assignment[] getJointAssignment(VarElim ve) {
        Query q_joint = ve.makeMPE();
        CGTable r_joint = (CGTable)ve.infer(q_joint);
        Variable.Assignment[] a = r_joint.getMPE();
        return a;
    }
    
    private Variable.Assignment[] getJointAssignment(VarElim ve, Variable queryNode) {
        Query q_joint = ve.makeMPE(queryNode);
        CGTable r_joint = (CGTable)ve.infer(q_joint);
        Variable.Assignment[] a = r_joint.getMPE();
        return a;
    }
    
    /**
     * Extract sequences for internal nodes from the asr matrix
     * Identify sequence of root node and print internal node results
     */
    public void getSequences(){
        //Retrieve and store reconstructions for each latent node
        List<EnumSeq.Gappy<Enumerable>> asrs = new ArrayList<>();
        //asr_matrix stores joint reconstruction - MPE assignment of each node in each network
        for (int row = 0; row < asr_matrix.length; row ++) { 
            Object[] asr_obj = asr_matrix[row]; //retrieve MP sequence for node/row
            EnumSeq.Gappy<Enumerable> myasr = new EnumSeq.Gappy<>(Enumerable.aacid_alt);
            myasr.set(asr_obj);
            myasr.setName(indexForNodes.get(row));
            asrs.add(myasr);
        }
        
        String rootname = replacePunct(tree.getRoot().toString());
        PhyloTree.Node[] nodes = tree.toNodesBreadthFirst(); //tree to nodes - recursive
        //Create a new alignment from the reconstructed sequences
        //Print reconstruction for each internal node
        EnumSeq.Alignment aln_asr = new EnumSeq.Alignment(asrs);
        for (int i = 0; i < aln_asr.getHeight(); i ++) {
            EnumSeq.Gappy<Enumerable> asr_seq = aln_asr.getEnumSeq(i);
            String nodename = asr_seq.getName();
            if (rootname.equals(nodename))
                this.asr_root = asr_seq.toString();
                //System.out.println(asr_seq.getName() + "\t" + asr_seq.toString());
                
            //Not root and not leaf
            if (nodes[i].getChildren().toArray().length > 0){
                System.out.println(">" + nodes[i].getLabel());
                System.out.println(asr_seq.toString());
                }
            }
    }
    
    /**
     * Estimates parameters of gamma distribution then creates a gamma distribution
     */
    public void calcGammaDistrib(){
        //estimates parameters of gamma distribution
        double alpha = GammaDistrib.getAlpha(R);
        double beta = 1 / alpha;
        System.out.println("Gamma alpha = " + alpha + " beta = " + beta);
        //Creates a gamma distribution
        this.gd = new GammaDistrib(alpha, 1/beta);
    }
    
    public String getAsrSeq(){
        return asr_root;
    }
    
    public PhyloTree getTree() {
        return tree;
    }
    
    public List<EnumSeq.Gappy<Enumerable>> getSeqs(){
        return seqs;
    }

    public EnumSeq.Alignment<Enumerable> getAln() {
        return aln;
    }
    
    //FIXME: Can you update the pbnets array without modifying the aln. Where
    //do the two interact?
    /**
     * Get the array of pbnets representing the alignment
     * @return array of pbnets
     */
    public PhyloBNet[] getPbnets() {
        return pbnets;
    }
    /**
     * Unlikely to be used
     * Set the array of pbnets representing the alignment
     * @param pbnets 
     */
    public void setPbnets(PhyloBNet[] pbnets) {
        this.pbnets = pbnets;
    }
    /**
     * Update a particular pbnet in the pbnet array
     * Use: when modifying single column in alignment, update array of networks
     * @param index
     * @param pbnet 
     */
    public void setPbnet(int index, PhyloBNet pbnet) {
        pbnets[index] = pbnet;
    }
    /**
     * Remove a specific pbnet from the array
     * Use: when removing a column from an alignment, update array of networks
     * @param index 
     */
    public void removePbnet(int index) {
        PhyloBNet[] pbnetsNew = new PhyloBNet[pbnets.length - 1];
        List<PhyloBNet> list = new ArrayList<PhyloBNet>(Arrays.asList(pbnets));
        list.remove(index);
        pbnets = list.toArray(pbnetsNew);
    }
        
    public double[] getRates(){
        return R;
    }
    
    public EnumDistrib[] getMarginDistribs(){
        return margin_distribs;
    }
    
    
    /**
     * Checks names of sequences from alignment for amendments and records 
     * appropriate substring label
     * @param names - array of names from alignment file
     * @return labels - list of labels with string modifications
     */
    private static List<String> getLabels(String[] names) {
        //seq_name_id - names of sequences
        List<String> labels = new ArrayList<>();
           
        for (int i = 0; i < names.length; i ++) {
            //check if any names ammended and modify if they are
            int index = names[i].indexOf("/"); // in this aln file, names have been amended
            if (index > 0)
                labels.add(names[i].substring(0, index));
            else
                labels.add(names[i]);
        }
        return labels;
    }
    
    private static String replacePunct(String str) {
        return str.replace('.', '_');
    }    
    
}
