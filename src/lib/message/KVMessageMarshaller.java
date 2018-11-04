package lib.message;

public class KVMessageMarshaller {

    private KVMessageMarshaller(){}

    /**
     * Returns marshalled string representation of the {@code kvMessage}
     * @param kvMessage the object to be marshalled
     * @return a string representation of the {@code kvMessage}
     */
    public static String marshall(KVMessage kvMessage){
        return kvMessage.getStatus().name()
                + "<"
                + escapeSpecialCharacters(kvMessage.getKey())
                + ","
                + escapeSpecialCharacters(kvMessage.getValue())
                + ">";
    }

    private static String escapeSpecialCharacters(String string){
        if (string == null || string.equals("")) return "";
        return string.replaceAll("/", "//").replaceAll("<", "/<").replaceAll(",", "/,").replaceAll(">", "/>");
    }
}
