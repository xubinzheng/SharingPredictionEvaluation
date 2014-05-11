package metrics.recipients;

import general.actionbased.messages.SingleMessage;

import java.util.Collection;

import metrics.DoubleResult;
import metrics.MetricResult;

public class MessagesWithSmallSeedsMetric<RecipientType, MessageType extends SingleMessage<RecipientType>>
		implements RecipientMetric<RecipientType, MessageType> {

	private int numMessages = 0;
	private int numMessagesWithSmallSeeds = 0;

	public static <RecipientType, MessageType extends SingleMessage<RecipientType>> RecipientMetricFactory<RecipientType, MessageType> factory(
			Class<RecipientType> recipientClass, Class<MessageType> messageClass) {

		return new RecipientMetricFactory<RecipientType, MessageType>() {

			@Override
			public RecipientMetric<RecipientType, MessageType> create() {
				return new MessagesWithSmallSeedsMetric<>();
			}
		};
	}

	@Override
	public String getHeader() {
		return "messages with small seeds";
	}

	@Override
	public void addMessageResult(SingleMessage<RecipientType> message,
			Collection<RecipientAddressingEvents> events) {

		numMessages++;
		if (events
				.contains(RecipientAddressingEvents.SeedTooSmallForListGeneration)) {
			numMessagesWithSmallSeeds++;
		}
	}

	@Override
	public MetricResult evaluate(Collection<MessageType> trainMessages,
			Collection<MessageType> testMessages) {
		return new DoubleResult((double) numMessagesWithSmallSeeds
				/ numMessages);
	}

}
