package lib;

public class KVMessageMarshaller {

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
