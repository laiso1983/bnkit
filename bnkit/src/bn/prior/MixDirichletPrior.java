package bn.prior;

import java.util.Arrays;

import dat.Enumerable;
import bn.Distrib;
import bn.prob.DirichletDistrib;
import bn.prob.EnumDistrib;
import bn.prob.GammaDistrib;
import bn.prob.MixDirichletDistrib;

public class MixDirichletPrior extends MixDirichletDistrib implements Prior {
	
	double[][] alpha;
	double[] m;
	private EnumDistrib likelihoodDistrib;
	private double[] countVector;
	
	public MixDirichletPrior(Enumerable domain, int component) {
		super(domain, component);
		alpha = new double[component][domain.size()];
		m = new double[component];
		countVector = new double[domain.size()];
		Arrays.fill(countVector, 0.0);
		for(int i = 0; i < component; i++) {
            DirichletDistrib dirichlet = (DirichletDistrib)this.getDistrib(i); 
            System.arraycopy(dirichlet.getAlpha(), 0, alpha[i], 0, domain.size());
            m[i] = this.getWeights(i);
		}
		
	}
	
	private double probCountVector(double[] alpha) {
		double result = 0.0;
		int sumCountVector = 0;
		double sumAlpha = 0;
		if(countVector.length != alpha.length) {
			throw new RuntimeException("the length of count vector and alpha should be same");
		}
		for(int i = 0; i < countVector.length; i++) {
			sumCountVector += countVector[i];
			sumAlpha += alpha[i];
		}
		result = GammaDistrib.lgamma(sumCountVector + 1) + GammaDistrib.lgamma(sumAlpha) - GammaDistrib.lgamma(sumCountVector + sumAlpha);
		for(int i = 0; i < countVector.length; i++) {
			result = GammaDistrib.lgamma(countVector[i] + alpha[i]) - GammaDistrib.lgamma(countVector[i] + 1) - GammaDistrib.lgamma(alpha[i]);
		}
		return result;
	}

	/**
	 * This algorithm comes from 
	 * Sj�lander, K., Karplus, K., e.l.(1996). 
	 * Dirichlet mixtures: a method for improved detection 
	 * of weak but significant protein sequence homology. 
	 * Computer applications in the biosciences: CABIOS, 12(4), 327-345.
	 */
	@Override
	public void learn(Object[] data, double[] prob) {
		Enumerable domain = getDomain();
		
		if(likelihoodDistrib == null) {
			System.err.println("likelihood distribution should be specificed");
			return;
		}
		/**
		 * get the count vector for each variable
		 */
		for(int i = 0; i < data.length; i++) {
			Object point = data[i];
			countVector[domain.getIndex(point)] += prob[i];
		}
	}

	@Override
	public void setLikelihoodDistrib(Distrib distrib) {
		try {
			likelihoodDistrib = (EnumDistrib) distrib;
		} catch(ClassCastException e) {
			System.out.println("the likelihood for Mixture Dirichlet prior should be enum distribution");
		}

	}

	@Override
	public Distrib getBayesDistrib() {
		Enumerable domain = getDomain();
		double[] prob = new double[domain.size()];
		double[] alphaSums = new double[this.getMixtureSize()];
		double probSum = 0.0;
		int countSum = 0;
		double[] dist = new double[domain.size()];
		for(int i = 0; i < domain.size(); i++) {
			countSum += countVector[i];
		}
		Arrays.fill(alphaSums, 0);
		for(int i = 0; i < this.getMixtureSize(); i++) {
			DirichletDistrib dirichlet = (DirichletDistrib) this.getDistrib(i);
			double[] alpha = dirichlet.getAlpha();
			prob[i] = this.getWeights(i) * Math.exp(probCountVector(alpha));
			probSum += prob[i];
			for(int j = 0; j < domain.size(); j++) {
				alphaSums[i] += alpha[j]; 
			}
		}
		Arrays.fill(dist, 0);
		for(int i = 0; i < domain.size(); i++) {
			for(int j = 0; j < this.getMixtureSize(); j++) {
				DirichletDistrib dirichlet = (DirichletDistrib) this.getDistrib(j);
				dist[i] = (prob[i] / probSum);
				dist[i] *= ((this.countVector[i] + dirichlet.getAlpha()[i]) / (countSum + alphaSums[j]));
			}
			
		}
		likelihoodDistrib.set(dist);
		return likelihoodDistrib;
	}

	@Override
	public void resetParameters() {
		this.setWeights(m);
		for(int i = 0; i < this.getMixtureSize(); i++) {
			DirichletDistrib dirichlet = (DirichletDistrib)this.getDistrib(i); 
			dirichlet.setPrior(alpha[i]);
		}

	}

	@Override
	public void learnPrior(Object[] data, double[] prob) {
		int[][] learningData = new int[data.length][];
		for(int i = 0; i < data.length; i++) {
			try {
				learningData[i] = (int[])data[i];
			} catch (ClassCastException e) {
				throw new ClassCastException("only accept int[][] data type");
			}
		}
		this.learnParameters(learningData);
		for(int i = 0; i < this.getMixtureSize(); i++) {
            DirichletDistrib dirichlet = (DirichletDistrib)this.getDistrib(i); 
            System.arraycopy(dirichlet.getAlpha(), 0, alpha[i], 0, this.getDomain().size());
            m[i] = this.getWeights(i);
		}
	}

}