package com.glpalma.HomeTreasury.incomes;

import com.glpalma.HomeTreasury.home.Home;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpectedIncomeRepository extends JpaRepository<ExpectedIncome, Long> {

    List<ExpectedIncome> findByHomeOrderByExpectedDayAscNameAsc(Home home);
}
