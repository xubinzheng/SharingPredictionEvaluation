package metrics.groups.evolution;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import metrics.DoubleResult;
import metrics.MetricResult;
import recommendation.groups.evolution.recommendations.RecommendedEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupChangeEvolution;
import recommendation.groups.evolution.recommendations.RecommendedGroupCreationEvolution;

public class ManualAdditionsToCreateGroups<V> extends GroupEvolutionMetric<V> {

	@Override
	public String getHeader() {
		return "manual additions to create groups";
	}

	@Override
	public MetricResult evaluate(Set<V> newMembers, Map<Set<V>, Collection<Set<V>>> oldToNewIdealGroups,
			Collection<Set<V>> newlyCreatedIdealGroups,
			Map<RecommendedGroupChangeEvolution<V>, Set<V>> groupChangeToIdeal,
			Map<RecommendedGroupCreationEvolution<V>, Set<V>> groupCreationToIdeal,
			Collection<RecommendedEvolution<V>> unusedRecommendations,
			Collection<Set<V>> unusedIdeals) {
		
		int additions = 0;
		for (Set<V> newlyCreatedIdeal : newlyCreatedIdealGroups) {
			additions += newlyCreatedIdeal.size();
		}

		return new DoubleResult(additions);
	}

}
