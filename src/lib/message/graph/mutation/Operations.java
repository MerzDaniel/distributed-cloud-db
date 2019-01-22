package lib.message.graph.mutation;

public enum Operations {
    /**
     * Replace a propterty in a document
     */
    REPLACE,
    /**
     * Merge values of a property in a document (for example add values to an array)
     */
    MERGE,
    /**
     * Used for data mutation in a nested data structure
     */
    NESTED,
}
