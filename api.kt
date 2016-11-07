package api

import java.net.Socket
import java.net.ConnectException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import org.json.simple.JSONObject
import org.json.simple.JSONArray
import org.json.simple.JSONValue

val HOST = "127.0.0.1"
val PORT = 9944

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


