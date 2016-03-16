package bn.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import bn.BNet;
import bn.BNode;
import bn.Distrib;
import bn.Predef;
import bn.alg.CGTable;
import bn.alg.EM;
import bn.alg.Query;
import bn.alg.VarElim;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.DirDT;
import bn.prob.DirichletDistrib;
import bn.prob.EnumDistrib;
import dat.Continuous;
import dat.EnumVariable;
import dat.Enumerable;
import dat.IntegerSeq;
import dat.Variable;

public class dirNaiveBayes {
	
	public static void main(String[] args) {
        int ncluster = Integer.parseInt(args[3]);
        
        long seed = 1;

        // add data file into data folder
        String filename = args[0];
        //FIXME: make this an input?
        int peakColumns = Integer.parseInt(args[1]);
        int kmerColumns = Integer.parseInt(args[2]);
//        int peakColumns = 20;
//        int kmerColumns = 64;
        
        int[][] data = loadData(filename);
        
        BNet bn = new BNet();
        EnumVariable Cluster;
        Enumerable peaks;
        Variable Peaks;
        Enumerable kmers;
        Variable Kmers;

        // Define nodes (connecting the variables into an acyclic graph, i.e. the structure)
        CPT cluster;
        DirDT peak;
        DirDT kmer;    
        
        
        if (args.length > 5) {
            bn = BNBuf.load(args[0]);
            cluster = (CPT)bn.getNode("Cluster");
            Cluster = cluster.getVariable();
            peak = (DirDT)bn.getNode("Peaks");
            Peaks = peak.getVariable();
            kmer = (DirDT)bn.getNode("Kmers");
            Kmers = kmer.getVariable();
        } else {
            Cluster = Predef.Number(ncluster, "Cluster");
            peaks = new Enumerable(peakColumns);
            Peaks = Predef.Distrib(peaks, "Peaks");
            kmers = new Enumerable(kmerColumns);
            Kmers = Predef.Distrib(kmers, "Kmers");
            cluster = new CPT(Cluster);
            peak = new DirDT(Peaks,    Cluster);
            kmer = new DirDT(Kmers, Cluster);
            bn.add(cluster, peak, kmer);                
        }
                
        int N = data.length;
        //FIXME: need a way of identifying nodes that contain data to set 2nd dimension of data
        //add some sort of tag/identifier?
        Variable[] vars = {Kmers, Peaks};
        Object[][] data_for_EM = new Object[N][vars.length]; //number of dirichlet or non-latent nodes
        int nseg = 0;
        
        List<BNode> nodes = bn.getAlphabetical();
        for (int i = 0; i < N; i ++) {
            if (i == 0) 
                nseg = data[i].length;
            else if (nseg != data[i].length)
                throw new RuntimeException("Error in data: invalid item at data point " + (i + 1));
            int j = 0;
            int end = 0;
          //FIXME: need a way of identifying nodes that contain data
            for (Variable v : vars) {
            	Object a = v.getDomain();
            	try{
            		EnumDistrib ed = (EnumDistrib)a;
            		int size = ed.getDomain().size();        		
            		int[] d = Arrays.copyOfRange(data[i], end, end+size);
            		data_for_EM[i][j] = new IntegerSeq(d);
            		
            		end += size;
            		j++;
            		
//            		System.out.println();
            	} catch (ClassCastException e) {
            		//FIXME - handle CPTs or GDTs
//            		data_for_EM[i][j] = data[i][j]; 
//            		j++;
//            		System.out.println();
            	}
//            	System.out.println();
            }
        }

        EM em = new EM(bn);
        em.train(data_for_EM, vars, seed);
//        cluster.print();
//        peak.print();
        BNBuf.save(bn, filename + ".xml");
        

        List[] bins = new ArrayList[ncluster];
        for (int i = 0; i < ncluster; i++)
            bins[i] = new ArrayList(); // start with empty bins

        VarElim inf = new VarElim();
        inf.instantiate(bn);
        for (int i = 0; i < data_for_EM.length; i ++) {
        	int j = 0;
        	for (Variable v : vars) {
        		bn.getNode(v).setInstance(data_for_EM[i][j]);
        		j++;
        	}
            Query q = inf.makeQuery(Cluster);
            CGTable r = (CGTable) inf.infer(q);
            EnumDistrib d = (EnumDistrib)r.query(Cluster);
            int predicted = d.getMaxIndex();
            bins[predicted].add(i);
        }

        for (int i = 0; i < ncluster; i ++) {
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_bin_" + i + ".out"));
                for (int a = 0; a < bins[i].size(); a ++) {
                    int k = (Integer)bins[i].get(a);
                    bw.write(k + "\t");
                    //FIXME: Edit for general use
                    int m = 0;
                    for (Variable v : vars) {
                    	IntegerSeq is = (IntegerSeq)data_for_EM[k][m];
                    	for (int j = 0; j < is.get().length; j ++)
                            bw.write(is.get()[j] + "\t");
                    	bw.write(k + "\t");
                    	EnumDistrib ed = (EnumDistrib)v.getDomain();
                		int size = ed.getDomain().size();
                        EnumDistrib d = new EnumDistrib(new Enumerable(size), IntegerSeq.intArray(is.get()));
                        for (int j = 0; j < d.getDomain().size(); j ++)
                            bw.write(d.get(j) + "\t");
                        m++;
                    } 
                    bw.newLine();
                }
                bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    public static int[][] loadData(String filename) {
        BufferedReader br = null;
        int[][] data = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            List<int[]> alldata = new ArrayList<>();
            while (line != null) {
                String[] tokens = line.split("\t");
                int[] values = new int[tokens.length];
                try {
                    for (int i = 0; i < tokens.length; i ++) {
                        values[i] = Integer.valueOf(tokens[i]);
                    }
                    alldata.add(values);
                } catch (NumberFormatException ex2) {
                    System.err.println("Ignored: " + line);
                }
                line = br.readLine();
            }
            data = new int[alldata.size()][];
            for (int k = 0; k < data.length; k ++) {
                data[k] = new int[alldata.get(k).length];
                for (int j = 0; j < data[k].length; j ++) 
                    data[k][j] = alldata.get(k)[j];
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(DirichletDistrib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

//        public static void main0(String[] args) {
//
//        Random rand = new Random(1);
//        
//        // Define variables
//        EnumVariable G = Predef.Nominal(new String[] {"Male", "Female"}, "Gender");
//        Enumerable colours = new Enumerable(new String[] {"Pink", "Green", "Blue"});
//        Enumerable sports = new Enumerable(new String[] {"Netball", "Soccer", "Rugby"});
//        Variable C     = Predef.Distrib(colours, "Colours");
//        Variable S     = Predef.Distrib(sports,  "Sports");
//
//        // Define nodes (connecting the variables into an acyclic graph, i.e. the structure)
//        CPT g = new CPT(G);
//        DirDT c = new DirDT(C,    G);
//        DirDT s = new DirDT(S,    G);
//
//        // Parameterise the nodes using our "expertise"
//        g.put(new EnumDistrib(new Enumerable(new String[] {"Male", "Female"}), 0.49, 0.51));
//        c.put(new DirichletDistrib(colours, new double[] {3.0, 5.0, 7.0}), "Male");
//        c.put(new DirichletDistrib(colours, new double[] {7.0, 2.0, 3.0}), "Female");
//        s.put(new DirichletDistrib(sports,  new double[] {2.0, 5.0, 5.0}), "Male");
//        s.put(new DirichletDistrib(sports,  new double[] {9.0, 4.0, 3.0}), "Female");
//        
//        BNet bn = new BNet();
//        bn.add(g,c,s);
//        c.setInstance(new EnumDistrib(colours, new double[] {0.3, 0.3, 0.4})); // primarily blue, so probably male...
//        VarElim inf = new VarElim();
//        inf.instantiate(bn);
//        Query q = inf.makeQuery(G,S);
//        CGTable r = (CGTable) inf.infer(q);
//        r.display();
//        Distrib d1 = r.query(S);
//        System.out.println("Prob of sports: " + d1);
//        double[] means = new double[sports.size()];
//        int NSAMPLE = 20;
//        for (int i = 0; i < NSAMPLE; i ++) {
//            EnumDistrib d1_sample = (EnumDistrib)d1.sample();
//            for (int j = 0; j < sports.size(); j ++) 
//                means[j] += d1_sample.get(j) / NSAMPLE;
//            System.out.println("\t" + (i+1) + "\t" + d1_sample);
//        }
//        System.out.print("\tMean\t");
//        for (int j = 0; j < sports.size(); j ++)
//            System.out.print(String.format("%5.2f", means[j]));
//        System.out.println();
//        Distrib d2 = r.query(G);
//        System.out.println("Prob of gender: " + d2);
//                
//        // re-training
//        DirichletDistrib colours_male = new DirichletDistrib(colours, new double[] {3.0, 5.0, 7.0});
//        DirichletDistrib colours_female = new DirichletDistrib(colours, new double[] {7.0, 2.0, 3.0});
//        DirichletDistrib sports_male = new DirichletDistrib(sports,  new double[] {2.0, 5.0, 5.0});
//        DirichletDistrib sports_female = new DirichletDistrib(sports,  new double[] {9.0, 4.0, 3.0});
//
//        Object[][] data = new Object[200][3]; // data set
//        for (int i = 0; i < 200; i ++) {
//            if (i % 2 == 0) { // even so male
//                //if (i % 20 == 0) data[i][0] = "Male";
//                EnumDistrib e1 = (EnumDistrib)colours_male.sample();
//                IntegerSeq is = new IntegerSeq(new Continuous());
//                int[] arr = new int[3];
//                for (int j = 0; j < rand.nextInt(100); j ++) 
//                    arr[e1.getDomain().getIndex(e1.sample())] ++;
//                is.set(IntegerSeq.objArray(arr));
//                data[i][1] = is;
//                EnumDistrib e2 = (EnumDistrib)sports_male.sample();
//                is = new IntegerSeq(new Continuous());
//                arr = new int[3];
//                for (int j = 0; j < rand.nextInt(100); j ++) 
//                    arr[e2.getDomain().getIndex(e2.sample())] ++;
//                is.set(IntegerSeq.objArray(arr));
//                data[i][2] = is;
//            } else { // female
//                //if (i % 20 == 1) data[i][0] = "Female";
//                EnumDistrib e1 = (EnumDistrib)colours_female.sample();
//                IntegerSeq is = new IntegerSeq(new Continuous());
//                int[] arr = new int[3];
//                for (int j = 0; j < rand.nextInt(100); j ++) 
//                    arr[e1.getDomain().getIndex(e1.sample())] ++;
//                is.set(IntegerSeq.objArray(arr));
//                data[i][1] = is;
//                EnumDistrib e2 = (EnumDistrib)sports_female.sample();
//                is = new IntegerSeq(new Continuous());
//                arr = new int[3];
//                for (int j = 0; j < rand.nextInt(100); j ++) 
//                    arr[e2.getDomain().getIndex(e2.sample())] ++;
//                is.set(IntegerSeq.objArray(arr));
//                data[i][2] = is;
//            }
//        }
//
//        EnumVariable G2 = Predef.Number(10, "Cluster");
//        Enumerable colours2 = new Enumerable(new String[] {"Pink", "Green", "Blue"});
//        Enumerable sports2 = new Enumerable(new String[] {"Netball", "Soccer", "Rugby"});
//        Variable C2     = Predef.Distrib(colours, "Colours");
//        Variable S2     = Predef.Distrib(sports,  "Sports");
//
//        // Define nodes (connecting the variables into an acyclic graph, i.e. the structure)
//        CPT g2 = new CPT(G2);
//        DirDT c2 = new DirDT(C2,    G2);
//        DirDT s2 = new DirDT(S2,    G2);
////        g2.put(new EnumDistrib(new Enumerable(new String[] {"Male", "Female"}), 0.25, 0.75));
////        c2.put(new DirichletDistrib(colours, 1), "Male");
////        c2.put(new DirichletDistrib(colours, 1), "Female");
////        s2.put(new DirichletDistrib(sports,  1), "Male");
////        s2.put(new DirichletDistrib(sports,  1), "Female");
//
//        BNet bn2 = new BNet();
//        bn2.add(g2,c2,s2);
//
////        for (int index = 0; index < G2.size(); index ++) {
////            c2.put(index, new DirichletDistrib(colours2, ((EnumDistrib)data[index*16][1]).get()));
////            s2.put(index, new DirichletDistrib(colours2, ((EnumDistrib)data[index*16][2]).get()));
////        }
//        
//        EM em = new EM(bn2);
//        System.out.println("Before EM");
//        g2.randomize(1);
//        g2.print();
//        c2.randomize(1);
//        c2.print();
//        s2.randomize(1);
//        s2.print();
//        em.EM_MAX_ROUNDS = 5;
// //       em.EM_PRINT_STATUS = false;
//
//        em.train(data, new Variable[] {G2, C2, S2}, 0);
//
//        System.out.println("After EM (1)");
//        g2.print();
//        c2.print();
//        s2.print();
//
//        c2.setInstance(new EnumDistrib(colours, new double[] {0.1, 0.1, 0.8})); // primarily blue
//        inf = new VarElim();
//        inf.instantiate(bn2);
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//        c2.setInstance(new EnumDistrib(colours, new double[] {0.8, 0.1, 0.1})); // primarily pink
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//
//        em.train(data, new Variable[] {G2, C2, S2}, 0);
//
//        System.out.println("After EM (2)");
//        g2.print();
//        c2.print();
//        s2.print();
//
//        //c2.setInstance(new EnumDistrib(colours, new double[] {0.1, 0.1, 0.8})); // primarily blue
//        c2.setInstance(IntegerSeq.intSeq(new int[] {1,1,8}));
//        inf = new VarElim();
//        inf.instantiate(bn2);
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//        //c2.setInstance(new EnumDistrib(colours, new double[] {0.8, 0.1, 0.1})); // primarily pink
//        c2.setInstance(IntegerSeq.intSeq(new int[] {8,1,1}));
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//
//        em.EM_MAX_ROUNDS = 1000;
//        em.train(data, new Variable[] {G2, C2, S2}, 0);
//
//        System.out.println("After EM (3)");
//        g2.print();
//        c2.print();
//        s2.print();
//
//        //c2.setInstance(new EnumDistrib(colours, new double[] {0.1, 0.1, 0.8})); // primarily blue
//        c2.setInstance(IntegerSeq.intSeq(new int[] {1,1,8}));
//        inf = new VarElim();
//        inf.instantiate(bn2);
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//        //c2.setInstance(new EnumDistrib(colours, new double[] {0.8, 0.1, 0.1})); // primarily pink
//        c2.setInstance(IntegerSeq.intSeq(new int[] {8,1,1}));
//        q = inf.makeQuery(G2,S2);
//        System.out.println(q);
//        r = (CGTable) inf.infer(q);
//        r.display();
//    }

}
