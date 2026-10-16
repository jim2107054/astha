package com.astha.simulator.service;

import com.astha.simulator.domain.GroundTruthRecord;
import com.astha.simulator.domain.SyntheticEntity;
import com.astha.simulator.repository.GroundTruthRepository;
import com.astha.simulator.repository.SyntheticEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SimulatorService {

    private static final Logger log = LoggerFactory.getLogger(SimulatorService.class);

    private final SyntheticEntityRepository entityRepository;
    private final GroundTruthRepository groundTruthRepository;

    private static final String[] FIRST_NAMES = {
            "Anisur", "Fatema", "Tanvir", "Shirin", "Kamal", "Nasrin", "Mustafa", "Rokeya", "Zahid", "Suraiya"
    };
    private static final String[] LAST_NAMES = {
            "Rahman", "Begum", "Ahmed", "Akter", "Hossain", "Chowdhury", "Islam", "Khatun", "Uddin", "Sarker"
    };
    private static final String[] DISTRICTS = {
            "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh", "Bogura", "Comilla"
    };

    public SimulatorService(SyntheticEntityRepository entityRepository,
                            GroundTruthRepository groundTruthRepository) {
        this.entityRepository = entityRepository;
        this.groundTruthRepository = groundTruthRepository;
    }

    @Transactional
    public List<SyntheticEntity> resetWithSeed(long seed) {
        entityRepository.deleteAll();
        groundTruthRepository.deleteAll();

        Random random = new Random(seed);
        List<SyntheticEntity> population = new ArrayList<>();

        // Generate 30 Customers
        for (int i = 1; i <= 30; i++) {
            String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String msisdn = String.format("+88017%08d", 10000000 + i);
            String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
            String tier = i % 3 == 0 ? "TIER_2" : (i % 7 == 0 ? "TIER_3" : "TIER_1");
            population.add(new SyntheticEntity("CUST-" + i, "CUSTOMER", name, msisdn, district, tier, "SALARIED"));
        }

        // Generate 8 Agents
        for (int i = 1; i <= 8; i++) {
            String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " Enterprise";
            String msisdn = String.format("+88018%08d", 20000000 + i);
            String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
            population.add(new SyntheticEntity("AGENT-" + i, "AGENT", name, msisdn, district, "TIER_3", "BUSY_AGENT"));
        }

        // Generate 6 Merchants
        for (int i = 1; i <= 6; i++) {
            String name = LAST_NAMES[random.nextInt(LAST_NAMES.length)] + " Store";
            String msisdn = String.format("+88019%08d", 30000000 + i);
            String district = DISTRICTS[random.nextInt(DISTRICTS.length)];
            population.add(new SyntheticEntity("MERCHANT-" + i, "MERCHANT", name, msisdn, district, "TIER_3", "GROCERY"));
        }

        return entityRepository.saveAll(population);
    }

    @Transactional
    public GroundTruthRecord injectTypology(String typology, String targetEntityId) {
        String entityId = targetEntityId != null ? targetEntityId : "CUST-1";
        String ringId = "RING-" + UUID.randomUUID().toString().substring(0, 6);
        String label = "HARD_NEGATIVE".equalsIgnoreCase(typology) ? "HARD_NEGATIVE" : "FRAUD";

        GroundTruthRecord record = new GroundTruthRecord(
                UUID.randomUUID().toString(),
                entityId,
                typology,
                ringId,
                label
        );

        return groundTruthRepository.save(record);
    }

    public List<GroundTruthRecord> getGroundTruth() {
        return groundTruthRepository.findAll();
    }

    public List<SyntheticEntity> getPopulation() {
        return entityRepository.findAll();
    }
}
