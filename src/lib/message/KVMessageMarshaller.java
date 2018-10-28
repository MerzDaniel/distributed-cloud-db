package lib.message;

public class KVMessageMarshaller {

    private KVMessageMarshaller(){}

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
