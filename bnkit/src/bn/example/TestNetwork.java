package bn.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bn.prob.MixtureDistrib;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import bn.BNet;
import bn.BNode;
import bn.Distrib;
import bn.alg.ApproxInference;
import bn.alg.CGTable;
import bn.alg.CGVarElim;
import dat.Variable;
import bn.prob.EnumDistrib;
import bn.alg.Query;
import bn.alg.QueryResult;

public class TestNetwork {
	private BNet bn;
	private BNode testNode;
	private Object[][] data;
	private Variable[] vars;

	public TestNetwork(BNet bn, Object[][] data, String nodeName) {
		this.bn = bn;
		this.data = data;
		List<BNode> nodes = bn.getAlphabetical();
		this.vars = new Variable[nodes.size()];
		//In order to get an ordered list of variable names rather than node names
        for (int i = 0; i < vars.length; i++) {
            vars[i] = nodes.get(i).getVariable();
            if (nodes.get(i).getVariable().getName().equals(nodeName)) {
            	this.testNode = nodes.get(i);
            }
        }
//		load(data, vars);
		
//		BNBuf.save(bn, bn_file + "2.new");
	}
	
	public double load(){
		
		double matches = 0.0;
		double ans = 0.0;
		for (int i = 0; i < data.length; i++) { //For row of data
			// set variables according to observations
			Object testVal = null;
			for (int j = 0; j < vars.length; j++) { //For each node/variable
				BNode instantiate_me = bn.getNode(vars[j]);
				if (instantiate_me.equals(testNode)) {
					testVal = data[i][j];
					instantiate_me.resetInstance();
				} else if (data[i][j] != null) { // check so that the observation is not null
					// the node is instantiated to the value in the data set
					instantiate_me.setInstance(data[i][j]);
				} else { // the observation is null
					// the node is reset, i.e. un-instantiated 
					instantiate_me.resetInstance();
				}
				Object ins = instantiate_me.getInstance();
//				System.out.println();
			}

//			ApproxInference ai = new ApproxInference();
//			ai.instantiate(bn);
//			ai.setIterations(1000);
//			Query q = ai.makeQuery(testNode.getVariable());
//			CGTable answer = ai.infer(q);
			CGVarElim ve = new CGVarElim();
			ve.instantiate(bn);
			Query q = ve.makeQuery(testNode.getVariable());
			CGTable answer = (CGTable) ve.infer(q);
			
			Distrib d = answer.query(testNode.getVariable());
//			double out = d.get(true);
			//**********MODIFIED FOR DREAM CHALLENGE*********
			double out = d.get("B");
			return out;
			 
//			if (testNode.getVariable().getPredef().equals("Boolean")) {
				
//				JPT jpt = answer.getJPT();	
//				return jpt;
//				jpt.display();
				
//				double[] vals = new double[jpt.table.getValues().size()];
//				String[] labels = new String[jpt.table.getValues().size()];
//				for (Entry<Integer, Double> ob : jpt.table.getMapEntries()) {
//					double val = ob.getValue();
//					Integer index = ob.getKey();
//					Object[] key = jpt.table.getKey(index);
//					vals[index] = val;
//					String s = null;
//					for (Object k : key) {
//						if (s == null) {
//							s = k.toString();
//						} else { 
//							s = s + k.toString();
//						}
//					}
//					labels[index] = s;
//				}
//				
//				return vals;
				
				
				
//				List<Double> v = new ArrayList<>();
//				v.addAll(jpt.table.getValues());
//				double[] vals = new double[v.size()];
//				for (int j = 0; j < v.size(); j++) {
//					vals[j] = v.get(j);
//				}
//				Double max = 0.0;
//				Object[] res = null;
//				for (Entry<Integer, Double> entry : jpt.table.getMapEntries()) {
//					Integer key = entry.getKey();
//					Double val = entry.getValue();
//					if (val > max) {
//						max = val;
//						res = jpt.table.getKey(key);
//					}
//				}
//								
//				if (res[0].equals(testVal)) {
//					matches++;
////					System.out.println(res[0]);
////					System.out.println(testVal);
////					jpt.display();
//				}
//				
//				if (i%1000 == 0) {
//					System.out.println(i);
////					System.out.println(res[0]);
////					System.out.println(testVal);
////					jpt.display();
//				}
				
//			} else if (testNode.getVariable().getPredef().equals("Real")) {
//				Map<Variable, Distrib> conTable = answer.getNonEnumDistrib();
//				Distrib d = conTable.get(testNode.getVariable());
//				System.out.println("Not yet available - possible?");
//				return ans;				
//
//			} else {
//				return ans;
//			}

		}
		return ans;
		
//		Double correct = matches/(double)values.length;
//		System.out.println(correct);
	}
	
	public List<Double> loadDiscrete(){

		List<Double> samples = new ArrayList<Double>(data.length);
		for (int i = 0; i < data.length; i++) { //For row of data
			// set variables according to observations
			Object testVal = null;
			for (int j = 0; j < vars.length; j++) { //For each node/variable
				BNode instantiate_me = bn.getNode(vars[j]);
				if (instantiate_me.equals(testNode)) {
					testVal = data[i][j];
					instantiate_me.resetInstance();
				} else if (data[i][j] != null) { // check so that the observation is not null
					// the node is instantiated to the value in the data set
					instantiate_me.setInstance(data[i][j]);
				} else { // the observation is null
					// the node is reset, i.e. un-instantiated 
					instantiate_me.resetInstance();
				}
			}

			CGVarElim ve = new CGVarElim();
			ve.instantiate(bn);
			Query q = ve.makeQuery(testNode.getVariable());
			CGTable answer = (CGTable) ve.infer(q);

			EnumDistrib d = (EnumDistrib) answer.query(testNode.getVariable());
			if (testNode.getVariable().getPredef().equals("Boolean")) {
				Object[] params = {true, false};
				double maxProb = 0.0;
				Object maxVal = null;
				for (Object o : params) {
					double val = d.get(o);
					if (val > maxProb) {
						maxProb = val;
						maxVal = o;
					}
				}
				if (maxVal.equals(testVal)) {
					samples.add(1.0);
				} else {
					samples.add(0.0);
				}
			} else {
				String[] params = testNode.getVariable().getParams().split(";");
				double maxProb = 0.0;
				String maxVal = null;
				for (String s : params) {
					double val = d.get(s);
					if (val > maxProb) {
						maxProb = val;
						maxVal = s;
					}
				}
				if (maxVal.equals(testVal)) {
					samples.add(1.0);
				} else {
					samples.add(0.0);
				}	
			}
//			String[] values = nodeDistribs[index].toString().split(";");
			//			double out = one + two + three;
//			return samples;
		}
		return samples;
	}
	
	public List<Double> loadReal() {
		List<Double> samples = new ArrayList<Double>(data.length);
		for (int i = 0; i < data.length; i++) { //For row of data
			// set variables according to observations
			Object testVal = null;
			for (int j = 0; j < vars.length; j++) { //For each node/variable
				BNode instantiate_me = bn.getNode(vars[j]);
				if (instantiate_me.equals(testNode)) {
					testVal = data[i][j];
					instantiate_me.resetInstance();
				} else if (data[i][j] != null) { // check so that the observation is not null
					// the node is instantiated to the value in the data set
					instantiate_me.setInstance(data[i][j]);
				} else { // the observation is null
					// the node is reset, i.e. un-instantiated 
					instantiate_me.resetInstance();
				}
			}
			CGVarElim ve = new CGVarElim();
			ve.instantiate(bn);
			Query q = ve.makeQuery(testNode.getVariable());
			CGTable cg = (CGTable)ve.infer(q);
			Distrib d = cg.query(testNode.getVariable());
//			cg.display();
//			System.out.println(a.toString());
//			System.out.println(d.toString());
					 
	        double sum = 0;
	        int nElem = 5000;
	        double minw = 0.0;
//	        double maxw = 5;
	        double[] hist = new double[nElem];
	        for (int k = 0; k < 50000; k ++) {
	            double s = (Double)d.sample();
	            if (s >= minw)
	                hist[(int)(s*1000.0)] += 1;
	            sum += s;
	        }
	        double maxVal = 0.0;
	        double maxBucket = 0.0;
	        for (int l = 0; l < hist.length; l ++) {
	        	if (hist[l] > maxVal) {
	        		maxVal = hist[l];
	        		maxBucket = (double)l/1000.0;
	        	}
	        }
	        samples.add(maxBucket);
	        
	      //Integration to find probability of result
			if ((double)testVal >= 0.0) {
				double delta = 0.01;
				double up = (double)testVal + delta;
				double low = (double)testVal - delta;
				double upBound = d.get(up);
				double lowBound = d.get(low);
				double area = ((upBound + lowBound)/2)*(up-low); //area of trapezoid (a+b)/2 * h
				samples.add(area);
				
			} else {
				samples.add(-1.0);
			}
						
			//Find median of non-zero buckets
			List<Double> store = new ArrayList<>();
			for (int j = 0; j < hist.length; j++) {
				if (hist[j] > 0) {
					double bucketVal = (double)(j/1000.0);
					store.add(bucketVal);
				}
			}
			double[] values = new double[store.size()];
			for (int k = 0; k < store.size(); k++) {
				values[k] = store.get(k);
			}
			Median med = new Median();
			double median = med.evaluate(values);
			samples.add(median);
	        
		}
				
		//RESET ALL NODES
		for (BNode n : bn.getNodes()) {
			n.resetInstance();
		}
		
		return samples;
	}
	
	public List<Double> loadReal(double rangeMin, double rangeMax, int histElements) {
		List<Double> samples = new ArrayList<Double>(data.length);
		ArrayList<double[]> histograms = new ArrayList<>(data.length);
		List<Double> testvals = new ArrayList<Double>(data.length);
		ArrayList nodeValues = new ArrayList(data[0].length);
		
//		PrintWriter writer = null;
//		try {
//			writer = new PrintWriter("testHistOut.txt");
//		} catch (FileNotFoundException e) {
//			System.out.println("Invalid filename");
//		}
		
		for (int i = 0; i < data.length; i++) { //For row of data
			// set variables according to observations
			Object testVal = null;
			for (int j = 0; j < vars.length; j++) { //For each node/variable
				BNode instantiate_me = bn.getNode(vars[j]);
				nodeValues.add(instantiate_me.getInstance());
				//if it is the test node, the node is reset (so there is no evidence)
				if (instantiate_me.equals(testNode)){
					testVal = data[i][j];
					instantiate_me.resetInstance();
					//only use when want to still train on score node (so loaded data has score group specified).
					//		                }else if (instantiate_me.getName().equals("Score_group")){
					//		                    instantiate_me.resetInstance();
				}else if (data[i][j] != null) { // check so that the observation is not null
					// the node is instantiated to the value in the data set
					instantiate_me.setInstance(data[i][j]);
				} else { // the observation is null
					// the node is reset, i.e. un-instantiated 
					instantiate_me.resetInstance();
				}
			}
			
//			ApproxInference ai = new ApproxInference();
//			ai.instantiate(bn);
//			ai.setIterations(10000);
//			Query q = ai.makeQuery(testNode.getVariable());
//			CGTable answer = ai.infer(q);
//			Distrib d = answer.query(testNode.getVariable());
			
			CGVarElim ve = new CGVarElim();
			ve.instantiate(bn);
			Query q = ve.makeQuery(testNode.getVariable());
			CGTable cg = (CGTable)ve.infer(q);
			Distrib d = cg.query(testNode.getVariable());
			//Sample from the distribution
			double sum = 0;
			int nElem = histElements; //number of elements
			double minE = rangeMin; //smallest value
			double maxE = rangeMax; //largest value
			double[] hist = new double[nElem];
			double binSize = (maxE - minE)/nElem;
			int x = 0;
			double prediction;
			try {
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
				//		                prediction = (maxindex * ((maxE - minE) / nElem)) + minE; //The bin with the highest counts
				//		                System.out.println(testVal + "\t" + prediction + "\t" + nodeValues);
				histograms.add(hist);
			}catch (NullPointerException e) {
				//		                System.out.println(testVal + "\t" + null + "\t" + nodeValues);
				//		                System.err.println("Null error");
				prediction = -100;
			}
			samples.add(prediction);
			
			//Integration to find probability of result
//			if ((double)testVal >= 0.0) {
//				double delta = 0.01;
//				double up = (double)testVal + delta;
//				double low = (double)testVal - delta;
//				double upBound = d.get(up);
//				double lowBound = d.get(low);
//				double area = ((upBound + lowBound)/2)*(up-low); //area of trapezoid (a+b)/2 * h
//				samples.add(area);
//				
//			} else {
//				samples.add(-1.0);
//			}
			samples.add(-1.0);
						
			//Find median of non-zero buckets
			List<Double> store = new ArrayList<>();
			for (int j = 0; j < hist.length; j++) {
				if (hist[j] > 0) {
					double bucketVal = (double)(j * binSize) + minE;
					store.add(bucketVal);
				}
			}
			double[] values = new double[store.size()];
			for (int k = 0; k < store.size(); k++) {
				values[k] = store.get(k);
			}
			Median med = new Median();
			double median = med.evaluate(values);
			samples.add(median);			
			
//			List<BNode> order = bn.getAlphabetical();
//			int index = order.indexOf(bn.getNode("VarianceC"));
			
//			System.out.println(prediction);
			if (prediction >= 0) {
//				System.out.println(testVal + "\t"+ prediction + "\t" + median + "\t" + Arrays.toString(hist));
			}
			
		}
//		writer.close();
		for (BNode node : bn.getNodes()){
			node.resetInstance();
		}
		return samples;
	}

	public int[][] loadDirichlet() {
		int[][] samples = new int[data.length][];
		ArrayList<double[]> histograms = new ArrayList<>(data.length);
		List<Double> testvals = new ArrayList<Double>(data.length);
		ArrayList nodeValues = new ArrayList(data[0].length);

//		PrintWriter writer = null;
//		try {
//			writer = new PrintWriter("testHistOut.txt");
//		} catch (FileNotFoundException e) {
//			System.out.println("Invalid filename");
//		}

		for (int i = 0; i < data.length; i++) { //For row of data
			// set variables according to observations
			Object testVal = null;
			for (int j = 0; j < vars.length; j++) { //For each node/variable
				BNode instantiate_me = bn.getNode(vars[j]);
				nodeValues.add(instantiate_me.getInstance());
				//if it is the test node, the node is reset (so there is no evidence)
				if (instantiate_me.equals(testNode)){
					testVal = data[i][j];
					instantiate_me.resetInstance();
				}else if (data[i][j] != null) { // check so that the observation is not null
					// the node is instantiated to the value in the data set
					instantiate_me.setInstance(data[i][j]);
				} else { // the observation is null
					// the node is reset, i.e. un-instantiated
					instantiate_me.resetInstance();
				}
			}

//			ApproxInference ai = new ApproxInference();
//			ai.instantiate(bn);
//			ai.setIterations(10000);
//			Query q = ai.makeQuery(testNode.getVariable());
//			CGTable answer = ai.infer(q);
//			Distrib d = answer.query(testNode.getVariable());

			CGVarElim ve = new CGVarElim();
			ve.instantiate(bn);
			Query q = ve.makeQuery(testNode.getVariable());
			CGTable cg = (CGTable)ve.infer(q);
			Distrib d = cg.query(testNode.getVariable());
			//Sample from the distribution
			int x = 0;
			int domain = 0;
            Map<String, Integer> hist = new HashMap<>();
			try {
				while (x < 500) { //sample from the distribution
					EnumDistrib sample = (EnumDistrib)d.sample();
                    domain = sample.getDomain().size();
                    int y = 0;
                    while (y < 100) {
                        String s = (String) sample.sample();
                        if (hist.containsKey(s)) {
                            Integer value = hist.get(s);
                            hist.put(s, value + 1);
                        } else {
                            hist.put(s, 1);
                        }
                        y++;
                    }
                    x++;
                }
			}catch (NullPointerException e) {
				//		                System.out.println(testVal + "\t" + null + "\t" + nodeValues);
				//		                System.err.println("Null error");
//				prediction = -100;
			}
            //Convert map histogram to ordered array
            int[] histSorted = new int[domain];
            for (int pos = 0; pos < domain; pos++) {
                String bin = String.valueOf(pos);
                int value = 0;
                try {
                    value = hist.get(bin);
                } catch (NullPointerException npe) {
                    value = 0;
                }
                histSorted[pos] = value;
            }
            samples[i] = histSorted;
		}
//		writer.close();
		for (BNode node : bn.getNodes()){
			node.resetInstance();
		}
		return samples;
	}
	
}
