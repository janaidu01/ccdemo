package retail_db

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
  * Created by itversity on 07/06/18.
  */
object GetRevenueForEachOrderDF {
  def main(args: Array[String]): Unit = {
    val env = args(0)
    val props = ConfigFactory.load
    val spark = SparkSession.
      builder.
      appName("Get Revenue for each order").
      master(props.getConfig(env).getString("execution.mode")).
      getOrCreate()

    spark.conf.set("spark.sql.shuffle.partitions", "2")
    import spark.implicits._

    val orderItems = spark.
      sparkContext.
      textFile(props.getConfig(env).getString("input.path") + "/order_items").
      map(s => {
        val a = s.split(",")
        (a(0).toInt, a(1).toInt, a(2).toInt,
          a(3).toInt, a(4).toFloat, a(5).toFloat)
      }).toDF("order_item_id", "order_item_order_id",
      "order_item_product_id", "order_item_quantity",
      "order_item_subtotal", "order_item_product_price")
    orderItems.
      groupBy("order_item_order_id").
      agg(sum("order_item_subtotal").alias("order_revenue")).
      withColumn("order_revenue", round($"order_revenue", 2)).
      write.
      csv(props.getConfig(env).getString("output.path") + "/ccdemo")
//
//    spark.sql("select oi.order_item_id, oi.order_item_order_id, " +
//          "(order_item_subtotal/order_revenue) * 100 pct_revenue from " +
//          "(select order_item_order_id, round(sum(order_item_subtotal), 2) order_revenue " +
//          "from order_items group by order_item_order_id) q " +
//          "join order_items oi " +
//          "on oi.order_item_order_id = q.order_item_order_id " +
//          "where (order_item_subtotal/order_revenue) * 100 >= 10")
////     Get percentage of order_item_subtotal against order_revenue
////     Revenue for each order_id
//    spark.sql("select * from (select order_item_id, order_item_order_id, order_item_subtotal, " +
//      "sum(order_item_subtotal) over (partition by order_item_order_id) order_revenue, " +
//      "round(order_item_subtotal/sum(order_item_subtotal) over (partition by order_item_order_id) * 100, 2) pct_revenue " +
//      "from order_items) q where q.pct_revenue >= 10 " +
//      "order by order_item_id, pct_revenue desc")

//    orderItems.createTempView("order_items")
//    spark.sql("select order_item_order_id, " +
//      "round(sum(order_item_subtotal), 2) as order_revenue " +
//      "from order_itmes group by order_item_order_id")
  }

}
