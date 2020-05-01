# tcp-client-tools

## Usage example
### IntegerClient   
```
mvn exec:java -Dexec.mainClass="fr.tcp.client.impl.IntegerClient" -Dexec.args="-h localhost -p 7777 -n 10 -t 1 -l 2"  
java -jar client-integer.jar -h localhost -p 7777 -n 10 -t 1 -l 2    
```   

### StringClient  
```   
mvn exec:java -Dexec.mainClass="fr.tcp.client.impl.StringClient" -Dexec.args="-h localhost -p 7777 -n 10 -t 1 -l 2"     
java -jar client-string.jar -h localhost -p 7777 -n 10 -t 1 -l 2    
```   