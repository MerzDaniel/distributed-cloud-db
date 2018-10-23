package lib;

public class KVMessageMarshaller {

    public String marshall(KVMessage kvMessage){
        //todo
        return kvMessage.getStatus().name() + "<" + kvMessage.getKey().replaceAll("<", "/<").replaceAll(",", "/,") + "," + kvMessage.getValue() + ">";
    }
}
