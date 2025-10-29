# Clojure Tasks:

## C1
Given an alphabet as a list of 1-character strings and a number `N`. Define a function that returns all possible strings of length `N` composed from this alphabet **with no equal adjacent characters**.  
Constraints: use only `map`/`reduce`/`filter`/`remove` and basic list operations (`str`, `cons`, `concat`, etc.). **Do not** use recursion, generators, or `flatten`.  
Example: for the alphabet `("a" "b" "c")` and `N = 2`, the result (up to permutation) is `("ab" "ac" "ba" "bc" "ca" "cb")`.

## C2
Define an **infinite** sequence of prime numbers using a **Sieve of Eratosthenes** implementation in a lazy, unbounded form. Cover the solution with unit tests.

## C3
Implement a **parallel** variant of `filter` using `future`. Each `future` must process a **block** of elements, not single elements. The input sequence may be finite or infinite, so the implementation must preserve **laziness** and provide performance improvements via parallelism. Cover the solution with unit tests and demonstrate efficiency with timing measurements.

## C4
Implement a simulation of a production line for safes and clocks. Clocks require 5 units of lumber and 10 units of gears; gears come from iron ore, and so on. Most of the code (including the production configuration) is provided; complete the missing parts explicitly marked in the code.  
Use **agents** for processors (factories) and **atoms + agents** for storages:
- A factory’s agent notifies the storage’s agent when a resource is produced.
- The storage’s agent updates the storage’s atom and notifies all relevant processors.
Implement the **processor notification message function** as specified in the provided code.  
There is a **metal shortage**; ensure both final products (clocks and safes) are still produced (at a reduced rate).

## C5
Solve the **dining philosophers** problem using **Clojure STM**. Represent each fork as a `ref` with a counter tracking how many times the fork has been used.  
Make the number of philosophers, lengths of thinking/eating periods, and number of cycles configurable.  
Measure efficiency by overall execution time and number of transaction restarts (e.g., via an `atom` counter).  
Experiment with even/odd counts of philosophers and try to provoke a **live-lock** scenario.

## C6
Given a map of air routes with limited ticket counts and per-route ticket prices, simulate **concurrent ticket booking**. Customers typically need multi-leg trips; they want to **atomically** book all required legs or be notified of failure. Use **STM transactions** to ensure atomicity.  
Prefer **lower total price** even if it requires more transfers.  
Complete the provided skeleton (fill in missing parts), measure efficiency in terms of transaction (re)starts, and tune timeouts to satisfy as many customers as possible.
