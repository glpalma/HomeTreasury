import type { BillMonthStatus } from "../api/bills";
import type { IncomeStatusResponse } from "../api/incomes";

export interface Coverage {
  /** Sum of the expected amounts of the bills that apply this month. */
  billsExpectedTotal: number;
  /** Sum of the expected amounts of the incomes that apply this month. */
  incomesExpectedTotal: number;
  /** Money found for those incomes so far. */
  incomesReceivedTotal: number;
  /** False when the money received so far is less than the bills' expected total. */
  covers: boolean;
  /** How much more is needed to cover the bills; 0 when it covers. */
  shortBy: number;
}

const toCents = (value: number) => Math.round(value * 100);
const fromCents = (cents: number) => cents / 100;

/** Pure: compares in integer cents so rounding noise never flips the verdict. */
export function computeCoverage(bills: BillMonthStatus[], incomes: IncomeStatusResponse): Coverage {
  const billsCents = bills.reduce((sum, bill) => sum + toCents(bill.expectedAmount), 0);
  const receivedCents = toCents(incomes.receivedTotal);
  const shortCents = Math.max(0, billsCents - receivedCents);

  return {
    billsExpectedTotal: fromCents(billsCents),
    incomesExpectedTotal: incomes.expectedTotal,
    incomesReceivedTotal: incomes.receivedTotal,
    covers: shortCents === 0,
    shortBy: fromCents(shortCents),
  };
}
