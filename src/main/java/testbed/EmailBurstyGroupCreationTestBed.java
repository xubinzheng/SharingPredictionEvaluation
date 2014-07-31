package testbed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import metrics.Metric;
import metrics.MetricResult;
import metrics.MetricResultCollection;
import metrics.groups.actionbased.GroupCenteredPercentAddedMetric;
import metrics.groups.actionbased.GroupCenteredPercentDeletedMetric;
import metrics.groups.actionbased.MessageCenteredPercentAddedMetric;
import metrics.groups.actionbased.MessageCenteredPercentDeletedMetric;
import metrics.groups.actionbased.RecommendationsMatchedToTestActionMetric;
import metrics.groups.actionbased.RecommendationsToTestActionPerfectMatchesMetric;
import metrics.groups.actionbased.TestActionsMatchedToRecommendationMetric;
import metrics.groups.actionbased.TestActionsToRecommendationPerfectMatchesMetric;
import metrics.groups.actionbased.TotalRecommendedGroupsMetric;
import metrics.groups.actionbased.TotalTestActionsMetric;
import metrics.groups.actionbased.bursty.BurstyGroupMetric;
import metrics.groups.actionbased.bursty.BurstyGroupMetricFactory;
import metrics.groups.actionbased.bursty.MessagesTriggeringGroupCreationMetric;
import metrics.groups.actionbased.bursty.RecommendationsCreatedPerBurstMetric;
import metrics.groups.actionbased.bursty.RepurposedActionBasedGroupMetric;
import model.recommendation.groups.BurstyGroupRecommendationAcceptanceModeler;
import recommendation.groups.seedless.SeedlessGroupRecommenderFactory;
import recommendation.groups.seedless.actionbased.GraphFormingActionBasedSeedlessGroupRecommender;
import recommendation.groups.seedless.actionbased.bursty.BurstyGroupRecommender;
import recommendation.groups.seedless.actionbased.bursty.RelativeEditsThresholdMatcher;
import recommendation.groups.seedless.hybrid.HybridRecommenderFactory;
import testbed.dataset.actions.ActionsDataSet;
import testbed.dataset.actions.messages.email.EnronEmailDataSet;
import data.preprocess.graphbuilder.ActionBasedGraphBuilder;
import data.preprocess.graphbuilder.ActionBasedGraphBuilderFactory;
import data.preprocess.graphbuilder.InteractionRankWeightedActionBasedGraphBuilder;
import data.preprocess.graphbuilder.SimpleActionBasedGraphBuilder;
import data.preprocess.graphbuilder.TimeThresholdActionBasedGraphBuilder;
import data.representation.actionbased.CollaborativeAction;
import data.representation.actionbased.messages.email.EmailMessage;
import data.representation.actionbased.messages.email.EmailThread;

public class EmailBurstyGroupCreationTestBed {
	static double percentTraining = 0.8;

	static Collection<ActionsDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>>> dataSets = new ArrayList<>();

	static Collection<SeedlessGroupRecommenderFactory<String>> seedlessRecommenderFactories = new ArrayList<>();
	static Collection<ActionBasedGraphBuilderFactory<String,EmailMessage<String>>> graphBuilderFactories = new ArrayList<>();

	static Map<Class<? extends ActionBasedGraphBuilder>, Collection<ConstantValues>> constantValues = new HashMap<>();

	static Collection<Double> distanceThresholds = new ArrayList<>();

	static Collection<BurstyGroupMetricFactory<String, EmailMessage<String>>> metricFactories = new ArrayList<>();

	static {

		// Add data sets
		dataSets.add(new EnronEmailDataSet("enron",
				EnronEmailDataSet.DEFAULT_ACCOUNTS, new File("data/Enron")));

		// Add seedless recommender factories
		seedlessRecommenderFactories.add(new HybridRecommenderFactory<String>(false));
		//seedlessRecommenderFactories.add(new FellowsRecommenderFactory<String>());

		// Add graph builders
		graphBuilderFactories.add(SimpleActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(TimeThresholdActionBasedGraphBuilder.factory(String.class, EmailMessage.class));
		graphBuilderFactories.add(InteractionRankWeightedActionBasedGraphBuilder.factory(String.class, EmailMessage.class));

		
		// Add constants for simple graph builder
		Collection<ConstantValues> simpleConstants = new ArrayList<>();
		simpleConstants.add(new ConstantValues(new Object[0]));
		constantValues.put(SimpleActionBasedGraphBuilder.class, simpleConstants);
		
		// Add constants for time threshold graph builder
		Collection<ConstantValues> timeThresholdConstants = new ArrayList<>();
		Object[] timeThresholdConstantSet1 = {1000L*60*60*24*7}; //1.0 weeks
		Object[] timeThresholdConstantSet2 = {1000L*60*60*24*7*4}; //4.0 weeks
		Object[] timeThresholdConstantSet3 = {1000L*60*60*24*365/2}; //0.5 years
		Object[] timeThresholdConstantSet4 = {1000L*60*60*24*365}; //1 year
		Object[] timeThresholdConstantSet5 = {1000L*60*60*24*365*2}; //2 years
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet1));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet2));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet3));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet4));
		timeThresholdConstants.add(new ConstantValues(timeThresholdConstantSet5));
		constantValues.put(TimeThresholdActionBasedGraphBuilder.class, timeThresholdConstants);
		
		// Add constants for interaction rank graph builder
		Collection<ConstantValues> interactionRankConstants = new ArrayList<>();
		Object[] interactionRankConstantSet1 = {1.0, 1000L*60*60*24*7*4, 0.01}; //wOut=1.0, halfLife=1.0 weeks, threshold=0.01
		Object[] interactionRankConstantSet2 = {1.0, 1000L*60*60*24*7*4, 0.01}; //wOut=1.0, halfLife=4 weeks, threshold=0.01
		Object[] interactionRankConstantSet3 = {1.0, 1000L*60*60*24*365/2, 1.0}; //wOut=1.0, halfLife=0.5 years, threshold=1.0
		Object[] interactionRankConstantSet4 = {1.0, 1000L*60*60*24*365, 1.25}; //wOut=1.0, halfLife=1 year, threshold=1.25
		Object[] interactionRankConstantSet5 = {1.0, 1000L*60*60*24*365*2, 1.75}; //wOut=1.0, halfLife=2 years, threshold=1.75
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet1));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet2));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet3));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet4));
		interactionRankConstants.add(new ConstantValues(interactionRankConstantSet5));
		constantValues.put(InteractionRankWeightedActionBasedGraphBuilder.class, interactionRankConstants);

		// Add thresholds for seed and recommendation matchers
		distanceThresholds.add(0.00);
		//distanceThresholds.add(0.05);
		distanceThresholds.add(0.10);
		//distanceThresholds.add(0.15);
		distanceThresholds.add(0.20);
		//distanceThresholds.add(0.25);
		//distanceThresholds.add(0.30);

		// Add metrics
		metricFactories.add(MessagesTriggeringGroupCreationMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RecommendationsCreatedPerBurstMetric.factory(String.class, EmailMessage.class));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalTestActionsMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TotalRecommendedGroupsMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentDeletedMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new GroupCenteredPercentAddedMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentDeletedMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new MessageCenteredPercentAddedMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsMatchedToRecommendationMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new TestActionsToRecommendationPerfectMatchesMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsMatchedToTestActionMetric<String, EmailMessage<String>>()));
		metricFactories.add(RepurposedActionBasedGroupMetric.factory(new RecommendationsToTestActionPerfectMatchesMetric<String, EmailMessage<String>>()));

	}

	private static String getHalfLifeName(double halfLife) {
		if (halfLife < 1000) {
			return halfLife + " ms";
		}
		halfLife /= 1000;
		if (halfLife < 60) {
			return halfLife + " seconds";
		}
		halfLife /= 60;
		if (halfLife < 60) {
			return halfLife + " minutes";
		}
		halfLife /= 60;
		if (halfLife < 24) {
			return halfLife + " hours";
		}
		halfLife /= 24;
		if (halfLife < 7) {
			return halfLife + " days";
		}
		if (halfLife <= 28) {
			return halfLife / 7 + " weeks";
		}
		halfLife /= 365;
		return halfLife + " years";
	}

	private static Collection<MetricResult> collectResults(
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			Double seedClosenessThreshold,
			Double recommendationClosenessThreshold,
			GraphFormingActionBasedSeedlessGroupRecommender<String,EmailMessage<String>> recommender) {

		ArrayList<BurstyGroupMetric<String, EmailMessage<String>>> metrics = new ArrayList<>();
		for (BurstyGroupMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
			metrics.add(metricFactory.create());
		}

		BurstyGroupRecommender<String,EmailMessage<String>> burstyRecommender = new BurstyGroupRecommender<>(
				recommender, new RelativeEditsThresholdMatcher<String>(
						seedClosenessThreshold),
				new RelativeEditsThresholdMatcher<String>(
						recommendationClosenessThreshold));
		BurstyGroupRecommendationAcceptanceModeler<String, EmailMessage<String>> modeler = new BurstyGroupRecommendationAcceptanceModeler<String, EmailMessage<String>>(
				burstyRecommender, trainMessages, testMessages,
				new ArrayList<Set<String>>(), metrics);

		Collection<MetricResult> results = modeler
				.modelRecommendationAcceptance();
		return results;
	}

	private static void useGraphBuilderNoArgs(
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String,EmailMessage<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {

		ActionBasedGraphBuilder<String,EmailMessage<String>> graphBuilder = graphBuilderFactory
				.create();

		GraphFormingActionBasedSeedlessGroupRecommender<String,EmailMessage<String>> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
				seedlessRecommenderFactory, graphBuilder);
		
		Collection<ConstantValues> constantSets = constantValues.get(graphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {
					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += "N/A,N/A,N/A";
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection.addResults(label, account, results);
				}
			}
		}

	}

	private static void useGraphBuilderTimeThreshold(
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String,EmailMessage<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {

		

		ActionBasedGraphBuilder<String,EmailMessage<String>> tempGraphBuilder = graphBuilderFactory
				.create(0L);
		
		Collection<ConstantValues> constantSets = constantValues.get(tempGraphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			Long timeThreshold = (Long) constantSet.constants[0];
			
			ActionBasedGraphBuilder<String,EmailMessage<String>> graphBuilder = graphBuilderFactory
					.create(timeThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<String,EmailMessage<String>> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
					seedlessRecommenderFactory, graphBuilder);
			
			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {
					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += "N/A,N/A,"
							+ getHalfLifeName(timeThreshold);
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection.addResults(label, account, results);
				}
			}
			
		}
	}

	private static void useGraphBuilderScoredEdges(
			String account,
			Collection<EmailMessage<String>> trainMessages,
			Collection<EmailMessage<String>> testMessages,
			SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory,
			ActionBasedGraphBuilderFactory<String,EmailMessage<String>> graphBuilderFactory,
			MetricResultCollection<String> resultCollection) throws IOException {

		ActionBasedGraphBuilder<String,EmailMessage<String>> tempGraphBuilder = graphBuilderFactory
				.create(0L, 0.0, 0.0);
		
		Collection<ConstantValues> constantSets = constantValues.get(tempGraphBuilder.getClass());
		for (ConstantValues constantSet : constantSets) {
			
			Double wOut = (Double) constantSet.constants[0];
			Long halfLife = (Long) constantSet.constants[1];
			Double scoreThreshold = (Double) constantSet.constants[2];
			
			ActionBasedGraphBuilder<String,EmailMessage<String>> graphBuilder = graphBuilderFactory
					.create((long) halfLife, wOut, scoreThreshold);

			GraphFormingActionBasedSeedlessGroupRecommender<String,EmailMessage<String>> recommender = new GraphFormingActionBasedSeedlessGroupRecommender<>(
					seedlessRecommenderFactory, graphBuilder);

			for (Double seedThreshold : distanceThresholds) {
				for (Double recommendationThreshold : distanceThresholds) {

					String label = graphBuilder.getName() + ",";
					label += seedThreshold + ","
							+ recommendationThreshold + ",";
					label += getHalfLifeName(halfLife) + "," + wOut
							+ "," + scoreThreshold;
					System.out.println("\t"+label);
					
					Collection<MetricResult> results = collectResults(
							trainMessages, testMessages, seedThreshold,
							recommendationThreshold, recommender);

					resultCollection
							.addResults(label, account, results);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {

		for (ActionsDataSet<String, String, EmailMessage<String>, EmailThread<String, EmailMessage<String>>> dataset : dataSets) {

			ArrayList<Metric> tempMetrics = new ArrayList<>();
			for (BurstyGroupMetricFactory<String, EmailMessage<String>> metricFactory : metricFactories) {
				tempMetrics.add(metricFactory.create());
			}
			String headerPrefix = "graph builder,seed threshold,recommendation threshold,"
					+ "account,half_life,w_out,edge threshold";
			MetricResultCollection<String> resultCollection = new MetricResultCollection<String>(
					headerPrefix, tempMetrics,
					dataset.getBurstyGroupsMetricsFile());

			for (String account : dataset.getAccountIds()) {
				System.out.println(account);

				Collection<EmailMessage<String>> trainMessages = dataset
						.getTrainMessages(account, percentTraining);
				Collection<EmailMessage<String>> testMessages = dataset
						.getTestMessages(account, percentTraining);

				for (SeedlessGroupRecommenderFactory<String> seedlessRecommenderFactory : seedlessRecommenderFactories) {
					for (ActionBasedGraphBuilderFactory<String,EmailMessage<String>> graphBuilderFactory : graphBuilderFactories) {

						if (graphBuilderFactory.takesScoredEdgeWithThreshold()) {
							useGraphBuilderScoredEdges(account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else if (graphBuilderFactory.takesTime()) {
							useGraphBuilderTimeThreshold(account,
									trainMessages, testMessages,
									seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						} else {
							useGraphBuilderNoArgs(account, trainMessages,
									testMessages, seedlessRecommenderFactory,
									graphBuilderFactory, resultCollection);
						}

					}
				}
			}
		}
	}
}
