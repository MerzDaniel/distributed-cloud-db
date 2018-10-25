package lib.message;

public class KVMessageMarshaller {
    private static KVMessageMarshaller instance;

    private KVMessageMarshaller(){}

    public static KVMessageMarshaller getInstance() {
        if (instance != null) {
            instance = new KVMessageMarshaller();
        }
        return instance;
    }

    public String marshall(KVMessage kvMessage){
        return kvMessage.getStatus().name()
                + "<"
                + escapeSpecialCharacters(kvMessage.getKey())
                + ","
                + escapeSpecialCharacters(kvMessage.getValue())
                + ">";
    }

    private String escapeSpecialCharacters(String string){
        return string.replaceAll("/", "//").replaceAll("<", "/<").replaceAll(",", "/,").replaceAll(">", "/>");
    }
}
