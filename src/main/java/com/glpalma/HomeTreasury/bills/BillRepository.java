package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.home.Home;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByHomeOrderByDueDayAscNameAsc(Home home);

    Optional<Bill> findByIdAndHome(Long id, Home home);
}
