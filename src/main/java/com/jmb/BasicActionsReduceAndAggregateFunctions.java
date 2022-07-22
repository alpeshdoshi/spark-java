package com.jmb;

import com.jmb.reducers.FarmStandSalesReducer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.spark.sql.functions.*;


public class BasicActionsReduceAndAggregateFunctions {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicActionsReduceAndAggregateFunctions.class);
    private static final String SPARK_FILES_FORMAT = "csv";
    private static final String PATH_RESOURCES = "src/main/resources/spark-data/farm_stands_sales_austin.csv";

    public static void main(String[] args) throws Exception {

        LOGGER.info("Application starting up");
        BasicActionsReduceAndAggregateFunctions app = new BasicActionsReduceAndAggregateFunctions();
        app.init();
        LOGGER.info("Application gracefully exiting...");
    }

    private void init() throws Exception {
        //Create the Spark Session
        SparkSession session = SparkSession.builder()
                .appName("BasicActionsReduceAndAggregateFunctions")
                .master("local").getOrCreate();

        //Ingest data from CSV file into a DataFrame
        Dataset<Row> df = session.read()
                .format(SPARK_FILES_FORMAT)
                .option("header", "true")
                //saves us from doing excessive parsing if the numeric fields are retrieved initially as String values.
                .option("inferSchema", "true")
                .load(PATH_RESOURCES);

        //Show first 5 records of the Raw ingested DataSet
        df.show(5);

        //Drop rows not used in calculation
        Dataset<Row> numericFieldsDf = df.drop("farm_stand","month","year","days","visitors");

        Row returnRow = numericFieldsDf.reduce(new FarmStandSalesReducer());

        //Print Results of reduce, format the Row holding them to JSON
        LOGGER.info("Total Calculated Sales " + returnRow.prettyJson());

        /** MAX */

        Dataset<Row> maxSalesDf = df.agg(max(df.col("total_sales")));
        maxSalesDf.show();

        /** MIN */

        Dataset<Row> minSalesDf = df.agg(min(df.col("total_sales")));
        minSalesDf.show();

        /** MIN */

        Dataset<Row> meanSalesDf = df.agg(mean(df.col("total_sales")));
        meanSalesDf.show();
    }

}