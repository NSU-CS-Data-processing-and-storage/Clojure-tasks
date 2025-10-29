# Clojure Tasks: C1, C2, C3

## C1

Given an alphabet as a list of 1-character strings and a number `N`. Define a function that returns all possible strings of length `N` composed from this alphabet **with no equal adjacent characters**.
Constraints: use only `map`/`reduce`/`filter`/`remove` and basic list operations (`str`, `cons`, `concat`, etc.). **Do not** use recursion, generators, or `flatten`.
Example: for the alphabet `("a" "b" "c")` and `N = 2`, the result (up to permutation) is `("ab" "ac" "ba" "bc" "ca" "cb")`.

## C2

Define an **infinite** sequence of prime numbers using a **Sieve of Eratosthenes** implementation in a lazy, unbounded form. Cover the solution with unit tests.

## C3

Implement a **parallel** variant of `filter` using `future`. Each `future` must process a **block** of elements, not single elements. The input sequence may be finite or infinite, so the implementation must preserve **laziness** and provide performance improvements via parallelism. Cover the solution with unit tests and demonstrate efficiency with timing measurements.
