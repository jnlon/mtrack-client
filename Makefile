PWD=$(shell pwd)
KTC = kotlinc-jvm
CLASSPATH = -cp $(PWD)/lib/json-simple/classes/:$(PWD)/build
KOPTS = -nowarn -include-runtime $(CLASSPATH) -d
API = api/api.kt
CLIENT = client/client.kt
ALLSRC = $(API) $(CLIENT)

BUILD = build
CLIENTOUT = $(BUILD)/client.jar
APIOUT = $(BUILD)/api.jar
RUNCMD = java -jar $(CLIENTOUT)

all: $(CLIENTOUT) ;

$(CLIENTOUT): $(BUILD)
	$(KTC) $(KOPTS) $(CLIENTOUT) $(ALLSRC)

api: $(BUILD)
	$(KTC) $(KOPTS) $(APIOUT) $(API) 

run: $(CLIENTOUT)
	$(RUNCMD)

clean: 
	rm -r $(BUILD)

$(BUILD):
	mkdir $(BUILD)


