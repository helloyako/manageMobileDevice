package models

import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Device(id: Pk[Long] = NotAssigned, name: String, version: Option[String], teamId: Option[Long], borrowed_date: Option[Date], borrowed_user_id: Option[String], status: Boolean)

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val current = page
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

object Device {
  
  // -- Parsers
  
  /**
   * Parse a Device from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("device.id") ~
    get[String]("device.name") ~
    get[Option[String]]("device.version") ~
    get[Option[Long]]("device.team_id") ~
    get[Option[Date]]("device.borrowed_date") ~
    get[Option[String]]("device.borrowed_user_id") ~
    get[Boolean]("device.status") map {
      case id~name~version~teamId~borrowed_date~borrowed_user_id~status => Device(id, name, version, teamId, borrowed_date, borrowed_user_id,status)
    }
  }
  
  /**
   * Parse a (Device,Team) from a ResultSet
   */
  val withTeam = Device.simple ~ (Team.simple ?) ~ (User.simple ?) map {
    case device~team~user => (device,team,user)
  }
  
  // -- Queries
  
  /**
   * Retrieve a device from the id.
   */
  def findById(id: Long): Option[Device] = {
    DB.withConnection { implicit connection =>
      SQL("select * from device where id = {id}").on('id -> id).as(Device.simple.singleOpt)
    }
  }
  
  
  /**
   * Return a page of (Device,Team).
   *
   * @param page Page to display
   * @param pageSize Number of devices per page
   * @param orderBy Device property used for sorting
   * @param filter Filter applied on the name column
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = -1, filter: String = "%"): Page[(Device, Option[Team], Option[User])] = {
    
    val offest = pageSize * page
    
    DB.withConnection { implicit connection =>
      
      val devices = SQL(
        """
          select *
          from device 
          left join team on device.team_id = team.id
          left join user on team.manager_id = user.id
          where device.name like {filter}
          order by {orderBy} nulls last
          limit {pageSize} offset {offset}
        """
      ).on(
        'pageSize -> pageSize, 
        'offset -> offest,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(Device.withTeam *)

      val totalRows = SQL(
        """
          select count(*) from device 
          left join team on device.team_id = team.id
          where device.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(devices, page, offest, totalRows)
      
    }
    
  }
  
  /**
   * Update a device.
   *
   * @param id The device id
   * @param device The device values.
   */
  def update(id: Long, device: Device) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update device
          set name = {name}, version = {version}, team_id = {team_id} 
          where id = {id}
        """
      ).on(
        'id -> id,
        'name -> device.name,
        'version -> device.version,
        'team_id -> device.teamId
      ).executeUpdate()
    }
  }
  
  def rentDivice(id: Long,borrowdUser: String) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update device
          set borrowed_date = {borrowed_date}, borrowed_user_id = {borrowed_user_id}, status = {status}
          where id = {id}
        """
      ).on(
        'id -> id,
        'borrowed_date -> new Date(),
        'borrowed_user_id -> borrowdUser,
        'status -> false
      ).executeUpdate()
    }
    
  }
  
  def returnDivice(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update device
          set borrowed_date = {borrowed_date}, borrowed_user_id = {borrowed_user_id}, status = {status}
          where id = {id}
        """
      ).on(
        'id -> id,
        'borrowed_date -> Option.empty[Date],
        'borrowed_user_id -> Option.empty[String],
        'status -> true
      ).executeUpdate()
    }
    
  }
  
  /**
   * Insert a new device.
   *
   * @param device The device values.
   */
  def insert(device: Device) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into device(name,version,team_id,borrowed_date,borrowed_user_id,status) values (            
            {name}, {version}, {team_id}, {borrowed_date}, {borrowed_user_id}, {status}
          )
        """
      ).on(
        'name -> device.name,
        'version -> device.version,        
        'team_id -> device.teamId,
        'borrowed_date -> Option.empty[Date],
        'borrowed_user_id -> Option.empty[String],
        'status -> true
      ).executeUpdate()
    }
  }
  
  /**
   * Delete a device.
   *
   * @param id Id of the device to delete.
   */
  def delete(id: Long) = {
    DB.withConnection { implicit connection =>
      SQL("delete from device where id = {id}").on('id -> id).executeUpdate()
    }
  }
  
}