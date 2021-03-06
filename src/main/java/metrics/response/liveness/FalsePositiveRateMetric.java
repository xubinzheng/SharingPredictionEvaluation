package metrics.response.liveness;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import metrics.MetricResult;
import metrics.StatisticsResult;

public class FalsePositiveRateMetric implements ResponseLivenessMetric {
	
	public static ResponseLivenessMetricFactory factory() {
		return new ResponseLivenessMetricFactory() {
			
			@Override
			public ResponseLivenessMetric create() {
				return new FalsePositiveRateMetric();
			}
		};
	}

	DescriptiveStatistics stats = new DescriptiveStatistics();
	
	@Override
	public String getHeader() {
		return "mean-false positive rate,stdev-false positive rate";
	}

	@Override
	public void addTestResult(List<Double> trueTimes, List<Double> livenessPredictions) {
		int truePositives = 0;
		int falseNegatives = 0;
		for (int i=0; i<trueTimes.size(); i++) {
			Double trueTime = trueTimes.get(i);
			Double livenessPrediction = livenessPredictions.get(i);
			
			if (livenessPrediction >= 0.5 ) {
				if (trueTime != Double.POSITIVE_INFINITY) {
					truePositives++;
				}
			} else {
				if (trueTime == Double.POSITIVE_INFINITY) {
					falseNegatives++;
				}
			}
		}
		stats.addValue(((double) falseNegatives)/(falseNegatives + truePositives));

	}

	@Override
	public MetricResult evaluate() {
		return new StatisticsResult(stats);
	}	

}
