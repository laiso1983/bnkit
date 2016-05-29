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
import bn.node.GDT;
import bn.prob.EnumDistrib;
import dat.EnumVariable;
import dat.Enumerable;
import dat.IntegerSeq;
import dat.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        int segments = Integer.parseInt(args[1]); //Used to figure out which columns belong to which distribution
        int ncluster = Integer.parseInt(args[2]);
        int ecluster = Integer.parseInt(args[3]);

//        int[][] data = loadData(filename);



//        EnumVariable Cluster = Predef.Number(ncluster, "Cluster");
//        CPT cluster = new CPT(Cluster);
//
//        EnumVariable EpiSig = Predef.Number(ecluster, "EpiSignal");
//        CPT epiSig = new CPT(EpiSig);

//        EnumVariable PeakLoc = Predef.Number(ncluster, "PeakLocation");
//        CPT peakLoc = new CPT(PeakLoc);
//
//        EnumVariable enhancer = Predef.Boolean("Enhancer");
//        CPT enh = new CPT(enhancer, PeakLoc);
//
//        EnumVariable promoter = Predef.Boolean("Promoter");
//        CPT pro = new CPT(promoter, PeakLoc);
//
//        EnumVariable repressive = Predef.Boolean("Repressive");
//        CPT rep = new CPT(repressive, PeakLoc);
//
//        Variable Distance = Predef.Real("Distance");
//        GDT dis = new GDT(Distance, enhancer, promoter, repressive);
//
////        //Can be included later...
////        EnumVariable CellType = Predef.Nominal(new String[]{"CT1", "CT2", "CT3", "CT4"}, "CellType");
//
//        //Require Enumerable to create distribution variable
//        Enumerable tf = new Enumerable(segments);
//        Variable TF = Predef.Distrib(tf, "TFNode");
//        DirDT tfN = new DirDT(TF, PeakLoc);
//
//        Enumerable h3k9ac = new Enumerable(segments);
//        Variable H3K9AC = Predef.Distrib(h3k9ac, "h3k9ac");
//        DirDT h3k9acN = new DirDT(H3K9AC, enhancer,promoter);
//
//        Enumerable h3k36me3 = new Enumerable(segments);
//        Variable H3K36ME3 = Predef.Distrib(h3k36me3, "h3k36me3");
//        DirDT h3k36me3N = new DirDT(H3K36ME3, PeakLoc);
//
//        Enumerable h3k27me3 = new Enumerable(segments);
//        Variable H3K27ME3 = Predef.Distrib(h3k27me3, "h3k27me3");
//        DirDT h3k27me3N = new DirDT(H3K27ME3,repressive);
//
//        Enumerable h3k4me3 = new Enumerable(segments);
//        Variable H3K4ME3 = Predef.Distrib(h3k4me3, "h3k4me3");
//        DirDT h3k4me3N = new DirDT(H3K4ME3, promoter);
//
//        Enumerable h3k4me1 = new Enumerable(segments);
//        Variable H3K4ME1 = Predef.Distrib(h3k4me1, "h3k4me1");
//        DirDT h3k4me1N = new DirDT(H3K4ME1, enhancer);
//
//        Enumerable h3k9me3 = new Enumerable(segments);
//        Variable H3K9ME3 = Predef.Distrib(h3k9me3, "h3k9me3");
//        DirDT h3k9me3N = new DirDT(H3K9ME3,repressive);
//
//        Enumerable h3k27ac = new Enumerable(segments);
//        Variable H3K27AC = Predef.Distrib(h3k27ac, "h3k27ac");
//        DirDT h3k27acN = new DirDT(H3K27AC, enhancer,promoter);
//
//        bn.add(tfN, peakLoc, enh,pro,rep, dis, h3k27acN, h3k27me3N, h3k36me3N, h3k4me1N, h3k4me3N, h3k9acN, h3k9me3N);
//        List<BNode> nodes = bn.getAlphabetical();
//
//        Variable[] vars = new Variable[nodes.size()];
//        for (int i = 0; i < vars.length; i++) {
//            vars[i] = nodes.get(i).getVariable();
//        }
//        BNBuf.save(bn, "160408_valTF_eh_distance_pwm.xml");



//        CPT peakLoc;
//        for (int pl = 2; pl < 10; pl++) {
//            EnumVariable PeakLoc = Predef.Number(pl, "PeakLocation");
//            peakLoc = new CPT(PeakLoc);
//            for (int e = 2; e < 7; e++) {
//                EnumVariable enhancer = Predef.Number(3, "Enhancer");
//                CPT enh = new CPT(enhancer, PeakLoc);
//                for (int r = 2; r < 7; r++) {
//                    EnumVariable repressive = Predef.Number(r, "Repressive");
//                    CPT rep = new CPT(repressive, PeakLoc);
//                    for (int p = 2; p < 7; p++) {
//                        EnumVariable promoter = Predef.Number(2, "Promoter");
//                        CPT pro = new CPT(promoter, PeakLoc);
//
////                        //Can be included later...
////                        EnumVariable CellType = Predef.Nominal(new String[]{"CT1", "CT2", "CT3", "CT4"}, "CellType");
////                        Variable Distance = Predef.Real("Distance");
////                        GDT dis = new GDT(Distance, enhancer, promoter, repressive);
//
//                        //Require Enumerable to create distribution variable
//                        EnumVariable TF = Predef.Boolean("TFNode");
//                        CPT tfN = new CPT(TF, PeakLoc);
//
//                        EnumVariable H3K9AC = Predef.Boolean("H3K9ac");
//                        CPT h3k9acN = new CPT(H3K9AC, enhancer,promoter);
//
//                        EnumVariable H3K36ME3 = Predef.Boolean("H3K36me3");
//                        CPT h3k36me3N = new CPT(H3K36ME3, PeakLoc);
//
//                        EnumVariable H3K27ME3 = Predef.Boolean("H3K27me3");
//                        CPT h3k27me3N = new CPT(H3K27ME3,repressive);
//
//                        EnumVariable H3K4ME3 = Predef.Boolean("H3K4me3");
//                        CPT h3k4me3N = new CPT(H3K4ME3, promoter);
//
//                        EnumVariable H3K4ME1 = Predef.Boolean("H3K4me1");
//                        CPT h3k4me1N = new CPT(H3K4ME1, enhancer);
//
//                        EnumVariable H3K9ME3 = Predef.Boolean("H3K9me3");
//                        CPT h3k9me3N = new CPT(H3K9ME3,repressive);
//
//                        EnumVariable H3K27AC = Predef.Boolean("H3K27ac");
//                        CPT h3k27acN = new CPT(H3K27AC, enhancer,promoter);
//
//                        EnumVariable DNase = Predef.Boolean("DNase");
//                        CPT dnase = new CPT(DNase, enhancer,promoter);
//
//                        BNet bn = new BNet();
//
//                        bn.add(tfN, peakLoc, enh,pro,rep,h3k27acN, dnase, h3k27me3N, h3k36me3N, h3k4me1N, h3k4me3N, h3k9acN, h3k9me3N);
//                        List<BNode> nodes = bn.getAlphabetical();
//
//                        Variable[] vars = new Variable[nodes.size()];
//                        for (int i = 0; i < vars.length; i++) {
//                            vars[i] = nodes.get(i).getVariable();
//                        }
//                        BNBuf.save(bn, "bnkit/data/160509_valTF_eh_bool_testParams_"+pl+"."+e+"."+r+"."+p+".xml");
//                    }
//                }
//            }
//        }


        CPT peakLoc;
        for (int pl = 2; pl < 81; pl++) {
            EnumVariable PeakLoc = Predef.Number(pl, "PeakLocation");
            peakLoc = new CPT(PeakLoc);

//            //Can be included later...
//            EnumVariable CellType = Predef.Nominal(new String[]{"CT1", "CT2", "CT3", "CT4"}, "CellType");
//            Variable Distance = Predef.Real("Distance");
//            GDT dis = new GDT(Distance, enhancer, promoter, repressive);

            //Require Enumerable to create distribution variable
            EnumVariable TF = Predef.Boolean("TFNode");
            CPT tfN = new CPT(TF, PeakLoc);

            EnumVariable H3K9AC = Predef.Boolean("H3K9ac");
            CPT h3k9acN = new CPT(H3K9AC, PeakLoc);

            EnumVariable H3K36ME3 = Predef.Boolean("H3K36me3");
            CPT h3k36me3N = new CPT(H3K36ME3, PeakLoc);

            EnumVariable H3K27ME3 = Predef.Boolean("H3K27me3");
            CPT h3k27me3N = new CPT(H3K27ME3,PeakLoc);

            EnumVariable H3K4ME3 = Predef.Boolean("H3K4me3");
            CPT h3k4me3N = new CPT(H3K4ME3, PeakLoc);

            EnumVariable H3K4ME1 = Predef.Boolean("H3K4me1");
            CPT h3k4me1N = new CPT(H3K4ME1, PeakLoc);

            EnumVariable H3K9ME3 = Predef.Boolean("H3K9me3");
            CPT h3k9me3N = new CPT(H3K9ME3,PeakLoc);

            EnumVariable H3K27AC = Predef.Boolean("H3K27ac");
            CPT h3k27acN = new CPT(H3K27AC, PeakLoc);

            EnumVariable DNase = Predef.Boolean("DNase");
            CPT dnase = new CPT(DNase, PeakLoc);

            BNet bn = new BNet();

            bn.add(tfN, peakLoc, h3k27acN, dnase, h3k27me3N, h3k36me3N, h3k4me1N, h3k4me3N, h3k9acN, h3k9me3N);
            List<BNode> nodes = bn.getAlphabetical();

            Variable[] vars = new Variable[nodes.size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = nodes.get(i).getVariable();
            }
            BNBuf.save(bn, "bnkit/data/160511_valTF_single_bool_testParams_"+pl+".xml");
        }












//        Object[][] data = load(filename, nodes);
//
//        EM em = new EM(bn);
//        em.train(data, nodes);
//        BNBuf.save(bn, filename + ".xml");
//
//        int N = data.length;

//        Object[][] data_for_EM = new Object[N][vars.length]; //number of dirichlet or non-latent nodes
//        int nseg = 0;

//        List[] bins = new ArrayList[ncluster];
//        for (int i = 0; i < ncluster; i++)
//            bins[i] = new ArrayList(); // start with empty bins
//
//        VarElim inf = new VarElim();
//        inf.instantiate(bn);
//        for (int i = 0; i < data.length; i ++) {
//            int j = 0;
//            for (Variable v : vars) {
//                bn.getNode(v).setInstance(data[i][j]);
//                j++;
//            }
//            Query q = inf.makeQuery(Cluster);
//            CGTable r = (CGTable) inf.infer(q);
//            EnumDistrib d = (EnumDistrib)r.query(Cluster);
//            int predicted = d.getMaxIndex();
//            bins[predicted].add(i);
//        }
//
//        for (int i = 0; i < ncluster; i ++) {
//            try {
//                BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_bin_" + i + ".out"));
//                for (int a = 0; a < bins[i].size(); a ++) {
//                    int k = (Integer)bins[i].get(a);
//                    bw.write(k + "\t");
//                    //FIXME: Edit for general use
//                    int m = 0;
//                    for (Variable v : vars) {
//                        IntegerSeq is = (IntegerSeq)data[k][m];
//                        for (int j = 0; j < is.get().length; j ++)
//                            bw.write(is.get()[j] + "\t");
//                        bw.write(k + "\t");
//                        EnumDistrib ed = (EnumDistrib)v.getDomain();
//                        int size = ed.getDomain().size();
//                        EnumDistrib d = new EnumDistrib(new Enumerable(size), IntegerSeq.intArray(is.get()));
//                        for (int j = 0; j < d.getDomain().size(); j ++)
//                            bw.write(d.get(j) + "\t");
//                        m++;
//                    }
//                    bw.newLine();
//                }
//                bw.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
    }


}
