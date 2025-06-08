package com.sadna_market.market.InfrastructureLayer.Adapters;


import com.sadna_market.market.DomainLayer.IReportRepository;
import com.sadna_market.market.DomainLayer.Report;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.ReportJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile({"dev", "prod", "default"})
public class ReportJpaAdapter implements IReportRepository {

    @Autowired
    private ReportJpaRepository reportJpaRepository;

    @Override
    public boolean save(Report report) {
        try {
            reportJpaRepository.save(report);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteById(UUID reportId) {
        try {
            if (reportJpaRepository.existsById(reportId)) {
                reportJpaRepository.deleteById(reportId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean update(Report report) {
        try {
            if (reportJpaRepository.existsById(report.getReportId())) {
                reportJpaRepository.save(report);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<Report> findById(UUID reportId) {
        return reportJpaRepository.findById(reportId);
    }

    @Override
    public List<Report> findBySender(String username) {
        return reportJpaRepository.findByUsername(username);
    }

    @Override
    public List<Report> getAllReports() {
        return reportJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void clear() {
        reportJpaRepository.deleteAll();
    }

    @Override
    public int countPendingReports() {
        // Since Report entity doesn't have status field yet,
        // return total count (assuming all are "pending")
        return Math.toIntExact(reportJpaRepository.count());
    }

    @Override
    public List<Report> findAll() {
        return reportJpaRepository.findAll();
    }

}