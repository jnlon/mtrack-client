SRC = client.kt
OUT = client.jar
KTC = kotlinc-jvm
CLASSPATH = json-simple/json-simple-1.1.1.jar:.
KOPTS = -include-runtime -cp $(CLASSPATH) -d $(OUT)
RUNCMD = java -jar $(OUT)

$(OUT): $(SRC) Makefile
	$(KTC) $(SRC) $(KOPTS) 
	jar -uf $(OUT) -C json-simple/classes/ .

run: $(OUT)
	$(RUNCMD)


