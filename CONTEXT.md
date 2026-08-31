# HomeTreasury

A household finance API that aggregates real bank balances and transactions for a shared home, with role-based access control for its members.

## Language

**Home**:
The household unit — the central entity that members belong to and that holds the financial goal.
_Avoid_: House, family, group

**User**:
A person who can be a member of a Home, authenticated by email and password.
_Avoid_: AppUser, account, profile

**Membership**:
The association between a User and a Home, carrying the User's Role within that Home.
_Avoid_: HomeMembership, participation

**Role**:
The permission level a User holds in a Home. Either `OWNER` or `VIEWER`.
_Avoid_: Permission, access level

**Owner**:
A User with the `OWNER` Role. The only Role that can change the Ideal Balance.
_Avoid_: Admin, manager

**Viewer**:
A User with the `VIEWER` Role. Can read financial data but cannot modify the Home.
_Avoid_: Guest, reader

**Ideal Balance**:
The monetary target set by an Owner for the Home — the balance the household is aiming to maintain or reach.
_Avoid_: Goal, target, budget

**Account**:
A bank account retrieved from Pluggy, identified by the Item. Holds a current Balance.
_Avoid_: Bank account (redundant in context)

**Balance**:
The current monetary amount held in an Account at the time of the last Pluggy sync.
_Avoid_: Saldo (Portuguese alias used in endpoint names)

**Statement**:
The list of Transactions for a given Account over a period.
_Avoid_: Extrato (Portuguese alias used in endpoint names), history, ledger

**Transaction**:
A single financial movement — credit or debit — recorded on an Account.
_Avoid_: Operation, entry, movement

**Item**:
A Pluggy concept representing a connected bank credential. An Item maps to one or more Accounts at a financial institution.
_Avoid_: Connection, integration, bank link

**Pluggy**:
The external Open Finance service used to fetch Accounts and Statements. Not a domain concept owned by this system — a data provider.
_Avoid_: Open Finance (too generic), bank API
