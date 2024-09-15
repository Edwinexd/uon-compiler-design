public enum DeclarationType {
    PROGRAM,
    CONSTANT,
    FUNCTION,
    // this should rather be STRUCT_TYPE
    STRUCT_TYPE,
    // and this could just be STRUCT i.e. an instance of a struct
    STRUCT,
    ARRAY_TYPE, // Since this also confuses me, this is the equivilance of typeid in the grammar ...
    ARRAY,
    ARRAY_CONSTANT,
    INT,
    FLOAT,
    BOOL,
    VOID;

    public boolean requiresSymbolTableRecord() {
        return this == ARRAY || this == ARRAY_CONSTANT || this == STRUCT;
    }


    // if instance struct we need to keep track of the symbol table record that defines the struct
}
