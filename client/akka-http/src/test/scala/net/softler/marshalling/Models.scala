package net.softler.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import net.softler.marshalling.Models.User
import spray.json.{ DefaultJsonProtocol, JsonWriter, RootJsonFormat }

trait Models {
  def user: User = User(1, "Name", "test@test.de")
}

object Models {
  case class User(id: Long, name: String, email: String)
}

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  import Models._
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
}
