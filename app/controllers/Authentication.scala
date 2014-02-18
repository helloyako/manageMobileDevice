package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

object Authentication extends Controller {
  
  val loginForm = Form(
    tuple(
      "id" -> text,
      "password" -> text
    ) verifying ("Invalid Id or password", result => result match {
      case (id, password) => User.authenticate(id, password).isDefined
    })
  )
  
  /**
   * Handle login form submission.
   */
  def authenticate(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.list(
      Device.list(page = page),orderBy, filter,formWithErrors
    )),
      user =>
        User.findById(user._1).map { user =>
          var se = session
          se = se+("id" -> user.id)
          se = se+("name" -> user.name)
          se = se+("permission" -> user.permission)
          	Redirect(routes.Application.list(page,orderBy,filter)).withSession(se)
          }.getOrElse(NotFound)
    )
  }
  
  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.list()).withNewSession
  }

}
