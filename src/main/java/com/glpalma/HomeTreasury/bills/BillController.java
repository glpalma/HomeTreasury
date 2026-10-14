package com.glpalma.HomeTreasury.bills;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.glpalma.HomeTreasury.home.Home;
import com.glpalma.HomeTreasury.user.CurrentMembershipService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final CurrentMembershipService memberships;
    private final BillRepository bills;
    private final BillPaymentMarkRepository marks;
    private final BillStatusService billStatus;
    private final BillPreviewService billPreview;

    public BillController(
            CurrentMembershipService memberships,
            BillRepository bills,
            BillPaymentMarkRepository marks,
            BillStatusService billStatus,
            BillPreviewService billPreview
    ) {
        this.memberships = memberships;
        this.bills = bills;
        this.marks = marks;
        this.billStatus = billStatus;
        this.billPreview = billPreview;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillResponse create(
            @Valid @RequestBody CreateBillRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        YearMonth startMonth = request.startMonth() == null ? HomeClock.currentMonth() : request.startMonth();
        Bill saved = bills.save(new Bill(
                home,
                request.name().trim(),
                request.expectedAmount(),
                request.dueDay(),
                request.keyword().trim(),
                startMonth
        ));
        return BillResponse.from(saved);
    }

    @GetMapping
    public List<BillResponse> list(Authentication authentication) {
        Home home = memberships.require(authentication).home();
        return bills.findByHomeOrderByDueDayAscNameAsc(home).stream()
                .map(BillResponse::from)
                .toList();
    }

    @GetMapping("/status")
    public BillStatusResponse status(
            @RequestParam YearMonth month,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        return billStatus.statusFor(home, month);
    }

    @GetMapping("/preview")
    public BillPreviewResponse preview(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "3") int months,
            Authentication authentication
    ) {
        if (keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword must not be blank");
        }
        Home home = memberships.require(authentication).home();
        return billPreview.preview(home, keyword.trim(), months);
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{billId}")
    public BillResponse update(
            @PathVariable Long billId,
            @Valid @RequestBody UpdateBillRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        Bill bill = findBill(billId, home);
        if (bill.getEndMonth() != null && request.startMonth().isAfter(bill.getEndMonth())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "startMonth must not be after the month the bill was archived");
        }
        bill.update(
                request.name().trim(),
                request.expectedAmount(),
                request.dueDay(),
                request.keyword().trim(),
                request.startMonth()
        );
        return BillResponse.from(bills.save(bill));
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{billId}/archive")
    public BillResponse archive(@PathVariable Long billId, Authentication authentication) {
        Home home = memberships.require(authentication).home();
        Bill bill = findBill(billId, home);
        YearMonth thisMonth = HomeClock.currentMonth();
        if (bill.getEndMonth() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bill is already archived");
        }
        if (thisMonth.isBefore(bill.getStartMonth())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Bill has not started yet; change its start month instead");
        }
        bill.archive(thisMonth);
        return BillResponse.from(bills.save(bill));
    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{billId}/months/{month}/paid")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPaid(
            @PathVariable Long billId,
            @PathVariable YearMonth month,
            @RequestBody MarkPaidRequest request,
            Authentication authentication
    ) {
        Home home = memberships.require(authentication).home();
        Bill bill = findBill(billId, home);

        var existing = marks.findByBillAndMonth(bill, month);
        if (request.paid() && existing.isEmpty()) {
            marks.save(new BillPaymentMark(bill, month, Instant.now()));
        } else if (!request.paid() && existing.isPresent()) {
            marks.delete(existing.get());
        }
    }

    private Bill findBill(Long billId, Home home) {
        return bills.findByIdAndHome(billId, home)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
    }
}
