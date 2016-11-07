package client

import api.*
import org.json.simple.JSONObject
import org.json.simple.JSONArray
import org.json.simple.JSONValue
import java.net.ConnectException

val HOST = "127.0.0.1"
val PORT = 9944

fun main(args: Array<String>) {

  fun getopt(msg: String) : String? {
    print("$msg: ")
    return readLine()
  }

  fun getline(msg: String) : String {
    print(msg)
    val line = readLine()
    return if (line == null) "" else line
  }

  fun printCmds() {
      println("""
      |get
      |set id
      |set user
      |create
      |query location
      |query users
      |update aps
      |update gps
      |q
      """.trimMargin())
  }

  //apsToJson(arrayOf(AP("testssid","testbssid"), AP("123", "456")))
  //gpsToJson(GPS(1.0,2.0))

  var appUser = User("id000000","username", 0)

  loop@ while (true) {
    try {
      when(getline(">")) {
          "get" -> println("id = '${appUser.id}', user = '${appUser.name}'")
          "set id" -> appUser = User(getline("id: "), appUser.name,appUser.lastUpdated)
          "set user" -> appUser = User(appUser.id, getline("user: "), appUser.lastUpdated)
          "create" -> {
            val requestedUser = getline("initial username: ")
            val createJson = createToJson(requestedUser)
            println("SEND: ${createJson.toString()}")
            val createResponse = sendJsonWithResponse(HOST,PORT,createJson)
            println("RECV: $createResponse")
            val innerjson = createResponse.get("CreateResponse") as JSONObject
            val newid = if (innerjson.get("id") == null) "none" else innerjson.get("id") as String
            println("Updated id ${appUser.id} -> $newid")
            appUser = User(newid, appUser.name, appUser.lastUpdated)
          }
          "query location" -> {
            val location = getline("location: ")
            val queryJson = queryLocationToJson(appUser, location)
            println("SEND: ${queryJson.toString()}")
            val queryResponse = sendJsonWithResponse(HOST,PORT,queryJson)
            println("RECV: $queryResponse")
          }
          "query users" -> {
            val numUsers = try {getline("# users: ").toInt()} catch (e: Exception) {0}
            val users = Array<String>(numUsers, {getline("user: ")})
            val queryJson = queryUsersToJson(appUser, users)
            println("SEND: ${queryJson.toString()}")
            val queryResponse = sendJsonWithResponse(HOST,PORT,queryJson)
            println("RECV: $queryResponse")
          }
          "update aps" -> {
            val numAps = try {getline("# aps: ").toInt()} catch (e: Exception) {0}
            val aps = Array<api.AP>(numAps, {api.AP(getline("ssid: "), getline("bssid: "))})
            val apsJson : JSONObject = api.apsToJson(aps)
            val updateJson = updateToJson(appUser, apsJson)
            println("SEND: ${updateJson.toString()}")
            sendJsonNoResponse(HOST,PORT,updateJson)
          }
          "update gps" -> {
            val gps = try {
              GPS(getline("lat: ").toDouble(), getline("lon: ").toDouble())
            } catch(e: Exception){
              GPS(0.0,0.0)
            }
            val gpsJson = gpsToJson(gps)
            val updateJson = updateToJson(appUser,gpsJson)
            println("SEND: ${updateJson.toString()}")
            sendJsonNoResponse(HOST,PORT,updateJson)
          }
          "q" -> break@loop
          else -> {printCmds()}
        }
    }
    catch (e: ConnectException) {
      println("Could not connect to $HOST on $PORT")
    }
  }
}
