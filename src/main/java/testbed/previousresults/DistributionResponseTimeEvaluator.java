package testbed.previousresults;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import metrics.response.time.ResponseTimeMetric;

import org.apache.commons.io.FileUtils;

import prediction.response.time.DistributionResponseTimePredictor;
import prediction.response.time.ResponseTimeRange;
import testbed.dataset.actions.messages.MessageDataset;
import data.representation.actionbased.messages.MessageThread;
import data.representation.actionbased.messages.SingleMessage;

public class DistributionResponseTimeEvaluator<Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>>
		extends ResponseTimeEvaluator<Id, Recipient, Message, ThreadType> {

	public static <Id, Recipient, Message extends SingleMessage<Recipient>, ThreadType extends MessageThread<Recipient, Message>> ResponseTimeEvaluatorFactory<Id, Recipient, Message, ThreadType> factory(
			Class<Id> idClass,
			Class<Recipient> recipientClass,
			Class<Message> messageClass,
			Class<ThreadType> threadClass,
			final DistributionResponseTimePredictor predictor) {
		return new ResponseTimeEvaluatorFactory<Id, Recipient, Message, ThreadType>() {

			@Override
			public ResponseTimeEvaluator<Id, Recipient, Message, ThreadType> create(
					MessageDataset<Id, Recipient, Message, ThreadType> dataset,
					Collection<ResponseTimeMetric> metrics) {
				return new DistributionResponseTimeEvaluator<>(dataset, metrics, predictor);
			}
		};
	}

	private DistributionResponseTimePredictor predictor;
	private File gradientAscentFolder;

	public DistributionResponseTimeEvaluator(
			MessageDataset<Id, Recipient, Message, ThreadType> dataset,
			Collection<ResponseTimeMetric> metrics, DistributionResponseTimePredictor predictor) {
		super(dataset, metrics);
		this.predictor = predictor;
		this.gradientAscentFolder = new File(new File(timeFolder, "gradient ascent"),
				"expected times");
	}

	@Override
	protected List<ResponseTimeRange> getPredictedResponseTimes(Integer test) throws IOException {
		File resultsFolder = new File(gradientAscentFolder, "" + test);
		File[] iterationFiles = resultsFolder.listFiles();
		Arrays.sort(iterationFiles, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				Integer val1 = Integer.parseInt(o1.getName());
				Integer val2 = Integer.parseInt(o2.getName());
				return val1.compareTo(val2);
			}
		});
		List<String> lines = FileUtils.readLines(iterationFiles[iterationFiles.length - 1]);
		List<ResponseTimeRange> predictions = new ArrayList<>();
		for (String line : lines) {
			predictions.add(new ResponseTimeRange(predictor.getPrediction(), null));
		}
		return predictions;
	}

	@Override
	public String getType() {
		return "distribution-based prediction - " + predictor.getLabel() + ",N/A";
	}
}
