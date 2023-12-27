package com.sgruendel.nextjs_dashboard.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import com.sgruendel.nextjs_dashboard.domain.Revenue;

@DataMongoTest
@ActiveProfiles("test")
class RevenueRepositoryTest {

    @Autowired
    private RevenueRepository revenueRepository;

    private List<Revenue> revenues;

    @BeforeEach
    public void setupTestData() {
        revenues = new ArrayList<>(12);
        final Revenue revenueJan = new Revenue();
        revenueJan.setMonth("Jan");
        revenueJan.setRevenue(1000);
        revenues.add(revenueJan);

        final Revenue revenueFeb = new Revenue();
        revenueFeb.setMonth("Feb");
        revenueFeb.setRevenue(2000);
        revenues.add(revenueFeb);

        final Revenue revenueMar = new Revenue();
        revenueMar.setMonth("Mar");
        revenueMar.setRevenue(3000);
        revenues.add(revenueMar);

        final Revenue revenueApr = new Revenue();
        revenueApr.setMonth("Apr");
        revenueApr.setRevenue(4000);
        revenues.add(revenueApr);

        final Revenue revenueMay = new Revenue();
        revenueMay.setMonth("May");
        revenueMay.setRevenue(5000);
        revenues.add(revenueMay);

        final Revenue revenueJun = new Revenue();
        revenueJun.setMonth("Jun");
        revenueJun.setRevenue(6000);
        revenues.add(revenueJun);

        final Revenue revenueJul = new Revenue();
        revenueJul.setMonth("Jul");
        revenueJul.setRevenue(7000);
        revenues.add(revenueJul);

        final Revenue revenueAug = new Revenue();
        revenueAug.setMonth("Aug");
        revenueAug.setRevenue(8000);
        revenues.add(revenueAug);

        final Revenue revenueSep = new Revenue();
        revenueSep.setMonth("Sep");
        revenueSep.setRevenue(9000);
        revenues.add(revenueSep);

        final Revenue revenueOct = new Revenue();
        revenueOct.setMonth("Oct");
        revenueOct.setRevenue(10000);
        revenues.add(revenueOct);

        final Revenue revenueNov = new Revenue();
        revenueNov.setMonth("Nov");
        revenueNov.setRevenue(11000);
        revenues.add(revenueNov);

        final Revenue revenueDec = new Revenue();
        revenueDec.setMonth("Dec");
        revenueDec.setRevenue(12000);
        revenues.add(revenueDec);
    }

    @Test
    void testEmptyRepository() {
        assertEquals(0, revenueRepository.count());
        assertEquals(0, revenueRepository.findAll().size());
    }

    @Test
    void testInitializedRepository() {
        revenues.forEach(revenueRepository::save);
        assertEquals(revenues.size(), revenueRepository.count());
        assertEquals(revenues.size(), revenueRepository.findAll().size());
    }

    @Test
    void testFindByMonthIgnoreCase() {
        revenues.forEach(revenueRepository::save);

        assertTrue(revenueRepository.existsByMonthIgnoreCase("jan"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("JAN"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Jan"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("feb"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("FEB"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Feb"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("mar"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("MAR"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Mar"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("apr"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("APR"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Apr"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("may"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("MAY"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("May"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("jun"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("JUN"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Jun"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("jul"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("JUL"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Jul"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("aug"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("AUG"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Aug"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("sep"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("SEP"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Sep"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("oct"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("OCT"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Oct"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Nov"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("NOV"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Nov"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("dec"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("DEC"));
        assertTrue(revenueRepository.existsByMonthIgnoreCase("Dec"));
    }

    @AfterEach
    void tearDown() {
        // no transactions with local MongoDB so we must clean up after each test
        revenueRepository.deleteAll();
    }
}
