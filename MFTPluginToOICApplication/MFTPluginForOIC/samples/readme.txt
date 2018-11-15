To deploy:

Copy jar and CallOIC*.xml files to the mft/callout directory
e.g.
cp CallOIC*.xml /u01/soacs/dbfs/share/mft/callout/
cp Call*.jar /u01/soacs/dbfs/share/mft/callout/

Modify and run the example deploy.wlst script.
e.g.
/u01/app/oracle/middleware/mft/common/bin/wlst.sh deploy.wlst

Create directory off the mft domain home called oic
e.g.
mkdir /u01/data/domains/MFT_domain/oic

Modify and copy the oic.properties file based on the sample.oic.properties
cp sample.oic.properties /u01/data/domains/MFT_domain/oic/oic.properties