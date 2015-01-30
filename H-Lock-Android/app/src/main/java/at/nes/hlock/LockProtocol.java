package at.nes.hlock;

/**
 * Created by Andraz Pajtler on 24/01/15.
 */
public class LockProtocol {
    public static class types {
        public static final byte REGISTRATION_REQ = 1;
        public static final byte KEY_EXCHANGE = 2;
        public static final byte LOCK_REQ = 3;
        public static final byte UNLOCK_REQ = 4;
        public static final byte ERROR_REGISTRATION_FAILED = 5;
        public static final byte ERROR_AUTHENTICATION = 6;
        public static final byte ERROR_WRONG_ID = 7;
        public static final byte ERROR_WRONG_COMMAND = 8;
        public static final byte STATUS_UNLOCKED = 0;
        public static final byte STATUS_LOCKED = 1;
    }

    // message parts length
    public static class lengths {
        public static final int TYPE = 1;
        public static final int RANDOM = 4;
        public static final int ID = 1;
        public static final int HMAC_KEY = 18;
        public static final int HMAC= 14;
        public static final int MESSAGE_FIRST_PART = TYPE + RANDOM + ID;
        public static final int MESSAGE = 20;
    }

    public static class positions {
        public static final int RANDOM = lengths.TYPE;
        public static final int ID = lengths.TYPE + lengths.RANDOM;
        public static final int HMAC= lengths.TYPE + lengths.RANDOM + lengths.ID;
    }
}
