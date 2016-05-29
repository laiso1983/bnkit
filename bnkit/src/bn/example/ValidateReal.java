package bn.example;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import sim.CrossValidate;
import sim.Model;
import sim.dataset.DataBuffer;
import sim.model.UntaggedTrainable;
import sim.result.ResultReal;
import sim.result.ResultSummary;
import bn.BNet;
import bn.BNode;
import bn.alg.EM;
import bn.file.BNBuf;
import bn.file.DataBuf;


public class ValidateReal {
	int nSets; // k-fold crossvalidation
	int seed = (int)System.currentTimeMillis();
	String bn_file;
	List<BNode> order;
	int maxRounds;
	int emCase;
	String testNode;
	double convergence;
	double pearson;
	double spearman;
	
	public ValidateReal(String bn_file, String data_file, int fold,int maxRounds, int emCase, String testNode, double convergence) {
		
		//Load data
		this.bn_file = bn_file;
		BNet bn = BNBuf.load(bn_file);
		this.order = bn.getAlphabetical();
		Object[][] data = DataBuf.load(data_file, order);
		this.nSets = fold;
		this.maxRounds = maxRounds;
		this.emCase = emCase;
		this.testNode = testNode;
		this.convergence = convergence;
		
		// put all sequence data in a DataBuffer
		DataBuffer<Object[]> dbuf=new DataBuffer<Object[]>();
		dbuf.putAll(data);  
		
		// Create models
		ArrayList<Network> model=new ArrayList<Network>();
			for (int m=0; m<nSets; m++) {
				model.add(new Network());
			}
		// create cross-validation simulation
		CrossValidate<Network, ResultReal<Object>, Object[]> cv=new CrossValidate<>(model, dbuf, seed);
		cv.train();

		ResultSummary<ResultReal<Object>> rsum1 = new ResultSummary<ResultReal<Object>>(cv.test());
		List<ResultReal<Object>> results = rsum1.getResults();
		if (bn.getNode(testNode).getVariable().getPredef().equals("Real")) {
			int index = order.indexOf(bn.getNode(testNode));
			double[] input = new double[results.size()];
			double[] predictions = new double[results.size()];
			double[] probabilities = new double[results.size()];
			double[] medians = new double[results.size()];
			double[][] matrix = new double[results.size()][2];
			int j = 0;
			int k = 0;
			double sum = 0.0;
			for (ResultReal<Object> r : results) {
				Object[] s = (Object[]) r.getSample();
//				if (r.getOutput()[0] >= 0){
//					input[j] = (double) s[index];
//					output[j] = r.getOutput()[0];
//					j++;
//				}	
				input[j] = (double) s[index];
				matrix[j][k] = (double) s[index];
				predictions[j] = r.getOutput()[0];
				matrix[j][k+1] = r.getOutput()[0];
				probabilities[j] = r.getOutput()[1];
				medians[j] = r.getOutput()[2];
				sum += r.getOutput()[1];
				j++;
			}
			
			PearsonsCorrelation pc = new PearsonsCorrelation(matrix);
			SpearmansCorrelation sc = new SpearmansCorrelation();
			double res = pc.correlation(input,  predictions);
			RealMatrix pearP = pc.getCorrelationPValues();
			double pp = pearP.getEntry(0, 1);
			double spear = sc.correlation(input,  predictions);
			
			double medRes = pc.correlation(input, medians);
//			double medSpear = sc.correlation(input, medians);
			
			this.pearson = res;
			this.spearman = spear;
			
			PrintWriter writer = null;
			try {
				writer = new PrintWriter("rf-data-test.txt");
			} catch (FileNotFoundException e) {
				System.out.println("Invalid filename");
			}
//			
//			writer.println("Sum of Probabilities: " + sum);
//			writer.println("Pearson's: " +res);
//			writer.println("Spearman's: " +spear);
//			writer.println("Median Pearson's: " +medRes);
//			writer.println("Median Spearman's: " +medSpear);
			writer.println(Arrays.toString(input));
			writer.println(Arrays.toString(predictions));
			writer.println(Arrays.toString(probabilities));
			writer.println(Arrays.toString(medians));
			writer.close();
//			
			System.out.println("Sum of Probabilities: " + sum);
			System.out.println("Pearson's: " +res);
			System.out.println("Spearman's: " +spear);
			System.out.println("Pearson's P-VALUE: " +pp);
			System.out.println("Median Pearson's: " +medRes);
//			System.out.println("Median Spearman's: " +medSpear);
//			System.out.println(Arrays.toString(input));
//			System.out.println(Arrays.toString(predictions));
		} else {
			double zeros = 0.0;
			double ones = 0.0;
			for (ResultReal<Object> r : results) {	
				double result = r.getOutput()[0];
				if (result == 0.0) {
					zeros++;
				} else {
					ones++;
				}
			}
			double prob = ones/(ones + zeros);
			
//			System.out.println("Probability = "+ prob);
		}	

	}
	
	class Network extends Model<ResultReal<Object>, Object[]> implements UntaggedTrainable<Object[]> {

		private static final long serialVersionUID = 1L;
		BNet bn;
		List<BNode> curOrder = new ArrayList<>();
		
		public Network() {
			BNet bn = BNBuf.reload(bn_file);
//			BNet bn = BNBuf.load(bn_file);
			this.bn = bn;
		}
		
		public ResultReal<Object> test(Object[] sample) {
			Object[][] data = new Object[1][sample.length];
			for (int col = 0; col < sample.length; col++) {
//				Object e = sample[col];
				data[0][col] = sample[col];
			}
			TestNetwork tn = new TestNetwork(bn, data, testNode);
//			JPT jpt = tn.load();
//			List<Double> samples = tn.loadReal();
//			List<Double> samples = tn.loadReal(0.0, 5, 300);
			List<Double> samples = new ArrayList<>();
			try {
				samples = tn.loadDiscrete();
			} catch (ClassCastException | NullPointerException e) {
				samples = tn.loadReal(0.0, 5, 300);
//				samples = tn.loadReal();
			}
			
			double[] output = new double[samples.size()];
			for (int m = 0; m < samples.size(); m++) {
				output[m] = samples.get(m);
			}
			
			return new ResultReal<Object>(sample, output);
		}
  
		public void train(Set<Object[]> sample) {
//			ApproxInference ai = new ApproxInference();
//			LearningAlg em = new EMA(bn, ai);
//			CGVarElimOld cg = new CGVarElimOld();
//			LearningAlg em = new EM(bn, cg);
//			LearningAlg em = new EM(bn);
			
			EM em = new EM(bn);
			em.setEMOption(emCase);
			em.setPrintStatus(true);
			em.setMaxRounds(maxRounds);
			em.setConvergeCrit(convergence);
			
			Set<Object[]> test = sample;
			Object[] c = (Object[]) test.toArray()[0];
			Object[][] data = new Object[test.size()][c.length];
			for (int row = 0; row < test.size(); row++) {
				Object[] values = (Object[]) test.toArray()[row];
				for (int col = 0; col < values.length; col++) {
					Object e = values[col];
					data[row][col] = values[col];
				}
			}
			System.out.println();
			List<BNode> nodes = bn.getAlphabetical();
			em.train(data, nodes);
			BNBuf.save(bn, System.currentTimeMillis()+"cg.new");
		}
		
//		@Override
//		public void train(Map<Object[], String> sample) {
//			LearningAlg em = new EM(bn);
//			Object[][] data = new Object[sample.size()][];
//			int i=0;
//			for (Map.Entry<Object[], String> entry:sample.entrySet()) { 
//				Object[] key = entry.getKey();
//				data[i]=new Object[key.length];
//				for (int j=0; j<key.length; j++)
//					data[i][j]=key[j];						// put it in the data matrix
//				i++;
//			}
//			List<BNode> nodes = bn.getOrdered();
//			em.train(data, nodes);
//			
//		}

		
	}	
	
	public double getPearson() {
		return pearson;
	}
	
	public double getSpearman() {
		return spearman;
	}
}
