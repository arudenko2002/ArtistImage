package ArtistImageStudy;

import com.google.cloud.bigquery.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RunSQL {
    private BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    String project="umg-dev";
    String runner="DirectRunner";
    int verbocity=1;
    ReadHTTP rhp = null; //new ReadHTTP(0);
    public RunSQL(int verbocity) {
        this.verbocity = verbocity;
        rhp=new ReadHTTP(verbocity);
    }
    public String getSQL2(String canopus) {
        String sql="select isrc\n" +
                "from `umg-swift.metadata.product`\n" +
                //"where master_artist_id=10025095\n" +
                //"where master_artist_id=10043015\n" +
                //"where master_artist_id="+canopus+"\n" +
                "where release_artist_id="+canopus+"\n" +
                "AND LENGTH(isrc)>0\n" +
                "GROUP BY 1\n" +
                "limit 100000";
        return sql;
    }

    public String getSQL(String canopus) {
        String sql="select isrc FROM  `umg-dev.swift_alerts.canopus_resource` where canopus_id="+canopus+" group by 1 limit 1000";
        return sql;
    }
    public Boolean runSQL(String query) throws Exception {
        if(verbocity>0)
            System.out.println(query);
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(query)
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        // Wait for the query to complete.
        queryJob = queryJob.waitFor();

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        // Get the results.
        QueryResponse response = bigquery.getQueryResults(jobId);
        QueryResult qresult = response.getResult();

        // Print all pages of the results.
        ArrayList<String> output = new ArrayList<String>();
        String result = "";
        int counter=0;
        while (qresult != null) {
            for (List<FieldValue> row : qresult.iterateAll()) {
                String isrc = row.get(0).getStringValue();
                //String fn1 = row.get(1).toString();
                //long fv1 = row.get(1).getLongValue();
                //System.out.println( "isrc="+isrc);

                if(verbocity>0)
                    System.out.println("ISRC="+counter+"      "+isrc);
                counter++;
                rhp.search_isrc(isrc);
            }
            qresult = qresult.getNextPage();
        }
        return false;
    }

    public void followAviciiProblemList() throws Exception {
        ArrayList<String> ar = new ArrayList<String>();
        ar.add("CH3131400001");
        ar.add("CH3131400001");
        ar.add("USWD11156381");
        ar.add("CH3131340013");
        ar.add("GB28K1100047");
        for(int i=0; i< ar.size();i++) {
            rhp.search_isrc(ar.get(i));
            System.out.println("End of ISRC\n\n\n\n\n\n");
        }
    }

    public void followJayZProblemList() throws Exception {
        ArrayList<String> ar = new ArrayList<String>();
        ar.add("USDJM0400007");
        ar.add("USUR19801711");
        for(int i=0; i< ar.size();i++) {
            rhp.search_isrc(ar.get(i));
            System.out.println("End of ISRC\n\n\n\n\n\n");
        }
    }

    public void followKlubbb3ProblemList() throws Exception {
        ArrayList<String> ar = new ArrayList<String>();
        ar.add("DEUL31700007");
        ar.add("DEUL31700020");
        ar.add("DEUL31700022");
        ar.add("DEUL31700017");
        ar.add("DEUL31700023");
        ar.add("DEUL31700021");

        for(int i=0; i< ar.size();i++) {
            rhp.search_isrc(ar.get(i));
            System.out.println("End of ISRC\n\n\n\n\n\n");
        }
    }
    public static void main(String[] args) throws Exception {
        int verbocity=0;
        for(int i=0; i<args.length;i++) {
            if(args[i].equals("-v"))
                verbocity=Integer.parseInt(args[i+1]);
        }
        RunSQL sql = new RunSQL(verbocity);
        //rhp.test();
        //rhp.test2();
        long t = System.currentTimeMillis();
        String release_artist_id_avicii="10025095";
        String release_artist_id_jayz="10043015";
        String canopus="10260300";  //Keany forgetit
        canopus="10263668"; //Klubbb3
        sql.runSQL(sql.getSQL(canopus));
        //sql.followAviciiProblemList();
        //sql.followJayZProblemList();
        //sql.followKlubbb3ProblemList();
        long d = (System.currentTimeMillis() - t)/1000;
        if(verbocity>=0)
            System.out.println("Elapsed time = "+d);
    }
}
