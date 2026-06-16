package org.SportsIn.repository;

import org.SportsIn.model.territory.ZoneDepartement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneDepartementRepository extends JpaRepository<ZoneDepartement, String> {

    @Query("SELECT z FROM ZoneDepartement z ORDER BY z.totalInfluence DESC")
    List<ZoneDepartement> findAllOrderByInfluenceDesc();
}
