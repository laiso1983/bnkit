package bn.example;

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

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static bn.file.DataBuf.load;

/**
 * Created by aesseb on 17-Mar-16.
 */
public class ValTf {

    public static void main(String[] args) {

        long seed = 1;
        //Arguments
        //datafile, colPeaks, colKmer, clusters,

        // add data file into data folder
        String filename = args[0];
        //FIXME change input of distributions to semi-colon separated list?
        int segments = Integer.parseInt(args[1]); //Used to figure out which columns belong to which distribution
        int kmerColumns = Integer.parseInt(args[2]);

        int ncluster = Integer.parseInt(args[3]);
        int ecluster = 10;

//        int[][] data = loadData(filename);

        BNet bn = new BNet();

        EnumVariable Cluster = Predef.Number(ncluster, "Cluster");
        CPT cluster = new CPT(Cluster);

        EnumVariable EpiSig = Predef.Number(ecluster, "EpiSignal");
        CPT epiSig = new CPT(EpiSig);

//        //Can be included later...
//        EnumVariable CellType = Predef.Nominal(new String[]{"CT1", "CT2", "CT3", "CT4"}, "CellType");

        //Require Enumerable to create distribution variable
        Enumerable tf = new Enumerable(segments);
        Variable TF = Predef.Distrib(tf, "TFNode");
        DirDT tfN = new DirDT(TF, Cluster, EpiSig);

        Enumerable h3k9ac = new Enumerable(segments);
        Variable H3K9AC = Predef.Distrib(h3k9ac, "h3k9ac");
        DirDT h3k9acN = new DirDT(H3K9AC, EpiSig);

        Enumerable h3k36me3 = new Enumerable(segments);
        Variable H3K36ME3 = Predef.Distrib(h3k36me3, "h3k36me3");
        DirDT h3k36me3N = new DirDT(H3K36ME3, EpiSig);

        Enumerable h3k27me3 = new Enumerable(segments);
        Variable H3K27ME3 = Predef.Distrib(h3k27me3, "h3k27me3");
        DirDT h3k27me3N = new DirDT(H3K27ME3, EpiSig);

        Enumerable h3k4me3 = new Enumerable(segments);
        Variable H3K4ME3 = Predef.Distrib(h3k4me3, "h3k4me3");
        DirDT h3k4me3N = new DirDT(H3K4ME3, EpiSig);

        Enumerable h3k4me1 = new Enumerable(segments);
        Variable H3K4ME1 = Predef.Distrib(h3k4me1, "h3k4me1");
        DirDT h3k4me1N = new DirDT(H3K4ME1, EpiSig);

        Enumerable h3k9me3 = new Enumerable(segments);
        Variable H3K9ME3 = Predef.Distrib(h3k9me3, "h3k9me3");
        DirDT h3k9me3N = new DirDT(H3K9ME3, EpiSig);

        Enumerable h3k27ac = new Enumerable(segments);
        Variable H3K27AC = Predef.Distrib(h3k27ac, "h3k27ac");
        DirDT h3k27acN = new DirDT(H3K27AC, EpiSig);

        bn.add(cluster, tfN, epiSig, h3k27acN, h3k27me3N, h3k36me3N, h3k4me1N, h3k4me3N, h3k9acN, h3k9me3N);
        List<BNode> nodes = bn.getAlphabetical();

        Object[][] data = load(filename, nodes);

        Variable[] vars = {TF, H3K27AC, H3K27ME3, H3K36ME3, H3K4ME1, H3K4ME3, H3K9AC, H3K9ME3};

        EM em = new EM(bn);
        em.train(data, vars, seed);
//        cluster.print();
//        peak.print();
        BNBuf.save(bn, filename + ".xml");


        int N = data.length;

        Object[][] data_for_EM = new Object[N][vars.length]; //number of dirichlet or non-latent nodes
        int nseg = 0;

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


}
