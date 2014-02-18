package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import helpers.Sha256

case class User(id: String, name: String, password: String, permission: String)

object User {
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.id") ~
    get[String]("user.name") ~
    get[String]("user.password") ~
    get[String]("user.permission") map {
      case id~name~password~permission => User(id, name, password,permission)
    }
  }
  
  // -- Queries
  
  /**
   * Retrieve a User from email.
   */
  def findById(id: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where id = {id}").on(
        'id -> id
      ).as(User.simple.singleOpt)
    }
  }
  
  /**
   * Retrieve a device from the id.
   */
  def isManager(permission: String): Boolean = {
    if(permission.equals("manager")){
    	return true
    } else{
      return false      
    }	
  }
  
  /**
   * Retrieve a device from the id.
   */
  def isAdmin(permission: String): Boolean = {
    if(permission.equals("admin")){
    	return true
    } else{
      return false      
    }	
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(User.simple *)
    }
  }
  
  /**
   * Authenticate a User.
   */
  def authenticate(id: String, password: String): Option[User] = {
    val encryptedPassword = Sha256.hex_digest(password)
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where 
         id = {id} and password = {password}
        """
      ).on(
        'id -> id,
        'password -> encryptedPassword
      ).as(User.simple.singleOpt)
    }
  }
   
  /**
   * Create a User.
   */
  def create(user: User): User = {
    val encryptedPassword = Sha256.hex_digest(user.password)
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {id}, {name}, {password}, {permission}
          )
        """
      ).on(
        'id -> user.id,
        'name -> user.name,
        'password -> encryptedPassword,
        'permission -> user.permission
      ).executeUpdate()
      
      user
      
    }
  }
  
  /**
   * change password.
   *
   * @param id The user id
   * @param user The user values.
   */
  def update(id: String, password: String) = {
    val encryptedPassword = Sha256.hex_digest(password)
    DB.withConnection { implicit connection =>
      SQL(
        """
          update user
          set password = {password} 
          where id = {id}
        """
      ).on(
    	'id -> id,
        'password -> encryptedPassword
      ).executeUpdate()
    }
  }
}
