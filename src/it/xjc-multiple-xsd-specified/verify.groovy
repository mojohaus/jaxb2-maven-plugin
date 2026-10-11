File jaxbDir = new File( basedir, "target/generated-sources/jaxb" )
assert jaxbDir.isDirectory() : "Could not find directory for JAXB generated sources: " + jaxbDir

for ( String name in ["AType", "BType", "CType"] )
{
    File file = new File( jaxbDir, "com/example/myschema/" + name + ".java" )
    assert file.isFile() : "Could not find file: " + file
}

File dType = new File( jaxbDir, "com/example/myschema/DType.java" )
assert !dType.exists() : "File '" + dType + "' found, which shouldn't exist"
