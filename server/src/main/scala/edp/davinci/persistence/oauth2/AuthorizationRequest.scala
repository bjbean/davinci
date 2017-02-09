//package edp.davinci.persistence.oauth2
//
//import edp.davinci.rest.LoginClass
//
//
//class AuthorizationRequest(val headers: Map[String, Seq[String]], val params: LoginClass) {
//
//  def header(name: String): Option[String] = headers.get(name).flatMap { _.headOption }
//
//  def requireHeader(name: String): String = header(name).getOrElse(throw new InvalidRequest(s"required header: $name"))
//
//  def grantType: String = OAuthGrantType.PASSWORD
//
//}
//
//case class PasswordRequest(request: AuthorizationRequest) extends AuthorizationRequest(request.headers, request.params) {
//  /**
//    * returns username.
//    *
//    * @return username.
//    * @throws InvalidRequest if the parameter is not found
//    */
//  def username = params.username
//  println("username: " + username)
//
//  /**
//    * returns password.
//    *
//    * @return password.
//    * @throws InvalidRequest if the parameter is not found
//    */
//  def password = params.password
//  println("password: " + password)
//}