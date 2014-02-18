package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import views._
import models._

/**
 * Manage a database of devices
 */
object Application extends Controller { 
  
  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list(0, -1, ""))
  
  /**
   * Describe the device form (used in both edit and create screens).
   */ 
  val deviceForm = Form(
    mapping(
      "id" -> ignored(NotAssigned:Pk[Long]),
      "name" -> nonEmptyText,
      "version" -> optional(text),
      "teamId" -> optional(longNumber),
      "borrowed_date" -> optional(date("yyyy-MM-dd")),
      "borrowed_user_id" -> optional(text),
      "status" -> boolean
    )(Device.apply)(Device.unapply)
  )
  
   val userForm = Form(
    mapping(
      "id" -> nonEmptyText.verifying("User name is already taken", id => !User.findById(id).isDefined),
      "name" -> nonEmptyText,
      "password" -> nonEmptyText,
      "permission" -> nonEmptyText      
    )(User.apply)(User.unapply)
  )
  
  val loginForm = Form(
    tuple(
      "id" -> text,
      "password" -> text
    ) verifying ("Invalid id or password", result => result match {
      case (id, password) => User.authenticate(id, password).isDefined
    })
  )
  
  // -- Actions

  /**
   * Handle default path requests, redirect to devices list
   */  
  def inde = Action { Home }
  
  /**
   * Display the paginated list of devices.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on device names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.list(
      Device.list(page = page, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter,loginForm
    ))
  }
  
  /**
   * Display the 'edit form' of a existing Device.
   *
   * @param id Id of the device to edit
   */
  def edit(id: Long) = Action { implicit request =>
    request.session.get("permission").map{ permission =>
      if(User.isAdmin(permission) || User.isManager(permission)){
    	  Device.findById(id).map { device =>
          	Ok(html.editForm(id, deviceForm.fill(device), Team.options, loginForm))
          }.getOrElse(NotFound)
      } else{
        Unauthorized("You can't access this page. Only Admin page")
      }
    }.getOrElse{Unauthorized("Wrong access")}
  }
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the device to edit
   */
  def update(id: Long) = Action { implicit request =>
    deviceForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors, Team.options, loginForm)),
      device => {
        Device.update(id, device)
        Home.flashing("success" -> "%s has been updated".format(device.name))
      }
    )
  }
  
  /**
   * Rent Device.
   *
   * 
   */
  def rentDevice(id: Long, name: String, page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    request.session.get("id").map { borrowedUser =>
      Device.rentDivice(id, borrowedUser)
      Redirect(routes.Application.list(page, orderBy, filter)).flashing("success" -> "%s has been rented".format(name))
    }.getOrElse(Home.flashing("warning" -> "Sign in please"))
    
  }
  
  /**
   * Return Device.
   *
   * 
   */
  def returnDevice(id: Long, name: String, page: Int, orderBy: Int, filter: String) = Action { implicit request =>    
    Device.returnDivice(id)
    Redirect(routes.Application.list(page, orderBy, filter)).flashing("success" -> "%s has been returned".format(name))
  }
  
  
  /**
   * Display the 'new device form'. 
   */
  def createDevice = Action { implicit request =>
    request.session.get("permission").map{ permission =>
      if(User.isAdmin(permission) || User.isManager(permission)){
          Ok(html.createDeviceForm(deviceForm, Team.options, loginForm))
      } else{
        Unauthorized("You can't access this page. Only Admin page")
      }
    }.getOrElse(Unauthorized("Wrong access"))
  }
  
  /**
   * Handle the 'new device form' submission.
   */
  def createDeviceSave = Action { implicit request =>
    deviceForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createDeviceForm(formWithErrors, Team.options, loginForm)),
      device => {
        Device.insert(device)
        Home.flashing("success" -> "%s has been created".format(device.name))
      }
    )
  }
  
  /**
   * Handle device deletion.
   */
  def delete(id: Long) = Action {
    Device.delete(id)
    Home.flashing("success" -> "Device has been deleted")
  }
  
  
  val changePasswordForm = Form(
    tuple(
      "id" -> nonEmptyText,
      "oldPassword" -> nonEmptyText,      
      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text
      ).verifying(
        // Add an additional constraint: both passwords must match
        "Passwords don't match", passwords => passwords._1 == passwords._2
      )
    ) verifying ("Wrong password", result => result match {
      case (id, oldPassword,password) => User.authenticate(id, oldPassword).isDefined
    })
  )
  
  
  /**
   * Display the 'change password form' of a existing User.
   *
   * @param id Id of the user to edit
   */
  def changePassword(id: String) = Action { implicit request =>
    request.session.get("id").map{ sessionId =>
      if(sessionId.equals(id)){        
          	Ok(html.changePassword(id, changePasswordForm, loginForm))
      } else{
        Unauthorized("Wrong access")
      }
    }.getOrElse{Unauthorized("Wrong access")}
  }
  
  def passwordUpdate(id: String) = Action { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      errors => BadRequest(html.changePassword(id, errors, loginForm)),
      user => {
        User.update(id, user._3._1)
        Home.flashing("success" -> "changed your password")
      }
    )
  }
  
  
  /**
   * Display the 'new device form'. 
   */
  def createUser = Action { implicit request =>
    request.session.get("permission").map{ permission =>
      if(User.isAdmin(permission) || User.isManager(permission)){
          Ok(html.createUserForm(userForm, loginForm))
      } else{
        Unauthorized("You can't access this page. Only Admin page")
      }
    }.getOrElse(Unauthorized("Wrong access"))
  }
  
   /**
   * Handle the 'new user form' submission.
   */
  def createUserSave = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createUserForm(formWithErrors, loginForm)),
      user => {
        User.create(user)
        Home.flashing("success" -> "%s has been created".format(user.id))
      }
    )
  }
  
  
}
