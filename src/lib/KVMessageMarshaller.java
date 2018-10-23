package lib;

public class KVMessageMarshaller {

    public String marshall(KVMessage kvMessage){
        return kvMessage.getStatus().name()
                + "<"
                + kvMessage.getKey().replaceAll("/", "//").replaceAll("<", "/<").replaceAll(",", "/,")
                + ","
                + kvMessage.getValue().replaceAll("/", "//").replaceAll(">", "/>").replaceAll(",", "/,")
                + ">";
    }
}
