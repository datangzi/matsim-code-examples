package org.matsim.codeexamples.extensions.dvrp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.common.zones.systems.grid.square.SquareGridZoneSystemParams;
import org.matsim.contrib.drt.optimizer.insertion.extensive.ExtensiveInsertionSearchParams;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.SnapshotStyle;
import org.matsim.core.config.groups.ReplanningConfigGroup.StrategySettings;
import org.matsim.core.config.groups.ScoringConfigGroup.ModeParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

class RunBerlinMitteDrt {
	// todo:
	// * have at least one drt use case in the "examples" project, so it can be
	// addressed via ExamplesUtils
	// * remove the DrtRoute.class thing; use Attributable instead (Route will have
	// to be made implement Attributable). If impossible, move the DrtRoute
	// class thing to the core.
	// * move consistency checkers into the corresponding config groups.
	// * make MultiModeDrt and normal DRT the same. Make config accordingly so that
	// 1-mode drt is just multi-mode with one entry.

	private static final Logger log = LogManager.getLogger(RunBerlinMitteDrt.class);
	private static final String DRT_A = "drt_A";
	private static final String DRT_B = "drt_B";
	// private static final String DRT_C = "drt_C";

	public static void main(String... args) {
		run(args);
	}

	public static void run(String... args) {
		Config config;
		if (args != null && args.length >= 1) {

			config = ConfigUtils.loadConfig(args);

		} else {

			config = ConfigUtils.loadConfig("./scenarios/berlin_drt/berlin-mitte_drt_config.xml");
			config.controller().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		}

		config.controller().setLastIteration(1);

		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);
		config.qsim().setSnapshotStyle(SnapshotStyle.queue);

		@SuppressWarnings("unused")
		DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule(config, DvrpConfigGroup.class);
		dvrpConfig.getTravelTimeMatrixParams().addParameterSet(new SquareGridZoneSystemParams());
		// (config group needs to be "materialized")

		MultiModeDrtConfigGroup multiModeDrtCfg = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);
		{
			DrtConfigGroup drtConfig = new DrtConfigGroup();
			drtConfig.setMode(DRT_A);
			drtConfig.setStopDuration(60.);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxWaitTime(900);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxTravelTimeAlpha(1.3);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxTravelTimeBeta(10. * 60.);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setRejectRequestIfMaxWaitOrTravelTimeViolated(false);
			drtConfig.setVehiclesFile("berlin-mitte_drt_taxi_A.xml");
			drtConfig.setChangeStartLinkToLastLinkInSchedule(true);
			drtConfig.setDrtInsertionSearchParams(new ExtensiveInsertionSearchParams());
			multiModeDrtCfg.addDrtConfigGroup(drtConfig);
		}
		{
			DrtConfigGroup drtConfig = new DrtConfigGroup();
			drtConfig.setMode(DRT_B);
			drtConfig.setStopDuration(60.);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxWaitTime(900);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxTravelTimeAlpha(1.3);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setMaxTravelTimeBeta(10. * 60.);
			drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
					.setRejectRequestIfMaxWaitOrTravelTimeViolated(false);
			drtConfig.setVehiclesFile("berlin-mitte_drt_taxi_B.xml");
			drtConfig.setChangeStartLinkToLastLinkInSchedule(true);
			drtConfig.setDrtInsertionSearchParams(new ExtensiveInsertionSearchParams());
			multiModeDrtCfg.addDrtConfigGroup(drtConfig);
		}
		// {
		// DrtConfigGroup drtConfig = new DrtConfigGroup();
		// drtConfig.setMode(DRT_C);
		// drtConfig.setStopDuration(60.);
		// drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
		// .setMaxWaitTime(900);
		// drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
		// .setMaxTravelTimeAlpha(1.3);
		// drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
		// .setMaxTravelTimeBeta(10. * 60.);
		// drtConfig.addOrGetDrtOptimizationConstraintsParams().addOrGetDefaultDrtOptimizationConstraintsSet()
		// .setRejectRequestIfMaxWaitOrTravelTimeViolated(false);
		// drtConfig.setVehiclesFile("drt_taxi_C.xml");
		// drtConfig.setChangeStartLinkToLastLinkInSchedule(true);
		// drtConfig.setDrtInsertionSearchParams(new ExtensiveInsertionSearchParams());
		// multiModeDrtCfg.addDrtConfigGroup(drtConfig);
		// }

		for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
			DrtConfigs.adjustDrtConfig(drtCfg, config.scoring(), config.routing());
		}
		{
			// add params so that scoring works:
			config.scoring().addModeParams(new ModeParams(DRT_A));
			config.scoring().addModeParams(new ModeParams(DRT_B));
			// config.scoring().addModeParams(new ModeParams(DRT_C));
		}
		{
			// clear strategy settings from config file:
			config.replanning().clearStrategySettings();

			// configure mode innovation so that travellers start using drt:
			config.replanning().addStrategySettings(
					new StrategySettings().setStrategyName(DefaultStrategy.ChangeSingleTripMode).setWeight(0.1));
			// config.changeMode().setModes(new String[] { TransportMode.car, DRT_A, DRT_B,
			// DRT_C });
			config.changeMode().setModes(new String[] { TransportMode.car, DRT_A, DRT_B });

			// have a "normal" plans choice strategy:
			config.replanning().addStrategySettings(
					new StrategySettings().setStrategyName(DefaultSelector.ChangeExpBeta).setWeight(1.));
		}

		// ===
		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DrtRoute.class,
				new DrtRouteFactory());
		ScenarioUtils.loadScenario(scenario);
		new NetworkCleaner().run(scenario.getNetwork());

		// Snap activities to the cleaned network
		for (org.matsim.api.core.v01.population.Person p : scenario.getPopulation().getPersons().values()) {
			for (org.matsim.api.core.v01.population.Plan plan : p.getPlans()) {
				for (org.matsim.api.core.v01.population.PlanElement pe : plan.getPlanElements()) {
					if (pe instanceof org.matsim.api.core.v01.population.Activity) {
						org.matsim.api.core.v01.population.Activity act = (org.matsim.api.core.v01.population.Activity) pe;
						if (!scenario.getNetwork().getLinks().containsKey(act.getLinkId())) {
							if (act.getCoord() != null) {
								act.setLinkId(org.matsim.core.network.NetworkUtils
										.getNearestLink(scenario.getNetwork(), act.getCoord()).getId());
							}
						}
					}
				}
			}
		}

		// ===
		Controler controler = new Controler(scenario);

		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new MultiModeDrtModule());

		// controler.configureQSimComponents(DvrpQSimComponents.activateModes(DRT_A,
		// DRT_B, DRT_C));
		controler.configureQSimComponents(DvrpQSimComponents.activateModes(DRT_A, DRT_B));

		controler.run();
	}

}
