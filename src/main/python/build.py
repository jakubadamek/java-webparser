import os, glob

skipTests = len(sys.argv) > 1 and sys.argv[1] == "skipTests"

curdir = os.path.abspath(os.curdir)
os.chdir("../../..")
mvn = "mvn clean assembly:directory"
if skipTests:
	mvn += " -DskipTests"
os.system(mvn)
dir = sorted([dir for dir in glob.glob("dist/*") if os.path.isdir(dir)])[-1]
dir = glob.glob(os.path.join(dir, "*"))[0]                                                                         
print dir
jars = glob.glob(os.path.join(dir, "*.jar"))
jars.extend(glob.glob(os.path.join(dir, "lib", "*.jar")))
for jar in jars:
	print jar
	os.system("jarsigner -keystore etc/webstart/myKeystore -storepass password " + jar + " jakubadamek")

for jar in jars:
	print '    <jar href="lib/' + os.path.basename(jar) + '"/>'

os.chdir(curdir)
