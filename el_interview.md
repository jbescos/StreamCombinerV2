# Stream Combiner

## Description

*Stream Combiner* is an application that allows you to create a 
*combined stream*. This stream combines / merges entries from all (original) 
individual streams. 

## Requirements
1. Application should be configurable to read data from N hosts:ports.
2. Input streams provide data in XML format.
  * Data in input stream could be veeery LARGE.
  * Data in input stream is sorted by timestamp.
3. Application output is JSON stream.
4. Output data should be sorted by timestamp.
5. If several inputs provide data with the same timestamp - amounts 
should be merged.
6. Amounts could be positive/negative. And it's very sensitive data - like money.
And nobody likes losing money.
7. For extra bonus points: 
  * add solution in case if some input streams hang.
  * imagine that timestamps comparing operation is VERY expensive - try to 
  minimize it's usage.  
  
## Implementation
1. You can use [Eclipselink](https://www.eclipse.org/eclipselink/ "Eclipselink") library and its dependencies for your application.
2. Project lifecycyle:
  * You must use either [Maven 3.x](http://maven.apache.org/ "Maven") or [Gradle 2.x](http://www.gradle.org/ "Gradle") to build the project.
  * You must use [JUnit](http://junit.org/ "JUnit") to write tests for the project. Use of any JUnit extensions is allowed.
3. Definition of XML format:<br/>
  `<data> <timestamp>123456789</timeStamp> <amount>1234.567890</amount> </data>`
4. XMLs in stream are separated by new line (`\n`)
5. Definition of JSON format:<br/> 
  `{ "data": { "timestamp":123456789, "amount":"1234.567890" }}`

  