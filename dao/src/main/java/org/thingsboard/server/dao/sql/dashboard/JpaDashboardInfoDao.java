package org.thingsboard.server.dao.sql.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.DashboardInfo;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.page.SortOrder;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.dashboard.DashboardInfoDao;
import org.thingsboard.server.dao.model.sql.DashboardInfoEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Valerii Sosliuk on 5/6/2017.
 */
@Slf4j
@Component
public class JpaDashboardInfoDao extends JpaAbstractSearchTextDao<DashboardInfoEntity, DashboardInfo> implements DashboardInfoDao {

    @Autowired
    private DashboardInfoRepository dashboardInfoRepository;

    @Override
    protected Class<DashboardInfoEntity> getEntityClass() {
        return DashboardInfoEntity.class;
    }

    @Override
    protected JpaRepository<DashboardInfoEntity, UUID> getRepository() {
        return dashboardInfoRepository;
    }

    @Override
    public PageData<DashboardInfo> findDashboardsByTenantId(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(dashboardInfoRepository
                .findByTenantId(
                        tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<DashboardInfo> findMobileDashboardsByTenantId(UUID tenantId, PageLink pageLink) {
        List<SortOrder> sortOrders = new ArrayList<>();
        sortOrders.add(new SortOrder("mobileOrder", SortOrder.Direction.ASC));
        if (pageLink.getSortOrder() != null) {
            sortOrders.add(pageLink.getSortOrder());
        }
        return DaoUtil.toPageData(dashboardInfoRepository
                .findMobileByTenantId(
                        tenantId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink, sortOrders)));
    }

    @Override
    public PageData<DashboardInfo> findDashboardsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, PageLink pageLink) {
        return DaoUtil.toPageData(dashboardInfoRepository
                .findByTenantIdAndCustomerId(
                        tenantId,
                        customerId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public PageData<DashboardInfo> findMobileDashboardsByTenantIdAndCustomerId(UUID tenantId, UUID customerId, PageLink pageLink) {
        List<SortOrder> sortOrders = new ArrayList<>();
        sortOrders.add(new SortOrder("mobileOrder", SortOrder.Direction.ASC));
        if (pageLink.getSortOrder() != null) {
            sortOrders.add(pageLink.getSortOrder());
        }
        return DaoUtil.toPageData(dashboardInfoRepository
                .findMobileByTenantIdAndCustomerId(
                        tenantId,
                        customerId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink, sortOrders)));
    }

    @Override
    public PageData<DashboardInfo> findDashboardsByTenantIdAndEdgeId(UUID tenantId, UUID edgeId, PageLink pageLink) {
        log.debug("Try to find dashboards by tenantId [{}], edgeId [{}] and pageLink [{}]", tenantId, edgeId, pageLink);
        return DaoUtil.toPageData(dashboardInfoRepository
                .findByTenantIdAndEdgeId(
                        tenantId,
                        edgeId,
                        Objects.toString(pageLink.getTextSearch(), ""),
                        DaoUtil.toPageable(pageLink)));
    }

    @Override
    public DashboardInfo findFirstByTenantIdAndName(UUID tenantId, String name) {
        return DaoUtil.getData(dashboardInfoRepository.findFirstByTenantIdAndTitle(tenantId, name));
    }
}
