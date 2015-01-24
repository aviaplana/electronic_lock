package db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by Andraz Pajtler on 17/06/14.
 */

@DatabaseTable(tableName = "Locks")
public class Lock {
    public static final String ADDRESS_FIELD_NAME = "address";
    public static final String NAME_FIELD_NAME = "name";
    public static final String SECRET_KEY_FIELD_NAME = "secretKey";


    @DatabaseField(id = true, columnName = ADDRESS_FIELD_NAME)
    public String address;

    @DatabaseField(columnName = NAME_FIELD_NAME)
    public String name;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = SECRET_KEY_FIELD_NAME)
    public byte[] secretKey;


    public Lock(){
        // needed by ormlite
    }

    public Lock(String address, String name, byte[] secretKey) {
        this.address = address;
        this.name = name;
        this.secretKey = secretKey;
    }
}
