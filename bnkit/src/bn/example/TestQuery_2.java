package bn.example;

import bn.BNet;
import bn.BNode;
import bn.Distrib;
import bn.alg.CGTable;
import bn.alg.Query;
import bn.alg.VarElim;
import bn.file.BNBuf;
import bn.file.DataBuf;
import bn.prob.EnumDistrib;
import dat.EnumVariable;
import dat.Variable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by aesseb on 24-Aug-16.
 */
public class TestQuery_2 {

    /**
     * @param args
     */
    public static PrintWriter writer = null;

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: LoadNTrain <bn-file> <data-file> <query> <ignore> <setting> <output-file>");
            System.exit(1);
        }
        String bn_file = args[0];
        String data_file = args[1];
        String query = args[2];
        String ignore = args[3];
        String setting = args[4];
        String output = args[5];

        BNet bn = BNBuf.load(bn_file);
        List<BNode> nodes = bn.getOrdered();
        Object[][] values = DataBuf.load(data_file, nodes);

        String[] queryS = query.split(";");
        Variable[] qVars = new Variable[queryS.length];
        for (int b = 0; b < queryS.length; b++) {
            qVars[b] = (bn.getNode(queryS[b]).getVariable());
        }

        Variable[] iVars = null;
        if (ignore.contains("None")) {
            System.out.println("Nothing to ignore");
        } else {
            String[] ignS = ignore.split(";");
            iVars = new Variable[ignS.length];
            for (int b = 0; b < ignS.length; b++) {
                try {
                    iVars[b] = (bn.getNode(ignS[b]).getVariable());
                } catch (NullPointerException npe) {
                    continue;
                }
            }
        }

        Variable[] vars = new Variable[nodes.size()];

        for (int k = 0; k < nodes.size(); k++) {
            vars[k] = nodes.get(k).getVariable();
        }

        try {
            writer = new PrintWriter(output, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            System.out.println("UnsupportedEncodingException");
        } catch (FileNotFoundException fnf) {
            System.out.println("FileNotFound" + output);
        }


        if (setting.equals("Infer")) {
            infer(values, vars, bn, qVars, iVars);
        } else if (setting.equals("InferDistrib")) {
            inferDistrib(values, vars, bn, qVars, iVars);
        } else if (setting.equals("MPE")) {
            MPE(values, vars, bn , qVars, iVars);
        } else if (setting.equals("Likelihood")) {
            likeli(values, vars, bn, qVars, iVars);
        } else {
            System.out.println("Invalid setting: must be Infer, MPE or Likelihood");
        }

        writer.close();
    }

    public static void MPE(Object[][] values, Variable[] vars, BNet bn, Variable[] qVars, Variable[] iVars) {


        boolean print = true;

        for (int i = 0; i < values.length; i++) {
            // set variables and keys according to observations
            Map<BNode, Object> store = new HashMap<>();
            for (int j = 0; j < vars.length; j++) {
                BNode instantiate_me = bn.getNode(vars[j]);
                if (instantiate_me == null) {
                    System.out.println("Instantiate_me == null");
                }
                //Ignore query variables
                if (Arrays.asList(qVars).contains(instantiate_me.getVariable())) {
                    store.put(instantiate_me, values[i][j]);
                    instantiate_me.resetInstance();
                    continue;
                }
                //Ignore variables set to ignore
                if (Arrays.asList(iVars).contains(instantiate_me.getVariable())) {
                    instantiate_me.resetInstance();
                    continue;
                }
                if (values[i][j] != null) { // check so that the observation is not null
                    // the node is instantiated to the value in the data set
                    instantiate_me.setInstance(values[i][j]);
                } else { // the observation is null
                    // the node is reset, i.e. un-instantiated
                    instantiate_me.resetInstance();
                }
            }

            Map<Variable, Object> results = new HashMap<>(qVars.length);
//            System.out.println("Variable elimination--------------");
            VarElim ve = new VarElim();
            ve.instantiate(bn);
            Query q = ve.makeQuery(qVars);
            CGTable cg = (CGTable)ve.infer(q);

            Query mpe = ve.makeMPE(qVars);
            CGTable mpeOut = (CGTable)ve.infer(mpe);

            Variable.Assignment[] out = mpeOut.getMPE();
//    		System.out.println(Arrays.toString(values[i]));
//    		System.out.println("MPE");
            for (Variable.Assignment a : out) {
                int b = 0;
                int max = 1000;
                double sumPredic = 0.0;
                try {
                    EnumVariable v = (EnumVariable)a.var;
                    results.put(a.var, a.val);
//    				System.out.println(a.val);
                } catch (ClassCastException e) {
                    while (b < max) {
                        Distrib d = (Distrib)a.val;
                        double sum = 0;
                        int nElem = 500; //number of elements
                        double minE = 0.0; //smallest value
                        double maxE = 5.0; //largest value
                        double[] hist = new double[nElem];
                        double binSize = (maxE - minE)/nElem;
                        int x = 0;
                        double prediction;
                        while (x < 50000) { //sample from the distribution
                            double s = (Double) d.sample();
                            int bin = (int) ((s - minE) / binSize);
                            //Keep sampling from the same distribution until you get a reasonable result
                            if (bin < 0 ) { continue;}
                            else if (bin >= nElem) {continue;}
                            else {
                                hist[bin] += 1;
                                x++;
                            }
                        }
                        //**************************************************
                        //find the index for the bin with the most counts
                        int maxindex = 0;
                        for (int z = 0; z < hist.length; z++) {
                            double newnumber = hist[z];
                            if (newnumber > hist[maxindex])
                                maxindex = z;
                            //**************************************************
                        }
                        prediction = (double)(maxindex * binSize) + minE; //The bin with the highest counts
                        sumPredic += prediction;
                        b++;
                    }
                    double avg = sumPredic / b;
                    results.put(a.var, avg);
//    				System.out.println(prediction);
                }
            }
            if (print) {
                System.out.println();
                System.out.println(results.keySet());
                List<Object> newList = new ArrayList<>();
                newList.addAll(store.values());
                newList.addAll(results.values());
                System.out.println(newList);

                print = false;
            } else {
                List<Object> newList = new ArrayList<>();
                newList.addAll(store.values());
                newList.addAll(results.values());
                System.out.println(newList);
            }
//    		return results;
        }
    }

    public static void infer(Object[][] values, Variable[] vars, BNet bn, Variable[] qVars, Variable[] iVars) {


        boolean print = true;
        writer.println("True\tPrediction\tProbability");

        for (int i = 0; i < values.length; i++) {
            // set variables and keys according to observations
            Map<BNode, Object> store = new HashMap<>();
            for (int j = 0; j < vars.length; j++) {
                BNode instantiate_me = bn.getNode(vars[j]);
                if (instantiate_me == null) {
                    System.out.println("Instantiate_me == null");
                }
                //Ignore query variables
                if (Arrays.asList(qVars).contains(instantiate_me.getVariable())) {
                    store.put(instantiate_me, values[i][j]);
                    instantiate_me.resetInstance();
                    continue;
                }
                //Ignore variables set to ignore
                if (!(iVars == null)) {
                    if (Arrays.asList(iVars).contains(instantiate_me.getVariable())) {
                        instantiate_me.resetInstance();
                        continue;
                    }
                }
                if (values[i][j] != null) { // check so that the observation is not null
                    // the node is instantiated to the value in the data set
                    instantiate_me.setInstance(values[i][j]);
                } else { // the observation is null
                    // the node is reset, i.e. un-instantiated
                    instantiate_me.resetInstance();
                }
            }

            Map<Variable, List<Object>> results = new HashMap<>(qVars.length);
//            System.out.println("Variable elimination--------------");
            VarElim ve = new VarElim();
            ve.instantiate(bn);
            Query q = ve.makeQuery(qVars);
            CGTable cg = (CGTable)ve.infer(q);

            boolean hPrint = true;

            for (Variable query : qVars) {
                double a = 0.0;
                int b = 0;
                int max = 100;
                double sumPredic = 0.0;
                try {
                    EnumDistrib d = (EnumDistrib)cg.query(query);
                    Map<Object, Object> counts = new HashMap<>();
                    while (a < max) {
                        Object s = null;
                        try {
                            s = d.sample();
                        } catch (RuntimeException re) {
                            s = "None";
                        }

                        if (counts.containsKey(s)) {
                            Integer count = (Integer)counts.get(s) + 1;
                            counts.put(s, count);
                        } else {
                            counts.put(s, 1);
                        }
                        a+=0.5;
                    }
                    Integer mCount = 0;
                    Object mKey = null;
                    for (Map.Entry<Object, Object> c : counts.entrySet()) {
                        Object key = c.getKey();
                        Integer val = (Integer)c.getValue();
                        if (val > mCount) {
                            mCount = val;
                            mKey = key;
                        }
                    }
                    double prob1 = d.get("4");
                    double prob2 = d.get("3");
                    double prob3 = d.get("2");
                    double prob = prob1+prob2+prob3;
                    List<Object> newList = new ArrayList<>();
                    newList.add(mKey);
                    newList.add(prob);
                    results.put(query, newList);
                } catch (ClassCastException e) {

                    while (b < max) {
                        Distrib d = cg.query(query);
                        double sum = 0;
                        int nElem = 500; //number of elements
                        double minE = 0.0; //smallest value
                        double maxE = 6.0; //largest value
                        double[] hist = new double[nElem];
                        double binSize = (maxE - minE)/nElem;
                        int x = 0;
                        double prediction;
                        while (x < 10000) { //sample from the distribution
                            double s = (Double) d.sample();
                            int bin = (int) ((s - minE) / binSize);
                            //Keep sampling from the same distribution until you get a reasonable result
                            if (bin < 0 ) { continue;}
                            else if (bin >= nElem) {continue;}
                            else {
                                hist[bin] += 1;
                                x++;
                            }
                        }
                        //**************************************************
                        //find the index for the bin with the most counts
                        int maxindex = 0;
                        for (int z = 0; z < hist.length; z++) {
                            double newnumber = hist[z];
                            if (newnumber > hist[maxindex])
                                maxindex = z;
                            //**************************************************
                        }
                        prediction = (double)(maxindex * binSize) + minE; //The bin with the highest counts
                        sumPredic += prediction;
                        if (hPrint) {
//							System.out.println(Arrays.toString(hist));
                            hPrint = false;
                        }
                        b++;
                    }
                    double avg = sumPredic / b;
                    List<Object> out = new ArrayList<>();
                    out.add(avg);
                    results.put(query, out);

//    				System.out.println(prediction);
                }

            }
            if (print) {
                System.out.println();
                System.out.println(results.keySet());
                List<Object> newList = new ArrayList<>();
                String builder = "";
                for (Object x : store.values()) {
                    newList.add(x);
                    if (x.equals("0")) {
                        builder = builder+"0"+"\t";
                    } else if (x.equals("1")) {
                        builder = builder+"0"+"\t";
                    } else if (x.equals("2")) {
                        builder = builder+"1"+"\t";
                    } else if (x.equals("3")) {
                        builder = builder+"1"+"\t";
                    } else if (x.equals("4")) {
                        builder = builder+"1"+"\t";
                    }
                }
                for (List<Object> x : results.values()) {
                    for (Object y : x) {
                        newList.add(x);
                        if (y.equals("0")) {
                            builder = builder+"0"+"\t";
                        } else if (y.equals("1")) {
                            builder = builder+"0"+"\t";
                        } else if (y.equals("2")) {
                            builder = builder+"1"+"\t";
                        } else if (y.equals("3")) {
                            builder = builder+"1"+"\t";
                        } else if (y.equals("4")) {
                            builder = builder+"1"+"\t";
                        } else {
                            builder = builder+y+"\t";
                        }
                    }
                }
//                System.out.println(newList);
//                System.out.println(builder);
                String line = builder.trim();
                writer.println(line);
                print = false;
            } else {
                List<Object> newList = new ArrayList<>();
                String builder = "";
                for (Object x : store.values()) {
                    newList.add(x);
                    if (x.equals("0")) {
                        builder = builder+"0"+"\t";
                    } else if (x.equals("1")) {
                        builder = builder+"0"+"\t";
                    } else if (x.equals("2")) {
                        builder = builder+"1"+"\t";
                    } else if (x.equals("3")) {
                        builder = builder+"1"+"\t";
                    } else if (x.equals("4")) {
                        builder = builder+"1"+"\t";
                    }
                }
                for (List<Object> x : results.values()) {
                    for (Object y : x) {
                        newList.add(x);
                        if (y.equals("0")) {
                            builder = builder+"0"+"\t";
                        } else if (y.equals("1")) {
                            builder = builder+"0"+"\t";
                        } else if (y.equals("2")) {
                            builder = builder+"1"+"\t";
                        } else if (y.equals("3")) {
                            builder = builder+"1"+"\t";
                        } else if (y.equals("4")) {
                            builder = builder+"1"+"\t";
                        } else {
                            builder = builder+y+"\t";
                        }
                    }
                }
//                System.out.println(newList);
//                System.out.println(builder);
                String line = builder.trim();
                writer.println(line);
            }
//    		return results;
        }
    }

    public static void inferDistrib(Object[][] values, Variable[] vars, BNet bn, Variable[] qVars, Variable[] iVars) {

        writer.println("State\tTrue\tDistribution");

        for (int i = 0; i < values.length; i++) {
            // set variables and keys according to observations
            Map<String, Object> store = new HashMap<>();
            for (int j = 0; j < vars.length; j++) {
                BNode instantiate_me = bn.getNode(vars[j]);
                if (instantiate_me == null) {
                    System.out.println("Instantiate_me == null");
                }
                //Ignore query variables
                if (Arrays.asList(qVars).contains(instantiate_me.getVariable())) {
                    store.put(instantiate_me.getVariable().getName(), values[i][j]);
                    instantiate_me.resetInstance();
                    continue;
                }
                //Ignore variables set to ignore
                if (!(iVars == null)) {
                    if (Arrays.asList(iVars).contains(instantiate_me.getVariable())) {
                        store.put(instantiate_me.getVariable().getName(), values[i][j]);
                        instantiate_me.resetInstance();
                        continue;
                    }
                }
                if (values[i][j] != null) { // check so that the observation is not null
                    // the node is instantiated to the value in the data set
                    store.put(instantiate_me.getVariable().getName(), values[i][j]);
                    instantiate_me.setInstance(values[i][j]);
                } else { // the observation is null
                    // the node is reset, i.e. un-instantiated
                    store.put(instantiate_me.getVariable().getName(), values[i][j]);
                    instantiate_me.resetInstance();
                }
            }

            Map<Variable, List<Object>> results = new HashMap<>(qVars.length);
//            System.out.println("Variable elimination--------------");
            VarElim ve = new VarElim();
            ve.instantiate(bn);
            Query q = ve.makeQuery(qVars);
            CGTable cg = (CGTable)ve.infer(q);

            for (Variable query : qVars) {
                double a = 0.0;
                int max = 250;

                EnumDistrib d = (EnumDistrib)cg.query(query);
                Map<Object, Object> counts = new HashMap<>();
                while (a < max) {
                    Object s;
                    try {
                        s = d.sample();
                    } catch (RuntimeException re) {
                        s = "None";
                    }

                    if (counts.containsKey(s)) {
                        Integer count = (Integer)counts.get(s) + 1;
                        counts.put(s, count);
                    } else {
                        counts.put(s, 1);
                    }
                    a+=0.5;
                }

                String builder = "";

                builder = builder + store.get("ChipCount") + "\t";
                if (store.get("ChipCount") == "0" || store.get("ChipCount") == "1")
                    builder = builder + "0" + "\t";
                else
                    builder = builder + "1" + "\t";
                Object[] states = {"0", "1", "2", "3", "4"};
                for (int o = 0; o < states.length; o++) {
                    Object state = states[o];
                    Object res = counts.get(state);
                    int count;
                    if (res == null)
                        count = 1;
                    else
                        count = (int)res;
                        count ++;
                    if (o < states.length-1)
                        builder = builder + count + "|";
                    else
                        builder = builder + count;
                }
                String line = builder.trim();
                writer.println(line);
            }
        }
    }

    public static void likeli(Object[][] values, Variable[] vars, BNet bn, Variable[] qVars, Variable[] iVars) {


        boolean print = true;

        for (int i = 0; i < values.length; i++) {
            // set variables and keys according to observations
            Map<BNode, Object> store = new HashMap<>();
            for (int j = 0; j < vars.length; j++) {
                BNode instantiate_me = bn.getNode(vars[j]);
                if (instantiate_me == null) {
                    System.out.println("Instantiate_me == null");
                }
                //Ignore query variables
                if (Arrays.asList(qVars).contains(instantiate_me.getVariable())) {
                    store.put(instantiate_me, values[i][j]);
                    instantiate_me.resetInstance();
                    continue;
                }
                //Ignore variables set to ignore
                if (Arrays.asList(iVars).contains(instantiate_me.getVariable())) {
                    instantiate_me.resetInstance();
                    continue;
                }
                if (values[i][j] != null) { // check so that the observation is not null
                    // the node is instantiated to the value in the data set
                    instantiate_me.setInstance(values[i][j]);
                } else { // the observation is null
                    // the node is reset, i.e. un-instantiated
                    instantiate_me.resetInstance();
                }
            }

            Map<Variable, Object> results = new HashMap<>(qVars.length);
//            System.out.println("Variable elimination--------------");
            VarElim ve = new VarElim();
            ve.instantiate(bn);
            Query q = ve.makeQuery(qVars);
            CGTable cg = (CGTable)ve.infer(q);

            boolean hPrint = true;

            for (Variable query : qVars) {
                double a = 0.0;
                int b = 0;
                int max = 100;
                double sumPredic = 0.0;
                try {
                    EnumDistrib d = (EnumDistrib)cg.query(query);
                    Map<Object, Object> counts = new HashMap<>();
                    while (a < max) {
                        Object s = d.sample();
                        if (counts.containsKey(s)) {
                            Integer count = (Integer)counts.get(s) + 1;
                            counts.put(s, count);
                        } else {
                            counts.put(s, 1);
                        }
                        a+=0.5;
                    }
                    Integer mCount = 0;
                    Object mKey = null;
                    for (Map.Entry<Object, Object> c : counts.entrySet()) {
                        Object key = c.getKey();
                        Integer val = (Integer)c.getValue();
                        if (val > mCount) {
                            mCount = val;
                            mKey = key;
                        }
                    }
                    results.put(query, mKey);
                } catch (ClassCastException e) {

                    while (b < max) {
                        Distrib d = cg.query(query);
                        double sum = 0;
                        int nElem = 500; //number of elements
                        double minE = 0.0; //smallest value
                        double maxE = 6.0; //largest value
                        double[] hist = new double[nElem];
                        double binSize = (maxE - minE)/nElem;
                        int x = 0;
                        double prediction;
                        while (x < 10000) { //sample from the distribution
                            double s = (Double) d.sample();
                            int bin = (int) ((s - minE) / binSize);
                            //Keep sampling from the same distribution until you get a reasonable result
                            if (bin < 0 ) { continue;}
                            else if (bin >= nElem) {continue;}
                            else {
                                hist[bin] += 1;
                                x++;
                            }
                        }
                        //**************************************************
                        //find the index for the bin with the most counts
                        int maxindex = 0;
                        for (int z = 0; z < hist.length; z++) {
                            double newnumber = hist[z];
                            if (newnumber > hist[maxindex])
                                maxindex = z;
                            //**************************************************
                        }
                        prediction = (double)(maxindex * binSize) + minE; //The bin with the highest counts
                        sumPredic += prediction;
                        if (hPrint) {
//							System.out.println(Arrays.toString(hist));
                            hPrint = false;
                        }
                        b++;
                    }
                    double avg = sumPredic / b;
                    results.put(query, avg);

//    				System.out.println(prediction);
                }

            }

            for (Map.Entry<Variable, Object> e : results.entrySet()) {
                BNode instantiate_me = bn.getNode(e.getKey());
                instantiate_me.setInstance(e.getValue());
            }

            VarElim veL = new VarElim();
            veL.instantiate(bn);
            double likelihood = veL.logLikelihood();
//    		System.out.println(likelihood);

            if (print) {
                System.out.println(results.keySet());
                System.out.println(results.values());
                System.out.println(likelihood);
                print = false;
            } else {
                System.out.println(results.values());
                System.out.println(likelihood);
            }
//    		return results;
        }
    }
}
//class ValueComparator implements Comparator<String> {
//
//    Map<String, Double> base;
//    public ValueComparator(Map<String, Double> base) {
//        this.base = base;
//    }
//
//    // Note: this comparator imposes orderings that are inconsistent with equals.
//    public int compare(String a, String b) {
//        if (base.get(a) >= base.get(b)) {
//            return 1;
//        } else {
//            return -1;
//        } // returning 0 would merge keys
//    }
//}




