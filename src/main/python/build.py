import os, glob

curdir = os.path.abspath(os.curdir)
os.chdir("../../..")
os.system("mvn clean assembly:directory")
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
