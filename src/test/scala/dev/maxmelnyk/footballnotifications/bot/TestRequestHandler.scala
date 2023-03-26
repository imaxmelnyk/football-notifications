package dev.maxmelnyk.footballnotifications.bot

import com.bot4s.telegram.api.RequestHandler

import scala.util.Try

trait TestRequestHandler extends RequestHandler[Try]
