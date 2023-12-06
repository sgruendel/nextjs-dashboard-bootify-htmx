package com.sgruendel.nextjs_dashboard.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.model.RevenueDTO;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.util.NotFoundException;

@Service
public class RevenueService {

    private final RevenueRepository revenueRepository;

    public RevenueService(final RevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    public List<RevenueDTO> findAll() {
        final List<Revenue> revenues = revenueRepository.findAll(Sort.by("month"));
        return revenues.stream()
                .map(revenue -> mapToDTO(revenue, new RevenueDTO()))
                .toList();
    }

    public RevenueDTO get(final String month) {
        return revenueRepository.findById(month)
                .map(revenue -> mapToDTO(revenue, new RevenueDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public String create(final RevenueDTO revenueDTO) {
        final Revenue revenue = new Revenue();
        mapToEntity(revenueDTO, revenue);
        revenue.setMonth(revenueDTO.getMonth());
        return revenueRepository.save(revenue).getMonth();
    }

    public void update(final String month, final RevenueDTO revenueDTO) {
        final Revenue revenue = revenueRepository.findById(month)
                .orElseThrow(NotFoundException::new);
        mapToEntity(revenueDTO, revenue);
        revenueRepository.save(revenue);
    }

    public void delete(final String month) {
        revenueRepository.deleteById(month);
    }

    private RevenueDTO mapToDTO(final Revenue revenue, final RevenueDTO revenueDTO) {
        revenueDTO.setMonth(revenue.getMonth());
        revenueDTO.setRevenue(revenue.getRevenue());
        return revenueDTO;
    }

    private Revenue mapToEntity(final RevenueDTO revenueDTO, final Revenue revenue) {
        revenue.setRevenue(revenueDTO.getRevenue());
        return revenue;
    }

    public boolean monthExists(final String month) {
        return revenueRepository.existsByMonthIgnoreCase(month);
    }

}
