package kinoko.common;

public final class GameConstants {
    public static final byte[] AES_USER_KEY = new byte[]{
            0x13, 0x00, 0x00, 0x00, 0x52, 0x00, 0x00, 0x00, 0x2A, 0x00, 0x00, 0x00, 0x5B, 0x00, 0x00, 0x00,
            0x08, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00,
            0x06, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x43, 0x00, 0x00, 0x00, 0x0F, 0x00, 0x00, 0x00,
            (byte) 0xB4, 0x00, 0x00, 0x00, 0x4B, 0x00, 0x00, 0x00, 0x35, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00,
            0x1B, 0x00, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x5F, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00,
            0x0F, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x1B, 0x00, 0x00, 0x00,
            0x33, 0x00, 0x00, 0x00, 0x55, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00,
            0x52, 0x00, 0x00, 0x00, (byte) 0xDE, 0x00, 0x00, 0x00, (byte) 0xC7, 0x00, 0x00, 0x00, 0x1E, 0x00, 0x00, 0x00,
    };

    public static final byte[] WZ_GMS_IV = new byte[]{0x4D, 0x23, (byte) 0xC7, 0x2B};
    public static final byte[] WZ_MSEA_IV = new byte[]{(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9};
    public static final byte[] WZ_EMPTY_IV = new byte[4];

    public static final int WZ_OFFSET_CONSTANT = 0x581C3F6D;
}
