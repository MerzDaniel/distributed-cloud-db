package lib.metadata;

import lib.message.MarshallingException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

    private List<MetaContent> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<MetaContent> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public KVStoreMetaData() {

    }

    public List<MetaContent> getKvServerList() {
        return kvServerList;
    }

    public static String marshall(KVStoreMetaData kvStoreMetaData) {
        return kvStoreMetaData.getKvServerList().stream().map(it -> MetaContent.marshall(it)).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    public static KVStoreMetaData unmarshall(String kvStoreMetaData) throws MarshallingException {
        List<MetaContent> metaContentList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            MetaContent metaContent = MetaContent.unmarshall(kvServer);
            metaContentList.add(metaContent);
        }

        return new KVStoreMetaData(metaContentList);
    }

}
