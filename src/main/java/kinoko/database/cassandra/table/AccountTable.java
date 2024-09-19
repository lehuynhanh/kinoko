package kinoko.database.cassandra.table;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import kinoko.database.cassandra.type.CashItemInfoUDT;
import kinoko.database.cassandra.type.ItemUDT;

public final class AccountTable {
    public static final String ACCOUNT_ID = "account_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SECONDARY_PASSWORD = "secondary_password";
    public static final String CHARACTER_SLOTS = "character_slots";
    public static final String NX_CREDIT = "nx_credit";
    public static final String NX_PREPAID = "nx_prepaid";
    public static final String MAPLE_POINT = "maple_point";
    public static final String TRUNK_ITEMS = "trunk_items";
    public static final String TRUNK_SIZE = "trunk_size";
    public static final String TRUNK_MONEY = "trunk_money";
    public static final String LOCKER_ITEMS = "locker_items";
    public static final String WISHLIST = "wishlist";
    public static final String GM = "gm";

    private static final String tableName = "account_table";

    public static String getTableName() {
        return tableName;
    }

    public static void createTable(CqlSession session, String keyspace) {
        session.execute(
                SchemaBuilder.createTable(keyspace, getTableName())
                        .ifNotExists()
                        .withPartitionKey(ACCOUNT_ID, DataTypes.INT)
                        .withColumn(USERNAME, DataTypes.TEXT)
                        .withColumn(PASSWORD, DataTypes.TEXT)
                        .withColumn(SECONDARY_PASSWORD, DataTypes.TEXT)
                        .withColumn(CHARACTER_SLOTS, DataTypes.INT)
                        .withColumn(NX_CREDIT, DataTypes.INT)
                        .withColumn(NX_PREPAID, DataTypes.INT)
                        .withColumn(MAPLE_POINT, DataTypes.INT)
                        .withColumn(TRUNK_ITEMS, DataTypes.frozenListOf(SchemaBuilder.udt(ItemUDT.getTypeName(), true)))
                        .withColumn(TRUNK_SIZE, DataTypes.INT)
                        .withColumn(TRUNK_MONEY, DataTypes.INT)
                        .withColumn(LOCKER_ITEMS, DataTypes.frozenListOf(SchemaBuilder.udt(CashItemInfoUDT.getTypeName(), true)))
                        .withColumn(WISHLIST, DataTypes.frozenListOf(DataTypes.INT))
                        .withColumn(GM, DataTypes.INT)
                        .build()
        );
        session.execute(
                SchemaBuilder.createIndex()
                        .ifNotExists()
                        .onTable(keyspace, getTableName())
                        .andColumn(USERNAME)
                        .build()
        );
    }
}
