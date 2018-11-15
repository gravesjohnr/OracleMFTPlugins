echo "Comple CallRestPre"
javac -cp .:/u01/app/oracle/middleware/mft/modules/oracle.mft/oracle.mft.jar:/u01/app/oracle/middleware/oracle_common/modules/clients/com.oracle.jersey.fmw.client.jar com/oracle/callout/sample/CallRestPre.java
echo "Comple CallRestPost"
javac -cp .:/u01/app/oracle/middleware/mft/modules/oracle.mft/oracle.mft.jar:/u01/app/oracle/middleware/oracle_common/modules/clients/com.oracle.jersey.fmw.client.jar com/oracle/callout/sample/CallRestPost.java
jar cvf CallRest.jar com resources
