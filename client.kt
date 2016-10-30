package client

import java.net.Socket
import java.nio.ByteBuffer
import org.json.simple.JSONObject
import org.json.simple.JSONArray

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
  innerjson.put("username", requestedUsername)
  outerjson.put("create", innerjson)
  return outerjson
}

fun updateToJson(appUser: User, updateData: JSONObject) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()

  innerjson.put("id", appUser.id)
  innerjson.put("username", appUser.name)
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

fun queryUserToJson(appUser: User, targetUser: String) : JSONObject {
  val outerjson = JSONObject()
  val innerjson = JSONObject()
  innerjson.put("id", appUser.id)
  innerjson.put("username", targetUser)
  outerjson.put("Query", innerjson)
  return outerjson
}

// Don't expect a result back from an update
fun sendUpdate(host: String, port: Int, updateJson: JSONObject) {
  val output = Socket(host, port).getOutputStream()
  val jsonBytes = updateJson.toString().toByteArray()
  val jsonLengthBytes = ByteBuffer.allocate(4).putInt(0,jsonBytes.size).array()
  output.write(jsonLengthBytes)
  output.write(jsonBytes)
  output.close()
}

/*fun sendQueryGetResponse(host: String, port: Int, JSONObject) : JSONObject {
  val sock = Socket(host, port)
}*/


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
      |query username
      |update aps
      |update gps
      |q
      """.trimMargin())
  }

  //apsToJson(arrayOf(AP("testssid","testbssid"), AP("123", "456")))
  //gpsToJson(GPS(1.0,2.0))

  var user = User("id000000","username", 0)

  loop@ while (true) {
    when(getline(">")) {
      "get" -> println("id = '${user.id}', user = '${user.name}'")
      "set id" -> user = User(getline("id: "), user.name,user.lastUpdated)
      "set user" -> user = User(user.id, getline("user: "), user.lastUpdated)
      "create" -> {
        val user = getline("initial username: ")
      
      }
      "query location" -> {
        val location = getline("location: ")
        val queryJson = queryLocationToJson(user, location)
        println(queryJson.toString())
      }
      "query username" -> { }
      "update aps" -> {
        val num_aps = try {getline("# aps: ").toInt()} catch (e: Exception) {0}
        val aps = Array<AP>(num_aps, {AP(getline("ssid: "), getline("bssid: "))})
        val apsJson = apsToJson(aps)
        val updateJson = updateToJson(user, apsJson)
        println(updateJson.toString())
      }
      "update gps" -> {
        val gps = try {
          GPS(getline("lat: ").toDouble(), getline("lon: ").toDouble())
        } catch(e: Exception){
          GPS(0.0,0.0)
        }
        val gpsJson = gpsToJson(gps)
        val updateJson = updateToJson(user,gpsJson)
        println(updateJson.toString())
      }
      "q" -> break@loop
      else -> {printCmds()}
    }
  }
}
