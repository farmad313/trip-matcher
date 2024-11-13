# trip-matcher
Matching tap-on and tap-off events to create trips


#### ✔️ Prerequisites
- Java 21
- Maven

#### ✔️ Build and Run
- Clone the repository
```shell
git clone  https://github.com/farmad313/trip-matcher.git
```
- Run the following command to build the project
```shell
mvnw clean install
```



---

## Software Design and Architecture Overview

#### ✔️ Where and how to store trip fares?
As our hypothetical system is not going to save the price rules securely, trip fares were stored in a simple java data structure (A map with composite key). At production level, a configuration or rule system should be used to store them in database and a distributed cache should be placed between database and services.

#### ✔️ Calculate in stream processing time or beforehand?
All the trip fare rules were stored as a base data, including:
1.	Completed trips’ fare for both directions.
2.	Cancelled trips: zero
3.	Incomplete trips: the highest fare from a specific source.
      For calculating trips in both direction and highest fare from a specific source, a new service should be designed to persist/update data received by transit companies. In this way, we calculate once and read forever.

#### ✔️ How to read and write from csv file?
As the focus of our hypothetical system is on taps to trips conversion, I simply read and write to and from csv file. At production level, as we are processing periodically big data files we should use Spring Batch to enjoy its built-in features for handling large volumes of data, such as chunk-based processing, which breaks down data into manageable chunks for processing.
For naming the modules in this hypothetical system, Spring Batch naming and terms such as itemReader, itemProcessor and ItemWriter has been used.


#### ✔️ How to improve stream processing time?
1. Parallel stream processing: It improves performance especially when facing huge amount of data while when operations depends on the order of execution or involve state add complexity so we should be careful.
2. Virtual thread: By providing lightweight mechanism to execute parallel task without overhead of traditional thread pools it can enhance performance.


#### ✔️ What is the best Data type for trip fare?
1. BigDecimal: Use BigDecimal for prices and volumes in financial applications to ensure precise and accurate calculations.
2. Double: double might be faster and use less memory, but it is prone to rounding errors and not suitable for financial precision.

#### ✔️ Highlevel design of tap matcher processor?
1. Read tap-on and tap-off events from csv file.
2. Store tap events in a List.
3. Group tap events by CompanyId, BusID, PAN.
4. Sort tap events by time.
5. Detect type of trip (completed, cancelled, incomplete) in each group by checking two back-to-back tap events.
6. Calculate fare for each trip.
7. Calculate trip duration.
8. Store trip data in a List.
9. Write trip data to csv file.