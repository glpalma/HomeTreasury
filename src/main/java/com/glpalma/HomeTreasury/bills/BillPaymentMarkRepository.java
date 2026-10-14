package com.glpalma.HomeTreasury.bills;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BillPaymentMarkRepository extends JpaRepository<BillPaymentMark, Long> {

    Optional<BillPaymentMark> findByBillAndMonth(Bill bill, YearMonth month);

    List<BillPaymentMark> findByBillInAndMonth(Collection<Bill> bills, YearMonth month);
}
