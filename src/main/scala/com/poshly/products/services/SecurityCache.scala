package com.poshly.products.services

import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.poshly.core.logging.Loggable
import spray.caching.{Cache, LruCache}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration


trait SecurityCache extends Loggable {

//  val apiKeyMap = Map[String,String]("poshly"->"12345", "poshly1"-> "54321")

//  def getApiKey(source: String): Future[String]

  private val tokenCache: Cache[String] =  LruCache(maxCapacity = 5000, timeToLive = Duration(30, TimeUnit.MINUTES),
    timeToIdle = Duration(10, TimeUnit.MINUTES))
  private val signatureCache: Cache[String] =  LruCache(maxCapacity = 5000, timeToLive = Duration(30, TimeUnit.MINUTES),
    timeToIdle = Duration(10, TimeUnit.MINUTES))
  private val apiKeyCache: Cache[String] = LruCache()

  private val duration5min = Duration(5, TimeUnit.MINUTES).toMillis

  def getApiKeyCached(source: String): Future[String] = apiKeyCache(source){
    Future("7d0f5be79c6c418d983fa1e92d1c7cb2") // poshly api key
  }

  def calcHashCache(source: String, timestamp: Long, token: String, apiKey: String): Future[String] = {
    signatureCache(source, timestamp, token) {
      calcHash(source, timestamp, token, apiKey)
    }
  }

  def tokenGenCache(timestamp: Long) = tokenCache(timestamp){
    UUID.randomUUID().toString
  }

  def timeIsWithinServerTime(timestamp: Long): Boolean = {
    val time = System.currentTimeMillis()
    timestamp >= time - duration5min && timestamp <= time + duration5min
  }

  final def newToken(timestamp: Long): Future[String] = {
    if(timeIsWithinServerTime(timestamp)) {
      tokenGenCache(timestamp)
    } else {
      Future(throw new Exception ("Time not within server time"))
    }
  }

  final def getToken(timestamp: Long): Future[String] = {
    tokenCache.get(timestamp).getOrElse(Future(throw new Exception("Invalid timestamp: " + timestamp.toString)))
  }

  final def checkSignature(source: String, timestamp: Long, signature: String): Future[Boolean] = {
    def signatureFromToken(token: String) = {
      for {
        apiKey <- getApiKeyCached(source)
        signature <- calcHashCache(source, timestamp, token, apiKey).map(signature.equalsIgnoreCase)
      } yield signature
    }

    val signatureFuture = for {
      token <- getToken(timestamp)
      signature <- signatureFromToken(token)
    } yield signature

    signatureFuture.recover{
      case t: Throwable => {
        logger.debug("signature check failed ", t)
        false
      }
    }
  }

  final def calcHash(source: String, timestamp: Long, token: String, apiKey: String): String = {
    val str = s"${source}_${timestamp}_${token}_${apiKey}_salt"
    val md = MessageDigest.getInstance("SHA-256")
    md.update(str.getBytes("UTF-8"))
    val bytes = md.digest()
    bytesToHex(bytes)
  }

  final def bytesToHex(bytes: Array[Byte]): String = bytes.map("%02X" format _).mkString
}
