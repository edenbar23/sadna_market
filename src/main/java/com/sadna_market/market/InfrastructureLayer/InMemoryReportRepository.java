package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.DomainLayer.IReportRepository;
import com.sadna_market.market.DomainLayer.Report;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Repository
public class InMemoryReportRepository implements IReportRepository {

    private final Logger logger = Logger.getLogger(InMemoryReportRepository.class.getName());
    private final Map<UUID, Report> reports = new ConcurrentHashMap<>();

    public InMemoryReportRepository() {
        logger.info("InMemoryReportRepository initialized");
    }

    @Override
    public boolean save(Report report) {
        if (report == null) {
            logger.severe("Cannot save null report");
            throw new IllegalArgumentException("Report cannot be null");
        }

        logger.info("Saving report: " + report.getReportId());
        reports.put(report.getReportId(), report);
        return true;
    }

    @Override
    public boolean deleteById(UUID reportId) {
        if (reportId == null) {
            logger.severe("Cannot delete report with null ID");
            return false;
        }

        logger.info("Deleting report by ID: " + reportId);
        return reports.remove(reportId) != null;
    }

    @Override
    public boolean update(Report report) {
        if (report == null) {
            logger.severe("Cannot update null report");
            throw new IllegalArgumentException("Report cannot be null");
        }

        logger.info("Updating report: " + report.getReportId());
        reports.put(report.getReportId(), report);
        return true;
    }

    @Override
    public List<Report> getAllReports() {
        logger.info("Getting all reports");
        return List.copyOf(reports.values());
    }

    @Override
    public Optional<Report> findById(UUID reportId) {
        if (reportId == null) {
            logger.severe("Cannot find report with null ID");
            return Optional.empty();
        }

        logger.info("Finding report by ID: " + reportId);
        return Optional.ofNullable(reports.get(reportId));
    }

    @Override
    public List<Report> findBySender(String username) {
        if (username == null || username.isEmpty()) {
            logger.severe("Cannot find reports with null or empty username");
            return List.of();
        }

        logger.info("Finding reports sent by user: " + username);

        return reports.values().stream()
                .filter(report -> username.equals(report.getUsername()))
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        reports.clear();
        logger.info("Report repository cleared");
    }
}