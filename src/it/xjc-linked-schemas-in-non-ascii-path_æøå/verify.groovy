File buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

String pathToGenerated = "target/generated-sources/jaxb/test/nonascii/"
File mainType = new File(basedir, pathToGenerated + "MainType.java")
File subType = new File(basedir, pathToGenerated + "SubType.java")
File objectFactory = new File(basedir, pathToGenerated + "ObjectFactory.java")

assert mainType.exists() && mainType.isFile(), "Missing expected generated file: " + mainType.path
assert subType.exists() && subType.isFile(), "Missing expected generated file: " + subType.path
assert objectFactory.exists() && objectFactory.isFile(), "Missing expected generated file: " + objectFactory.path
