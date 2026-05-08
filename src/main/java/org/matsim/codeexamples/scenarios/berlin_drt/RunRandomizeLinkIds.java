package org.matsim.codeexamples.scenarios.berlin_drt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RunRandomizeLinkIds {

    public static void main(String[] args) {
        String networkFile = "scenarios/berlin_drt/berlin-mitte_network.xml";
        String plansFile = "scenarios/berlin_drt/berlin-mitte_drt_10_passengers_plans.xml";

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(networkFile);
        config.plans().setInputFile(plansFile);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();

        List<Id<Link>> linkIds = new ArrayList<>(network.getLinks().keySet());
        Random random = new Random(42); // Use a seed for reproducibility

        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        // Avoid interactions like pt interactions if they exist, but generally it's safe to set linkId for all
                        // In DRT there might be "drt interaction" activities but randomly placing them is fine if that's what's asked.
                        Id<Link> randomLinkId = linkIds.get(random.nextInt(linkIds.size()));
                        activity.setLinkId(randomLinkId);
                    }
                }
            }
        }

        new PopulationWriter(population).write(plansFile);
        System.out.println("Finished randomizing link ids in " + plansFile);
    }
}
