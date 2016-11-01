package client

import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import org.json.simple.JSONObject
import org.json.simple.JSONArray
import org.json.simple.JSONValue

val HOST = "127.0.0.1"
val PORT = 9993

class AP(ssid: String, bssid: String) {
  public val ssid = ssid
  public val bssid = bssid
}

class GPS(lat: Double, lon: Double) {
  val lat = lat;
  val lon = lon
}

class User(id: String, name: String, lastUpdated: Long) {
  val id = id
  val name = name;
  val lastUpdated = lastUpdated
}

class APIException : Exception() { }

fun gpsToJson(gps: GPS) : JSONObject {
  val gpsdata = JSONObject()
  val latlon = JSONObject()
  latlon.put("lat", gps.lat)
  latlon.put("lon", gps.lon)
  gpsdata.put("gps", latlon)
  return gpsdata
}

fun apsToJson(aps: Array<AP>) : JSONObject {
  val apsdata = JSONObject()
  val apsarray = JSONArray()
  for (ap in aps) {
    val apjson = JSONObject()
    apjson.put("ssid", ap.ssid)
    apjson.put("bssid", ap.bssid)
    apsarray.add(apjson)
  }
  apsdata.put("aps", apsarray)
  return apsdata
}

fun createToJson(requestedUsername: String) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()
  innerjson.put("user", requestedUsername)
  outerjson.put("Create", innerjson)
  return outerjson
}

fun updateToJson(appUser: User, updateData: JSONObject) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()

  innerjson.put("id", appUser.id)
  innerjson.putAll(updateData)
  outerjson.put("Update", innerjson)

  return outerjson
}

fun queryLocationToJson(appUser: User, location: String) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()
  innerjson.put("id", appUser.id)
  innerjson.put("location", location)
  outerjson.put("Query", innerjson)
  return outerjson
}

fun queryUsersToJson(appUser: User, targetUsers: Array<String>) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()
  innerjson.put("id", appUser.id)
  innerjson.put("users", targetUsers)
  outerjson.put("Query", innerjson)
  return outerjson
}

// Don't expect a result back from an update
fun sendJsonNoResponse(host: String, port: Int, updateJson: JSONObject) {
  val output = DataOutputStream(Socket(host, port).getOutputStream())
  val jsonBytes = updateJson.toString().toByteArray()
  val jsonLengthBytes = ByteBuffer.allocate(4).putInt(0,jsonBytes.size).array()
  output.write(jsonLengthBytes)
  output.write(jsonBytes)
  output.close()
}

fun sendJsonWithResponse(host: String, port: Int, updateJson: JSONObject) : JSONObject {
  val sock = Socket(host, port)
  val input = DataInputStream(sock.getInputStream())
  val output = DataOutputStream(sock.getOutputStream())
  val jsonBytes = updateJson.toString().toByteArray()
  val jsonLengthBytes = ByteBuffer.allocate(4).putInt(0, jsonBytes.size).array()
  output.write(jsonLengthBytes)
  output.write(jsonBytes)
  val toRead = input.readInt()
  val readJsonBytes = ByteArray(toRead)
  input.read(readJsonBytes, 0, toRead)
  sock.close()
  val jsonString = String(readJsonBytes, StandardCharsets.UTF_8)
  return (JSONValue.parseWithException(jsonString) as JSONObject)
}


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
        val aps = Array<AP>(numAps, {AP(getline("ssid: "), getline("bssid: "))})
        val apsJson = apsToJson(aps)
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
}
