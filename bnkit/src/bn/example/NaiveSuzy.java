package bn.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import bn.BNet;
import bn.BNode;
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
import dat.EnumVariable;
import dat.Enumerable;
import dat.IntegerSeq;
import dat.Variable;

public class NaiveSuzy {

	public static void main(String[] args) {
        int ncluster = Integer.parseInt(args[2]);
        
        long seed = 1;

        // add data file into data folder
        String filename = args[0];
        //FIXME: make this an input?
        int tfbsColumns = Integer.parseInt(args[1]);
//        int kmerColumns = Integer.parseInt(args[2]);
//        int peakColumns = 20;
//        int kmerColumns = 64;
        
        int[][] data = loadData(filename);
        
        BNet bn = new BNet();
        EnumVariable Cluster;
        Enumerable batf;
        Variable Batf;
        Enumerable cebpb;
        Variable Cebpb;
        Enumerable irf;
        Variable Irf;
        Enumerable rela;
        Variable Rela;
        Enumerable stat3;
        Variable Stat3;
        Enumerable stat5;
        Variable Stat5;
        Enumerable usf2;
        Variable Usf2;

        // Define nodes (connecting the variables into an acyclic graph, i.e. the structure)
        CPT cluster;
//        DirDT peak;
//        DirDT kmer;
        
        DirDT batfd;
        DirDT cebpbd;
        DirDT irfd;
        DirDT relad;
        DirDT stat3d;
        DirDT stat5d;
        DirDT usf2d;
        
        
        
//        if (args.length > 5) {
//            bn = BNBuf.load(args[0]);
//            cluster = (CPT)bn.getNode("Cluster");
//            Cluster = cluster.getVariable();
//            peak = (DirDT)bn.getNode("Peaks");
//            Peak = peak.getVariable();
//            kmer = (DirDT)bn.getNode("Kmers");
//            Kmers = kmer.getVariable();
//        } else {
//            Cluster = Predef.Number(ncluster, "Cluster");
//            peaks = new Enumerable(peakColumns);
//            Peak = Predef.Distrib(peaks, "Peaks");
//            kmers = new Enumerable(kmerColumns);
//            Kmers = Predef.Distrib(kmers, "Kmers");
//            cluster = new CPT(Cluster);
//            peak = new DirDT(Peak,    Cluster);
//            kmer = new DirDT(Kmers, Cluster);
//            bn.add(cluster, peak, kmer);                
//        }
        
        if (args.length > 5) {
            bn = BNBuf.load(args[0]);
            cluster = (CPT)bn.getNode("Cluster");
            Cluster = cluster.getVariable();
//            peak = (DirDT)bn.getNode("Peaks");
//            Peak = peak.getVariable();
//            kmer = (DirDT)bn.getNode("Kmers");
//            Kmers = kmer.getVariable();
            batf = new Enumerable(tfbsColumns);
            Batf = Predef.Distrib(batf, "BATF");
            cebpb = new Enumerable(tfbsColumns);
            Cebpb = Predef.Distrib(cebpb, "CEBPB");
            irf = new Enumerable(tfbsColumns);
            Irf = Predef.Distrib(irf, "IRF3");
            rela = new Enumerable(tfbsColumns);
            Rela = Predef.Distrib(rela, "RELA");
            stat3 = new Enumerable(tfbsColumns);
            Stat3 = Predef.Distrib(stat3, "STAT3");
            stat5 = new Enumerable(tfbsColumns);
            Stat5 = Predef.Distrib(stat5, "STAT5");
            usf2 = new Enumerable(tfbsColumns);
            Usf2 = Predef.Distrib(usf2, "USF2");
        } else {
            Cluster = Predef.Number(ncluster, "Cluster");
            batf = new Enumerable(tfbsColumns);
            Batf = Predef.Distrib(batf, "BATF");
            cebpb = new Enumerable(tfbsColumns);
            Cebpb = Predef.Distrib(cebpb, "CEBPB");
            irf = new Enumerable(tfbsColumns);
            Irf = Predef.Distrib(irf, "IRF3");
            rela = new Enumerable(tfbsColumns);
            Rela = Predef.Distrib(rela, "RELA");
            stat3 = new Enumerable(tfbsColumns);
            Stat3 = Predef.Distrib(stat3, "STAT3");
            stat5 = new Enumerable(tfbsColumns);
            Stat5 = Predef.Distrib(stat5, "STAT5");
            usf2 = new Enumerable(tfbsColumns);
            Usf2 = Predef.Distrib(usf2, "USF2");
            cluster = new CPT(Cluster);
            batfd = new DirDT(Batf,    Cluster);
            cebpbd = new DirDT(Cebpb, Cluster);
            irfd = new DirDT(Irf,    Cluster);
            relad = new DirDT(Rela, Cluster);
            stat3d = new DirDT(Stat3,    Cluster);
            stat5d = new DirDT(Stat5, Cluster);
            usf2d = new DirDT(Usf2, Cluster);
            bn.add(cluster, batfd, cebpbd, irfd, relad, stat3d, stat5d, usf2d);                
        }
        
        int N = data.length;
        //FIXME: need a way of identifying nodes that contain data to set 2nd dimension of data
        //add some sort of tag/identifier?
        Variable[] vars = {Batf, Cebpb, Irf, Rela, Stat3, Stat5, Usf2};
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
}
